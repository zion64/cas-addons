package net.unicon.cas.addons.authentication.principal;

import net.unicon.cas.addons.authentication.principal.util.PrincipalUtils;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * An implementation of the {@link AbstractPersonDirectoryCredentialsToPrincipalResolver} that accepts an email address
 * as the {@link UsernamePasswordCredentials}'s username and resolves it back to the user id.
 * <p>Note: this API is only intended to be called by CAS server code e.g. any custom CAS server overlay extension, etc.</p>
 * 
 * @author <a href="mailto:mmoayyed@unicon.net">Misagh Moayyed</a>
 * @author Unicon, inc.
 * @since 0.6
 */
@SuppressWarnings("deprecation")
public class EmailAddressPasswordCredentialsToPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public boolean supports(final Credentials credential) {
        return credential != null && UsernamePasswordCredentials.class.isAssignableFrom(credential.getClass());
    }

    protected String extractPrincipalId(final Credentials credential) {
        if (credential == null) {
            return null;
		}

        final UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credential;

        if (StringUtils.isBlank(usernamePasswordCredentials.getUsername())) {
            return null;
		}

        return PrincipalUtils.parseNamePartFromEmailAddressIfNecessary(usernamePasswordCredentials.getUsername());
    }
}