package edu.swmed.qbrc.auth.cashmac.server.guice;

import com.google.inject.Guice;
import com.google.inject.Provider;
import edu.swmed.qbrc.auth.cashmac.server.acl.CrudAclSearchFactory;
import edu.swmed.qbrc.auth.cashmac.server.acl.utils.CasHmacValidation;

public class CasHmacValidationProvider implements Provider<CasHmacValidation> {

	@Override
	public CasHmacValidation get() {
		// Inject CasHmac's CasHmacValidation Utility Class
		GuiceModule guiceModule = Guice.createInjector(MainGuiceModule.getModule()).getInstance(GuiceModule.class);
		return Guice.createInjector(guiceModule).getInstance(CrudAclSearchFactory.class).getCasHmacValidation();
	}
	
}