package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class AddPokerCallback extends AbstractCallBack implements Callback {

	public void messageReceived(WebsocketBus bus, JsonElement payload) {
		Poker poker = GSON.fromJson(payload.getAsJsonObject(), Poker.class);
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(poker);
		em.getTransaction().commit();
		sendPokers(bus);
	}
}