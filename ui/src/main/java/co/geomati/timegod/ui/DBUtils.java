package co.geomati.timegod.ui;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DBUtils {

	public static EntityManager getEntityManager() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("local-db");
		EntityManager em = emf.createEntityManager();
		return em;
	}

}
