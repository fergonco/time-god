package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangePokerTotalCreditsCallback extends AbstractLoggingCallback
		implements LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		String pokerName = updateTaskMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		int oldCredits = poker.getTotalCredits();
		em.getTransaction().begin();
		poker.setTotalCredits(updateTaskMessage.get("totalCredits").getAsInt());
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(pokerName, oldCredits, poker.getTotalCredits()));

		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}

	public String getEventName() {
		return "change-poker-totalCredits";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private int oldCredits;
		private int newCredits;

		public Memento() {
		}

		public Memento(String pokerName, int oldCredits, int newCredits) {
			super();
			this.pokerName = pokerName;
			this.oldCredits = oldCredits;
			this.newCredits = newCredits;
		}

		@Override
		public String toString() {
			return "en poker " + pokerName + "\n" //
					+ " de: " + oldCredits + "\n" //
					+ " a : " + newCredits;
		}
	}
}