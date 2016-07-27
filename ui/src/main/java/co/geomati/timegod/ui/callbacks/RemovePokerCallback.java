package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Poker;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RemovePokerCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject payloadObj = (JsonObject) payload;
		String name = removeEntity(Poker.class, payloadObj.get("pokerName"));

		log(eventName, payloadObj, new Memento(name));

		bus.broadcast("poker-removed", GSON.toJsonTree(name));
	}

	public String getEventName() {
		return "remove-poker";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;

		public Memento() {
		}

		public Memento(String pokerName) {
			super();
			this.pokerName = pokerName;
		}

		@Override
		public String toString() {
			return "Eliminado " + pokerName;
		}
	}
}