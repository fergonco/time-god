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

public class RemoveTaskCallback extends AbstractCallBack implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		JsonObject removeTaskMessage = payload.getAsJsonObject();
		long taskId = removeTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		Poker poker = task.getPoker();
		em.getTransaction().begin();
		poker.getTasks().remove(task);
		em.remove(task);
		em.getTransaction().commit();
		bus.broadcast("updated-poker", GSON.toJsonTree(task.getPoker()));
	}
}