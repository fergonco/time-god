package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import co.geomati.timegod.jpa.Event;
import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SetPokerEventCallback extends AbstractLoggingCallback implements
		LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		String pokerName = updateTaskMessage.get("pokerName").getAsString();
		long timestamp = updateTaskMessage.get("timestamp").getAsLong();
		String[] keywords = GSON.fromJson(updateTaskMessage.get("keywords")
				.getAsJsonArray(), String[].class);
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		em.getTransaction().begin();
		Event event = new Event();
		event.setTimestamp(timestamp);
		event.setKeywords(keywords);
		em.persist(event);
		poker.getEvents().add(event);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(pokerName, keywords));

		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}

	public String getEventName() {
		return "add-poker-event";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String[] keywords;

		public Memento() {
		}

		public Memento(String pokerName, String[] keywords) {
			super();
			this.pokerName = pokerName;
			this.keywords = keywords;
		}

		@Override
		public String toString() {
			return "en poker " + pokerName + ", evento "
					+ StringUtils.join(keywords, ",");
		}
	}
}