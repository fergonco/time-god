package co.geomati.timegod.ui.callbacks;

import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class GetPokersCallback extends AbstractCallBack implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		sendPokers(bus);
	}
}