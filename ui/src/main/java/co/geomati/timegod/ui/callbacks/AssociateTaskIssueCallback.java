package co.geomati.timegod.ui.callbacks;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AssociateTaskIssueCallback extends AbstractLoggingCallback
		implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) throws CallbackException {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		int[] issueNumbers = GSON.fromJson(updateTaskMessage
				.get("issueNumbers").getAsJsonArray(), int[].class);
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);

		em.getTransaction().begin();
		int[] issues = task.getIssues() != null ? task.getIssues() : new int[0];
		int newIssueIndex = issues.length;
		issues = Arrays.copyOf(issues, issues.length + issueNumbers.length);
		for (int i = newIssueIndex; i < newIssueIndex + issueNumbers.length; i++) {
			issues[i] = issueNumbers[i - newIssueIndex];
		}
		task.setIssues(issues);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(taskId, task.getName(),
				issueNumbers));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "associate-task-issue";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private String taskName;
		private int[] issueNumbers;

		public Memento() {
		}

		public Memento(long taskId, String taskName, int[] issueNumbers) {
			super();
			this.taskId = taskId;
			this.taskName = taskName;
			this.issueNumbers = issueNumbers;
		}

		@Override
		public String toString() {
			return "Asociadas las issues "
					+ StringUtils.join(Arrays.asList(issueNumbers), ",")
					+ " a la tarea " + taskName;
		}
	}
}