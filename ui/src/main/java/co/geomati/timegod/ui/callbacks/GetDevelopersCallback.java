package co.geomati.timegod.ui.callbacks;

import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class GetDevelopersCallback extends AbstractCallback implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		sendDevelopers(bus);
	}
}