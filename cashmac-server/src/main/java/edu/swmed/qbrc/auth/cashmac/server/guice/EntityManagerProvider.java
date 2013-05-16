package edu.swmed.qbrc.auth.cashmac.server.guice;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

public class EntityManagerProvider implements Provider<EntityManager> {

	private final EntityManager em;
	
	public EntityManagerProvider(EntityManager em) {
		this.em = em;
	}
	
	@Override
	public EntityManager get() {
		return em;
	}

}
