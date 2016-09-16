package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

public class ToggleTaskStatusCallback extends AbstractLoggingCallback implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus, String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		int status = task.getStatus();
		em.getTransaction().begin();
		task.setStatus((status + 1) % 3);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(taskId, task.getStatus()));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "toggle-task-status";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private int status;

		public Memento() {
		}

		public Memento(long taskId, int status) {
			super();
			this.taskId = taskId;
			this.status = status;
		}

		@Override
		public String toString() {
			return "Estado de la tarea cambió a " + status + "(0=abierta, 1=cerrada, 2=cancelada)";

		}
	}
}