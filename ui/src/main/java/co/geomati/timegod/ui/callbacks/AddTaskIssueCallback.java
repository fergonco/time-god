package co.geomati.timegod.ui.callbacks;

import java.io.IOException;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AddTaskIssueCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) throws CallbackException {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		String issueName = updateTaskMessage.get("title").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		String issueRepository = task.getPoker().getAPIRepository() + "issues";
		String encoding = new String(
				Base64.encodeBase64("fergonco:desarrollista80".getBytes()));
		PostMethod post = new PostMethod(issueRepository);
		post.addRequestHeader("Authorization", "Basic " + encoding);
		int issueNumber = -1;
		try {
			RequestEntity requestEntity = new StringRequestEntity(
					"{\"title\":\"" + issueName + "\"}", "application/json",
					"utf-8");
			post.setRequestEntity(requestEntity);
			HttpClient client = new HttpClient();
			int status = client.executeMethod(post);
			if (status == 201) {
				String response = post.getResponseBodyAsString();
				JsonObject jsonResponse = (JsonObject) new JsonParser()
						.parse(response);
				issueNumber = jsonResponse.get("number").getAsInt();
			} else {
				throw new CallbackException("Cannot add the issue: " + status);
			}
		} catch (HttpException e) {
			throw new CallbackException("Cannot add the issue", e);
		} catch (IOException e) {
			throw new CallbackException("Cannot add the issue", e);
		} finally {
			post.releaseConnection();
		}

		em.getTransaction().begin();
		int[] issues = task.getIssues();
		issues = issues != null ? issues : new int[0];
		issues = Arrays.copyOf(issues, issues.length + 1);
		issues[issues.length - 1] = issueNumber;
		task.setIssues(issues);
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(taskId, task.getName(), issueNumber));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "add-task-issue";
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
			return "Añadida issue " + issueNumber + " a la tarea " + taskName;
		}
	}
}