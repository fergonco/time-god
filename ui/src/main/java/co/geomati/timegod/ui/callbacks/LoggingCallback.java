package co.geomati.timegod.ui.callbacks;

import java.util.Date;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.LogEvent;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.InternalLogException;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LoggingCallback implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) throws CallbackException,
			InternalLogException {
		JsonObject payloadObject = (JsonObject) payload;

		EntityManager em = DBUtils.getEntityManager();
		LogEvent event = new LogEvent();
		event.setEventName(eventName);
		event.setTimestamp(new Date().getTime());
		Developer developer = em.find(Developer.class,
				payloadObject.get("developerName").getAsString());
		event.setDeveloper(developer);
		event.setPayload(payload.toString());
		em.getTransaction().begin();
		em.persist(event);
		em.getTransaction().commit();
	}

}
