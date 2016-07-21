package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Poker;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RemovePokerCallback extends AbstractCallBack implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject payloadObj = (JsonObject) payload;
		String name = removeEntity(Poker.class, payloadObj.get("pokerName"));
		bus.broadcast("poker-removed", GSON.toJsonTree(name));
	}
}