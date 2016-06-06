package co.geomati.timegod.ui.callbacks;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.timegod.ui.TaskMapping;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public abstract class AbstractCallBack implements Callback {

	public static Gson GSON;
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Task.class, new TaskMapping());
		GSON = builder.create();
	}

	<T> void removeEntity(Class<T> clazz, JsonElement payload) {
		EntityManager em = DBUtils.getEntityManager();
		T entity = em.find(clazz, payload.getAsString());
		em.getTransaction().begin();
		em.remove(entity);
		em.getTransaction().commit();

	}

	void sendDevelopers(WebsocketBus bus) {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<Developer> query = em.createQuery(
				"SELECT d FROM Developer d", Developer.class);
		List<Developer> results = query.getResultList();
		bus.broadcast("updated-developer-list", GSON.toJsonTree(results));
	}

	void sendPokers(WebsocketBus bus) {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<Poker> query = em.createQuery("SELECT p FROM Poker p",
				Poker.class);
		List<Poker> results = query.getResultList();
		bus.broadcast("updated-poker-list", GSON.toJsonTree(results));
	}

}
