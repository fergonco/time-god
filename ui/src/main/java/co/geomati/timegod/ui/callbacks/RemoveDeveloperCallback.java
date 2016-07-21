package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Developer;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class RemoveDeveloperCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		String name = removeEntity(Developer.class, payload);
		bus.broadcast("developer-removed", GSON.toJsonTree(name));
	}
}