package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import org.hibernate.event.spi.PostDeleteEvent;
import com.google.inject.Guice;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.acl.CrudAclSearchFactory;
import edu.swmed.qbrc.auth.cashmac.server.guice.GuiceModule;
import edu.swmed.qbrc.auth.cashmac.server.guice.MainGuiceModule;

public class CasHmacPostDeleteListener implements org.hibernate.event.spi.PostDeleteEventListener {

	private static final long serialVersionUID = 7464693798303694010L;
	
	@Inject CrudAclSearchFactory crudAclSearchFactory;
	
	public CasHmacPostDeleteListener() {
		GuiceModule guiceModule = Guice.createInjector(MainGuiceModule.getModule()).getInstance(GuiceModule.class);
		Guice.createInjector(guiceModule).injectMembers(this);
	}
	
	@Override
	public void onPostDelete(PostDeleteEvent event) {
		crudAclSearchFactory.deleteAcls(event.getEntity(), event.getId());
	}
	
}
