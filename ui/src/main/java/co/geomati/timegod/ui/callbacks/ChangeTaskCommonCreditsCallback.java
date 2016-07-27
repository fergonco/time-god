package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangeTaskCommonCreditsCallback extends AbstractLoggingCallback
		implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		Integer oldEstimation = task.getCommonEstimation();
		em.getTransaction().begin();
		task.setCommonEstimation(updateTaskMessage.get("credits").getAsInt());
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(taskId, task.getName(), task.getCommonEstimation(),
						oldEstimation));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "change-task-common-credits";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private String taskName;
		private Integer newCredits;
		private Integer oldCredits;

		public Memento() {
		}

		public Memento(long taskId, String taskName, Integer commonEstimation,
				Integer oldCredits) {
			super();
			this.taskId = taskId;
			this.taskName = taskName;
			this.newCredits = commonEstimation;
			this.oldCredits = oldCredits;
		}

		@Override
		public String toString() {
			return "Cambio de estimación común de la tarea " + taskName + "\n" //
					+ " de: " + oldCredits + "\n" //
					+ " a : " + newCredits;
		}
	}
}