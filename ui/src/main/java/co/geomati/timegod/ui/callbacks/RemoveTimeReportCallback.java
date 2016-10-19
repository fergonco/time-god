package co.geomati.timegod.ui.callbacks;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.jpa.TimeSegment;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

public class RemoveTimeReportCallback extends AbstractLoggingCallback implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus, String eventName, JsonElement payload)
			throws CallbackException {
		JsonObject removeReportMessage = payload.getAsJsonObject();
		long reportId = removeReportMessage.get("id").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		List<Task> tasks = em.createQuery("SELECT t FROM Task t", Task.class).getResultList();
		boolean removed = false;
		for (Task task : tasks) {
			ArrayList<TimeSegment> segments = task.getTimeSegments();
			for (TimeSegment segment : segments) {
				if (segment.getId() == reportId) {
					em.getTransaction().begin();
					task.getTimeSegments().remove(segment);
					em.remove(segment);
					em.getTransaction().commit();

					log(eventName, payload, new Memento(task.getName(), segment.getId()));

					bus.broadcast("updated-task",
							GSON.toJsonTree(new TaskUpdatedMessage(task.getPoker().getName(), task)));

					removed = true;
					break;
				}
			}
		}

		if (!removed) {
			caller.sendError("could not find time segment: " + reportId);
		}
	}

	public String getEventName() {
		return "remove-time-report";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String taskName;
		private long segmentId;

		public Memento() {
		}

		public Memento(String taskName, long segmentId) {
			super();
			this.taskName = taskName;
			this.segmentId = segmentId;
		}

		@Override
		public String toString() {
			return "Eliminación de reporte de tiempo en tarea " + taskName + " = " + segmentId;
		}
	}
}