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

public class AddTaskCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject addTaskMessage = payload.getAsJsonObject();
		Task task = GSON.fromJson(addTaskMessage.get("task"), Task.class);
		String pokerName = addTaskMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		task.setPoker(poker);
		em.getTransaction().begin();
		em.persist(task);
		poker.getTasks().add(task);
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(pokerName, task.getName(), task.getId()));

		TaskAddedMessage taskAdded = new TaskAddedMessage(poker.getName(), task);
		bus.broadcast("task-added", GSON.toJsonTree(taskAdded));
	}

	public String getEventName() {
		return "add-task-to-poker";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String taskName;
		private long taskId;

		public Memento() {
		}

		public Memento(String pokerName, String taskName, long taskId) {
			this.pokerName = pokerName;
			this.taskName = taskName;
			this.taskId = taskId;
		}

		@Override
		public String toString() {
			return "Tarea " + taskName + " añadida al poker " + pokerName;
		}
	}
}