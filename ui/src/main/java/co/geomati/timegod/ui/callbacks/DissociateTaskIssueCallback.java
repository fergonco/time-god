package co.geomati.timegod.ui.callbacks;

import java.util.Arrays;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DissociateTaskIssueCallback extends AbstractLoggingCallback
		implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) throws CallbackException {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		int issueNumber = updateTaskMessage.get("issueNumber").getAsInt();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);

		em.getTransaction().begin();
		int[] issues = task.getIssues();
		if (issues != null && issues.length > 0) {
			for (int i = 0; i < issues.length; i++) {
				if (issues[i] == issueNumber) {
					for (; i < issues.length - 1; i++) {
						issues[i] = issues[i + 1];
					}
				}
			}
		}
		issues = Arrays.copyOf(issues, issues.length - 1);
		task.setIssues(issues);
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(taskId, task.getName(), issueNumber));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "dissociate-task-issue";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private String taskName;
		private int issueNumber;

		public Memento() {
		}

		public Memento(long taskId, String taskName, int issueNumber) {
			super();
			this.taskId = taskId;
			this.taskName = taskName;
			this.issueNumber = issueNumber;
		}

		@Override
		public String toString() {
			return "Desasociada issue " + issueNumber + " a la tarea "
					+ taskName;
		}
	}
}