package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Estimation;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangeTaskUserCreditsCallback extends AbstractLoggingCallback
		implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
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

		log(eventName, payload,
				new Memento(task.getName(), estimation.getValue()));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "change-task-user-credits";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String taskName;
		private int estimation;

		public Memento() {
		}

		public Memento(String taskName, int estimation) {
			super();
			this.taskName = taskName;
			this.estimation = estimation;
		}

		@Override
		public String toString() {
			return "Estimación para la tarea " + taskName + " a " + estimation;
		}
	}
}