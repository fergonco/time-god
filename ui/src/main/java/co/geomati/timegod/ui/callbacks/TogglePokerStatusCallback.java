package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

public class TogglePokerStatusCallback extends AbstractLoggingCallback implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus, String eventName, JsonElement payload) {
		JsonObject updatePokerMessage = payload.getAsJsonObject();
		String pokerName = updatePokerMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		int status = poker.getStatus();
		em.getTransaction().begin();
		poker.setStatus((status + 1) % 2);
		em.getTransaction().commit();
		log(eventName, payload, new Memento(pokerName, poker.getStatus()));

		// update poker since order may change
		sendPokers(bus);
	}

	public String getEventName() {
		return "toggle-poker-status";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private int status;

		public Memento() {
		}

		public Memento(String pokerName, int status) {
			super();
			this.pokerName = pokerName;
			this.status = status;
		}

		@Override
		public String toString() {
			return "Estado del poker " + pokerName + " cambió a " + status + "(0=abierta, 1=cerrada)";
		}
	}
}