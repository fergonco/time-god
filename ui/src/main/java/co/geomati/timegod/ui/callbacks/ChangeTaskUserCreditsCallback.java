package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Estimation;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangeTaskUserCreditsCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(WebsocketBus bus, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		em.getTransaction().begin();
		Estimation estimation = new Estimation();
		String developerName = updateTaskMessage.get("userName").getAsString();
		estimation.setDeveloper(em.find(Developer.class, developerName));
		estimation.setValue(updateTaskMessage.get("credits").getAsInt());
		em.persist(estimation);
		task.getEstimations().add(estimation);
		em.getTransaction().commit();
		bus.broadcast("updated-poker", GSON.toJsonTree(task.getPoker()));
	}
}