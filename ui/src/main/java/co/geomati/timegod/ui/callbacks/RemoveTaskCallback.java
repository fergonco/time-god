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

public class RemoveTaskCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject removeTaskMessage = payload.getAsJsonObject();
		long taskId = removeTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		Poker poker = task.getPoker();
		em.getTransaction().begin();
		poker.getTasks().remove(task);
		em.remove(task);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(poker.getName(), task.getName()));

		bus.broadcast("task-removed", GSON.toJsonTree(new TaskRemovedMessage(
				poker.getName(), taskId)));
	}

	public String getEventName() {
		return "remove-task";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String taskName;

		public Memento() {
		}

		public Memento(String pokerName, String taskName) {
			super();
			this.pokerName = pokerName;
			this.taskName = taskName;
		}

		@Override
		public String toString() {
			return "Eliminada la tarea " + taskName + " de " + pokerName;
		}
	}

}