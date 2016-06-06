package co.geomati.timegod.ui.callbacks;

import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class GetDevelopersCallback extends AbstractCallBack implements Callback {

	public void messageReceived(WebsocketBus bus, JsonElement payload) {
		sendDevelopers(bus);
	}
}