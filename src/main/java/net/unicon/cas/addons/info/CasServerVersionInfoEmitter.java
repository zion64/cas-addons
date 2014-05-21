package net.unicon.cas.addons.info;

import java.util.concurrent.atomic.AtomicInteger;

import net.unicon.cas.addons.support.ThreadSafe;

import org.jasig.cas.CasVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Outputs CAS version number to the configurred logger at the Spring Apllication Context refresh time.
 *
 * @author Dmitriy Kopylenko
 * @author Unicon, inc.
 * @since 1.0.1
 */
@ThreadSafe
@Component
public final class CasServerVersionInfoEmitter implements ApplicationListener<ContextRefreshedEvent> {

	private static Logger logger = LoggerFactory.getLogger(CasServerVersionInfoEmitter.class);

	/**
	 * The ContextRefreshEvent could happen several times in the application context. We are only interested to emit the version info
	 * during the first refresh
	 */
	private AtomicInteger numberOfRefreshes = new AtomicInteger(0);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (this.numberOfRefreshes.compareAndSet(0, 1)) {
			logger.info("=======| WELCOME TO CAS VERSION [{}] |=======", CasVersion.getVersion());
		}
	}
}
