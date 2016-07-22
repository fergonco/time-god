package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangePokerKeywordsCallback extends AbstractLoggingCallback
		implements LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		String pokerName = updateTaskMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		String[] oldKeywords = poker.getKeywords();
		em.getTransaction().begin();
		JsonArray array = updateTaskMessage.get("keywords").getAsJsonArray();
		poker.setKeywords(GSON.fromJson(array, String[].class));
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(pokerName, oldKeywords, poker.getKeywords()));

		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}

	public String getEventName() {
		return "change-poker-keywords";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String[] oldKeywords;
		private String[] newKeywords;

		public Memento() {
		}

		public Memento(String pokerName, String[] oldKeywords,
				String[] newKeywords) {
			this.pokerName = pokerName;
			this.oldKeywords = oldKeywords;
			this.newKeywords = newKeywords;
		}

		@Override
		public String toString() {
			return "en poker " + pokerName + "\n" //
					+ " de: " + StringUtils.join(oldKeywords, ",") + "\n" //
					+ " a : " + StringUtils.join(newKeywords, ",");
		}
	}

}