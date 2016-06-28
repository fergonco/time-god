package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangePokerTotalCreditsCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		String pokerName = updateTaskMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		em.getTransaction().begin();
		poker.setTotalCredits(updateTaskMessage.get("totalCredits").getAsInt());
		em.getTransaction().commit();

		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}
}