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
	private JsonArray pokers = new JsonArray();

	public int getArrayIndex(JsonArray array, String name) {
		for (int i = 0; i < array.size(); i++) {
			String testName = array.get(i).getAsJsonObject().get("name")
					.getAsString();
			if (testName.equals(name)) {
				return i;
			}
		}
		assert false;
		return -1;
	}

	public void contextInitialized(ServletContextEvent sce) {
		WebsocketBusHandler.addListener("add-developer", new Callback() {

			public void messageReceived(JsonElement payload) {
				JsonObject developer = payload.getAsJsonObject();
				developers.add(developer);
				WebsocketBusHandler.broadcast("updated-developer-list",
						developers);
			}
		});
		WebsocketBusHandler.addListener("remove-developer", new Callback() {

			public void messageReceived(JsonElement payload) {
				String developerName = payload.getAsString();
				int index = getArrayIndex(developers, developerName);
				developers.remove(index);
				WebsocketBusHandler.broadcast("updated-developer-list",
						developers);
			}
		});
		WebsocketBusHandler.addListener("add-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				JsonObject poker = payload.getAsJsonObject();
				pokers.add(poker);
				WebsocketBusHandler.broadcast("updated-poker-list", pokers);
			}
		});
		WebsocketBusHandler.addListener("remove-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				String pokerName = payload.getAsString();
				int index = getArrayIndex(pokers, pokerName);
				pokers.remove(index);
				WebsocketBusHandler.broadcast("updated-poker-list", pokers);
			}
		});
		WebsocketBusHandler.addListener("add-task-to-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				JsonObject addTaskMessage = payload.getAsJsonObject();
				JsonObject poker = getPokerByName(addTaskMessage.get(
						"pokerName").getAsString());
				JsonArray tasks = poker.get("tasks").getAsJsonArray();
				tasks.add(addTaskMessage.get("task"));
				WebsocketBusHandler.broadcast("updated-poker", poker);
			}

		});
		WebsocketBusHandler.addListener("remove-task-from-poker",
				new Callback() {

					public void messageReceived(JsonElement payload) {
						JsonObject removeTaskMessage = payload
								.getAsJsonObject();
						JsonObject poker = getPokerByName(removeTaskMessage
								.get("pokerName").getAsString());
						JsonArray tasks = poker.get("tasks").getAsJsonArray();
						String taskName = removeTaskMessage.get("taskName")
								.getAsString();
						tasks.remove(getArrayIndex(tasks, taskName));
						WebsocketBusHandler.broadcast("updated-poker", poker);
					}
				});
		WebsocketBusHandler.addListener("change-task-user-credits",
				new Callback() {

					public void messageReceived(JsonElement payload) {
						JsonObject updateTaskMessage = payload
								.getAsJsonObject();
						String userName = updateTaskMessage.get("userName")
								.getAsString();
						JsonObject poker = getPokerByName(updateTaskMessage
								.get("pokerName").getAsString());
						JsonArray tasks = poker.get("tasks").getAsJsonArray();
						String taskName = updateTaskMessage.get("taskName")
								.getAsString();
						JsonObject task = tasks.get(
								getArrayIndex(tasks, taskName))
								.getAsJsonObject();
						JsonElement estimationsElement = task
								.get("estimations");
						estimationsElement = estimationsElement != null ? estimationsElement
								: new JsonObject();
						task.add("estimations", estimationsElement);
						estimationsElement.getAsJsonObject().addProperty(
								userName,
								updateTaskMessage.get("credits").getAsInt());

						WebsocketBusHandler.broadcast("updated-poker", poker);
					}
				});
		WebsocketBusHandler.addListener("change-task-common-credits",
				new Callback() {

					public void messageReceived(JsonElement payload) {
						JsonObject updateTaskMessage = payload
								.getAsJsonObject();
						JsonObject poker = getPokerByName(updateTaskMessage
								.get("pokerName").getAsString());
						JsonArray tasks = poker.get("tasks").getAsJsonArray();
						String taskName = updateTaskMessage.get("taskName")
								.getAsString();
						JsonObject task = tasks.get(
								getArrayIndex(tasks, taskName))
								.getAsJsonObject();
						task.addProperty("commonEstimation", updateTaskMessage
								.get("credits").getAsInt());

						WebsocketBusHandler.broadcast("updated-poker", poker);
					}
				});
		WebsocketBusHandler.addListener("get-developers", new Callback() {

			public void messageReceived(JsonElement payload) {
				WebsocketBusHandler.broadcast("updated-developer-list",
						developers);
			}
		});
		WebsocketBusHandler.addListener("get-pokers", new Callback() {

			public void messageReceived(JsonElement payload) {
				WebsocketBusHandler.broadcast("updated-poker-list", pokers);
			}
		});
		WebsocketBusHandler.addListener("get-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				WebsocketBusHandler.broadcast("updated-poker", pokers
						.get(getArrayIndex(pokers, payload.getAsString())));
			}
		});
	}

	private JsonObject getPokerByName(String pokerName) {
		int index = getArrayIndex(pokers, pokerName);
		JsonObject poker = pokers.get(index).getAsJsonObject();
		return poker;
	}

	public void contextDestroyed(ServletContextEvent sce) {
	}

}
