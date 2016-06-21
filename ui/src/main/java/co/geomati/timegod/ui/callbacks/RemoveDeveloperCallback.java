package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Developer;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class RemoveDeveloperCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		removeEntity(Developer.class, payload);
		sendDevelopers(bus);
	}
}