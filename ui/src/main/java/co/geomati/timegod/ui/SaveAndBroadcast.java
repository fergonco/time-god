package co.geomati.timegod.ui;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBusHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@WebListener
public class SaveAndBroadcast implements ServletContextListener {

	private JsonArray developers = new JsonArray();

	public void contextInitialized(ServletContextEvent sce) {
		WebsocketBusHandler.addListener("add-developer", new Callback() {

			public void messageReceived(JsonElement payload) {
				JsonObject developer = payload.getAsJsonObject();
				developers.add(developer);
				WebsocketBusHandler.broadcast("updated-developer-list",
						developers);
			}
		});
	}

	public void contextDestroyed(ServletContextEvent sce) {
	}

}
