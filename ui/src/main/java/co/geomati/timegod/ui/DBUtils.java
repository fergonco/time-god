package co.geomati.timegod.ui;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

public class DBUtils {

	public static String JPA_CONF_NAME = "local-db-pg";

	public static EntityManager getEntityManager() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(JPA_CONF_NAME, Collections
				.singletonMap(PersistenceUnitProperties.SESSION_CUSTOMIZER, SchemaSessionCustomizer.class.getName()));
		EntityManager em = emf.createEntityManager();
		return em;
	}

}
