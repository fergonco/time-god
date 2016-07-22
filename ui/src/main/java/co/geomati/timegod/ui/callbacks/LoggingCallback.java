package co.geomati.timegod.ui.callbacks;

import co.geomati.websocketBus.Callback;

import com.google.gson.JsonObject;

public interface LoggingCallback extends Callback {

	String getEventName();

	String eventToString(JsonObject memento);

}
