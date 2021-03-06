package co.geomati.timegod.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Estimation;
import co.geomati.timegod.jpa.Event;
import co.geomati.timegod.jpa.LogEvent;
import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.jpa.Taxonomy;
import co.geomati.timegod.jpa.TimeSegment;

@WebServlet("/migrate")
public class MigrateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		EntityManagerFactory h2emf = Persistence.createEntityManagerFactory("local-db-h2");
		EntityManager h2em = h2emf.createEntityManager();

		Map<String, String> propertiesMap = Collections.singletonMap(PersistenceUnitProperties.SESSION_CUSTOMIZER,
				SchemaSessionCustomizer.class.getName());
		EntityManagerFactory pgemf = Persistence.createEntityManagerFactory("local-db-pg", propertiesMap);
		EntityManager pgem = pgemf.createEntityManager();

		List<Developer> pgDevelopers = new ArrayList<>();
		try {
			pgem.createQuery("SELECT p FROM Developer p", Developer.class).getResultList();
		} catch (PersistenceException e) {
		}
		if (pgDevelopers.size() > 0) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, "PG database has data");
		} else {
			Class<?>[] entityClasses = new Class[] { Developer.class, Estimation.class, Event.class, LogEvent.class,
					TimeSegment.class, Taxonomy.class, Task.class, Poker.class };
			List<Object> entities = new ArrayList<>();
			for (Class<?> entityClass : entityClasses) {
				List<?> queryResult = h2em
						.createQuery("SELECT p FROM " + entityClass.getSimpleName() + " p", entityClass)
						.getResultList();
				entities.addAll(queryResult);
			}
			pgem.getTransaction().begin();
			for (Object entity : entities) {
				h2em.detach(entity);
				pgem.persist(entity);
			}
			pgem.getTransaction().commit();
			resp.setStatus(200);
		}

	}

}
