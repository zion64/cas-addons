package net.unicon.cas.addons.authentication.strong.yubikey;

import java.security.GeneralSecurityException;

import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadUsernameOrPasswordAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * An authentication handler that uses the Yubico cloud validation platform to authenticate
 * one-time password tokens that are issued by a YubiKey device. To use YubiCloud you need a
 * client id and an API key which must be obtained from Yubico.
 * <p/>
 * <p>For more info, please visit <a href="http://yubico.github.io/yubico-java-client/">this link</a></p>
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 1.5
 */
public class YubiKeyAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements
        InitializingBean {

    private YubiKeyAccountRegistry registry = new AcceptAnyYubiKeyAccountRegistry();

    private YubicoClient client;

    /**
     * Prepares the Yubico client with the received clientId and secretKey. By default,
     * all YubiKey accounts are allowed to authenticate.
     * <p/>
     * WARNING: THIS CONSTRUCTOR RESULTS IN AN EXAMPLE YubiKeyAuthenticationHandler
     * CONFIGURATION THAT CONSIDERS ALL Yubikeys VALID FOR ALL USERS.  YOU MUST NOT USE
     * THIS CONSTRUCTOR IN PRODUCTION.
     *
     * @param clientId
     * @param secretKey
     */
    public YubiKeyAuthenticationHandler(final Integer clientId, final String secretKey) {
        this.client = YubicoClient.getClient(clientId);
        this.client.setKey(secretKey);
    }

    /**
     * Prepares the Yubico client with the received clientId and secretKey. If you wish to
     * limit the usage of this handler only to a particular set of yubikey accounts for a special
     * group of users, you may provide an compliant implementation of {@link YubiKeyAccountRegistry}.
     * By default, all accounts are allowed.
     *
     * @param clientId
     * @param secretKey
     * @param registry
     */
    public YubiKeyAuthenticationHandler(final Integer clientId, final String secretKey, final YubiKeyAccountRegistry registry) {
        this(clientId, secretKey);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.registry instanceof AcceptAnyYubiKeyAccountRegistry) {
            log.warn("{} instantiated with example accept-any configuration handled via {}. " +
                    "THIS IS NOT OKAY IN PRODUCTION. NO. NO. NO.", this.getClass().getSimpleName(),
                    AcceptAnyYubiKeyAccountRegistry.class.getSimpleName());
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Attempts to authenticate the received credentials using the Yubico cloud validation platform.
     * In this implementation, the {@link org.jasig.cas.authentication.principal.UsernamePasswordCredentials#getUsername()}
     * is mapped to the <code>uid</code> which will be used by the plugged-in instance of the {@link YubiKeyAccountRegistry}
     * and the {@link org.jasig.cas.authentication.principal.UsernamePasswordCredentials#getPassword()} is the received
     * one-time password token issued sby the YubiKey device.
     *
     * @param usernamePasswordCredentials
     *
     * @return true if the authentication succeeds. False, otherwise.
     *
     * @throws AuthenticationException
     */
    @Override
    protected boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials usernamePasswordCredentials) throws AuthenticationException {
        try {
            final String uid = usernamePasswordCredentials.getUsername();
            final String otp = usernamePasswordCredentials.getPassword();

            if (YubicoClient.isValidOTPFormat(otp)) {

                final String publicId = YubicoClient.getPublicId(otp);

                if (this.registry.isYubiKeyRegisteredFor(uid, publicId)) {
                    final YubicoResponse response = client.verify(otp);
                    log.debug("YubiKey response status {} at {}", response.getStatus(), response.getTimestamp());
                    return (response.getStatus() == YubicoResponseStatus.OK);

                }
                else {
                    log.debug("YubiKey public id [{}] is not registered for user [{}]", publicId, uid);
                }
            }
            else {
                log.debug("Invalid OTP format [{}]", otp);
            }
            return false;
        }
        catch (final Exception e) {
            throw new BadUsernameOrPasswordAuthenticationException(e);
        }

    }

    /**
     * Example implementation of YubiKeyAccountRegistry that considers all yubikey Ids
     * registered for all users.
     * THIS IS OBVIOUSLY COMPLETELY UNACCEPTABLE FOR PRODUCTION USE AND YOU MUST USE
     * A REGISTRY THAT ACTUALLY REGISTERS IN PRODUCTION.
     */
    private static final class AcceptAnyYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

        @Override
        public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
            return true;
        }
    }

	@Override
	protected HandlerResult authenticateUsernamePasswordInternal(
			UsernamePasswordCredential transformedCredential)
			throws GeneralSecurityException, PreventedException {
		// TODO Auto-generated method stub
		return null;
	}
}
