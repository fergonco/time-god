package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangeTaskCommonCreditsCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(WebsocketBus bus, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		String taskName = updateTaskMessage.get("taskName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskName);
		em.getTransaction().begin();
		task.setCommonEstimation(updateTaskMessage.get("credits").getAsInt());
		em.getTransaction().commit();
		bus.broadcast("updated-poker", GSON.toJsonTree(task.getPoker()));
	}
}