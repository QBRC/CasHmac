package edu.swmed.qbrc.auth.cashmac.server.guice;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;

public class MainGuiceModule extends AbstractModule {
	
	public static MainGuiceModule mainGuiceModule = null;
	
	private final Map<Class<? extends Annotation>, Provider<EntityManager>> emMap = new HashMap<Class<? extends Annotation>, Provider<EntityManager>>();
	
	public MainGuiceModule() {
	}
	
	private void addEmp(Provider<EntityManager> p, Class<? extends Annotation> ann) {
		emMap.put(ann, p);
	}
	
	public Map<Class<? extends Annotation>, Provider<EntityManager>> getEntityManagerProviderMap() {
		return emMap; 
	}
	
	public static void addEntityManagerProvider(Provider<EntityManager> p, Class<? extends Annotation> ann) {
		if (mainGuiceModule == null)
			mainGuiceModule = new MainGuiceModule();
		mainGuiceModule.addEmp(p, ann);
	}
	
	public static MainGuiceModule getModule() {
		if (mainGuiceModule == null)
			mainGuiceModule = new MainGuiceModule();
		return mainGuiceModule;
	}
	
	@Override
	protected void configure() {
		bind(GuiceModule.class).toProvider(GuiceModuleProvider.class).in(Singleton.class);
	}
	
}