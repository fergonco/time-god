package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangePokerKeywordsCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(WebsocketBus bus, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		String pokerName = updateTaskMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		em.getTransaction().begin();
		JsonArray array = updateTaskMessage.get("keywords").getAsJsonArray();
		poker.setKeywords(GSON.fromJson(array, String[].class));
		em.getTransaction().commit();

		sendPokers(bus);
	}

}