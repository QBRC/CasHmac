package edu.swmed.qbrc.auth.cashmac.server.guice;

import com.google.inject.Provider;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;

public class GuiceModuleProvider implements Provider<GuiceModule> {

	private final GuiceModule guiceModule;
	
	public GuiceModuleProvider() {
		guiceModule = new GuiceModule(CasHmacRequestFilter.getConfig(), MainGuiceModule.getModule());
	}
	
	@Override
	public GuiceModule get() {
		return guiceModule;
	}

}
