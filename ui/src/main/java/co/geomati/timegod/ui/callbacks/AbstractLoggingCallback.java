package co.geomati.timegod.ui.callbacks;

import java.util.Date;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.LogEvent;
import co.geomati.timegod.ui.DBUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class AbstractLoggingCallback extends AbstractCallback
		implements LoggingCallback {

	protected void log(String eventName, JsonElement payload, Object memento) {
		EntityManager em = DBUtils.getEntityManager();
		LogEvent event = new LogEvent();
		event.setEventName(eventName);
		event.setTimestamp(new Date().getTime());
		Developer developer = em.find(Developer.class, ((JsonObject) payload)
				.get("developerName").getAsString());
		event.setDeveloper(developer);
		event.setPayload(GSON.toJson(memento));
		em.getTransaction().begin();
		em.persist(event);
		em.getTransaction().commit();
	}

	public String eventToString(JsonObject memento) {
		return GSON.fromJson(memento, getMementoClass()).toString();
	}

	protected abstract Class<?> getMementoClass();
}
