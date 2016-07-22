package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangeTaskNameCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		String oldName = task.getName();
		em.getTransaction().begin();
		String newName = updateTaskMessage.get("name").getAsString();
		task.setName(newName);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(taskId, oldName, task.getName()));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "change-task-name";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private String oldTaskName;
		private String newTaskName;

		public Memento() {
		}

		public Memento(long taskId, String oldTaskName, String newTaskName) {
			super();
			this.taskId = taskId;
			this.oldTaskName = oldTaskName;
			this.newTaskName = newTaskName;
		}

		@Override
		public String toString() {
			return "Renombrado de la tarea " + oldTaskName + " a : "
					+ newTaskName;
		}
	}
}