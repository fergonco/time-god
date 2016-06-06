package co.geomati.timegod.ui;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DBUtils {

	public static String JPA_CONF_NAME = "local-db";

	public static EntityManager getEntityManager() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory(JPA_CONF_NAME);
		EntityManager em = emf.createEntityManager();
		return em;
	}

}
