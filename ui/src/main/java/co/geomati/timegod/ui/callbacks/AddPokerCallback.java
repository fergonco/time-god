package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class AddPokerCallback extends AbstractLoggingCallback implements
		LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		Poker poker = GSON.fromJson(payload.getAsJsonObject(), Poker.class);
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(poker);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(poker.getName()));

		bus.broadcast("poker-added", GSON.toJsonTree(poker));
	}

	public String getEventName() {
		return "add-poker";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;

		public Memento() {
		}

		public Memento(String name) {
			pokerName = name;
		}

		@Override
		public String toString() {
			return pokerName;
		}
	}
}