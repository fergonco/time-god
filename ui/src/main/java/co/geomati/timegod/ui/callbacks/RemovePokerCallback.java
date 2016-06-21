package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Poker;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class RemovePokerCallback extends AbstractCallBack implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		removeEntity(Poker.class, payload);
		sendPokers(bus);
	}
}