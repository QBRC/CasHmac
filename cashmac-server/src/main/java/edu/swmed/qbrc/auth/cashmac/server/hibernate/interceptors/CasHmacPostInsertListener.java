package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import org.hibernate.event.spi.PostInsertEvent;
import com.google.inject.Guice;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.acl.CrudAclSearchFactory;
import edu.swmed.qbrc.auth.cashmac.server.guice.GuiceModule;
import edu.swmed.qbrc.auth.cashmac.server.guice.MainGuiceModule;

public class CasHmacPostInsertListener implements org.hibernate.event.spi.PostInsertEventListener {

	private static final long serialVersionUID = 653464444067520669L;

	@Inject CrudAclSearchFactory crudAclSearchFactory;
	
	public CasHmacPostInsertListener() {
		GuiceModule guiceModule = Guice.createInjector(MainGuiceModule.getModule()).getInstance(GuiceModule.class);
		Guice.createInjector(guiceModule).injectMembers(this);
	}
	
	@Override
	public void onPostInsert(PostInsertEvent event) {
		crudAclSearchFactory.addAcl(event.getEntity(), event.getId());
	}
	
}
