package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.jpa.TimeSegment;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportTaskTimesCallback extends AbstractCallBack implements
		Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		long timeStart = updateTaskMessage.get("timeStart").getAsLong();
		long timeEnd = updateTaskMessage.get("timeEnd").getAsLong();
		String[] keywords = GSON.fromJson(updateTaskMessage.get("keywords")
				.getAsJsonArray(), String[].class);

		em.getTransaction().begin();
		TimeSegment timeSegment = new TimeSegment();
		timeSegment.setStart(timeStart);
		timeSegment.setEnd(timeEnd);
		timeSegment.setKeywords(keywords);
		em.persist(timeSegment);
		task.getTimeSegments().add(timeSegment);
		em.getTransaction().commit();
		bus.broadcast("updated-task", GSON.toJsonTree(task));
	}
}