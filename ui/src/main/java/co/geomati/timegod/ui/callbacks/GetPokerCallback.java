package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class GetPokerCallback extends AbstractCallback implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, payload.getAsString());
		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}

}
