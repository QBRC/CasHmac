package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class CasHmacIntegrator implements Integrator {

	@Override
	public void disintegrate(SessionFactoryImplementor arg0, SessionFactoryServiceRegistry arg1) {
	}

	@Override
	public void integrate(Configuration config, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry registry) {
		final EventListenerRegistry eventRegistry = registry.getService(EventListenerRegistry.class);
		eventRegistry.prependListeners(EventType.POST_INSERT, new CasHmacPostInsertListener());
		eventRegistry.prependListeners(EventType.POST_DELETE, new CasHmacPostDeleteListener());
	}

	@Override
	public void integrate(MetadataImplementor arg0,	SessionFactoryImplementor arg1, SessionFactoryServiceRegistry arg2) {
	}
}
