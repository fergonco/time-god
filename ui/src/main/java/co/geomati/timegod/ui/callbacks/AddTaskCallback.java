package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AddTaskCallback extends AbstractCallBack implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject addTaskMessage = payload.getAsJsonObject();
		Task task = GSON.fromJson(addTaskMessage.get("task"), Task.class);
		String pokerName = addTaskMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		task.setPoker(poker);
		em.getTransaction().begin();
		em.persist(task);
		poker.getTasks().add(task);
		em.getTransaction().commit();

		TaskAddedMessage taskAdded = new TaskAddedMessage(poker.getName(), task);

		bus.broadcast("task-added", GSON.toJsonTree(taskAdded));
	}
}