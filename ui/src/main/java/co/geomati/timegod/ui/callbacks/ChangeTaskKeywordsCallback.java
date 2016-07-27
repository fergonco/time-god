package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangeTaskKeywordsCallback extends AbstractLoggingCallback
		implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		String[] oldKeywords = task.getKeywords();
		em.getTransaction().begin();
		JsonArray array = updateTaskMessage.get("keywords").getAsJsonArray();
		task.setKeywords(GSON.fromJson(array, String[].class));
		em.getTransaction().commit();

		log(eventName, payload, new Memento(taskId, task.getName(),
				oldKeywords, task.getKeywords()));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "change-task-keywords";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private String taskName;
		private String[] oldKeywords;
		private String[] newKeywords;

		public Memento() {
		}

		public Memento(long taskId, String taskName, String[] oldKeywords,
				String[] newKeywords) {
			super();
			this.taskId = taskId;
			this.taskName = taskName;
			this.oldKeywords = oldKeywords;
			this.newKeywords = newKeywords;
		}

		@Override
		public String toString() {
			return "Cambio de keywords en tarea " + taskName + "\n" //
					+ " de: " + StringUtils.join(oldKeywords, ",") + "\n" //
					+ " a : " + StringUtils.join(newKeywords, ",");
		}
	}
}