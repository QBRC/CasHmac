package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Here, we define some event handlers for Hibernate.  Please see also
 * the CasHmacInterceptor class, where we configure "pre" intereceptors
 * for checking ACLs.  The intereceptors defined here are for "post" 
 * events, like inserting ACLs for new objects and deleting old ACLs
 * for deleted objects.
 * 
 * Please note that to enable this custom Integrator, you need to create 
 * a file (on your server) at "META-INF/services" named
 * "org.hibernate.integrator.spi.Integrator" and enter the following
 * single line in the file:
 * 
 *     edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors.CasHmacIntegrator
 * 
 * Be sure that the file referenced above is properly deployed to your
 * server (packaged in the .WAR file), or these event handlers won't be
 * used.
 * 
 * @author JYODE1
 *
 */
public class CasHmacIntegrator implements Integrator {

	@Override
	public void disintegrate(SessionFactoryImplementor arg0, SessionFactoryServiceRegistry arg1) {
	}

	@Override
	public void integrate(Configuration config, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry registry) {
		final EventListenerRegistry eventRegistry = registry.getService(EventListenerRegistry.class);
		// Runs after a new entity is INSERTED.
		eventRegistry.prependListeners(EventType.POST_INSERT, new CasHmacPostInsertListener());
		// Runs after an entity is DELETED.
		eventRegistry.prependListeners(EventType.POST_DELETE, new CasHmacPostDeleteListener());
	}

	@Override
	public void integrate(MetadataImplementor arg0,	SessionFactoryImplementor arg1, SessionFactoryServiceRegistry arg2) {
	}
}
