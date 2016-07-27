package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.jpa.TimeSegment;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportTaskTimesCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public static final String EVENT_NAME = "report-task-time";

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		long timeStart = updateTaskMessage.get("timeStart").getAsLong();
		long timeEnd = updateTaskMessage.get("timeEnd").getAsLong();
		String[] keywords = GSON.fromJson(updateTaskMessage.get("keywords")
				.getAsJsonArray(), String[].class);
		String developerName = updateTaskMessage.get("developerName")
				.getAsString();
		Developer developer = em.find(Developer.class, developerName);

		em.getTransaction().begin();
		TimeSegment timeSegment = new TimeSegment();
		timeSegment.setStart(timeStart);
		timeSegment.setEnd(timeEnd);
		timeSegment.setKeywords(keywords);
		timeSegment.setDeveloper(developer);
		em.persist(timeSegment);
		task.getTimeSegments().add(timeSegment);
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(task.getPoker().getName(), task.getName(),
						(int) (timeEnd - timeStart)));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return EVENT_NAME;
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String taskName;
		private int total;

		public Memento() {
		}

		public Memento(String pokerName, String taskName, int total) {
			super();
			this.pokerName = pokerName;
			this.taskName = taskName;
			this.total = total;
		}

		@Override
		public String toString() {
			return "Reportó " + (total / (60 * 60 * 1000.0))
					+ "h para la tarea " + taskName + " de " + pokerName;
		}
	}
}