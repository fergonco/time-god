package co.geomati.timegod.ui;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

public class SchemaSessionCustomizer implements SessionCustomizer {

	@Override
	public void customize(Session session) throws Exception {
		session.getLogin().setTableQualifier("app");
	}

}