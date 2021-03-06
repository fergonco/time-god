package co.geomati.timegod.ui.callbacks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

public class AddTaskIssueCallback extends AbstractLoggingCallback implements Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus, String eventName, JsonElement payload)
			throws CallbackException {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		String issueName = updateTaskMessage.get("title").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		String repository = updateTaskMessage.get("repository").getAsString();
		String issuesURL = getIssuesURL(repository);
		String user = System.getenv("TIMEGOD_GITHUB_API_USER");
		String password = System.getenv("TIMEGOD_GITHUB_API_PASSWORD");
		String encoding = new String(Base64.encodeBase64((user + ":" + password).getBytes()));
		HttpPost post = new HttpPost(issuesURL);
		post.addHeader("Authorization", "Basic " + encoding);
		String issueString = null;
		try {
			StringEntity requestEntity = new StringEntity("{\"title\":\"" + issueName + "\"}",
					ContentType.APPLICATION_JSON);
			post.setEntity(requestEntity);
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status == 201) {
				InputStream responseStream = response.getEntity().getContent();
				String body = IOUtils.toString(responseStream);
				responseStream.close();
				JsonObject jsonResponse = (JsonObject) new JsonParser().parse(body);
				issueString = repository + jsonResponse.get("number").getAsInt();
			} else {
				throw new CallbackException("Cannot add the issue: " + status);
			}
		} catch (IOException e) {
			throw new CallbackException("Cannot add the issue", e);
		} finally {
			post.releaseConnection();
		}

		em.getTransaction().begin();
		String[] issues = task.getIssues();
		issues = issues != null ? issues : new String[0];
		issues = Arrays.copyOf(issues, issues.length + 1);
		issues[issues.length - 1] = issueString;
		task.setIssues(issues);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(taskId, task.getName(), issueString));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(task.getPoker().getName(), task)));
	}

	private String getIssuesURL(String repo) {
		return "https://api.github.com/repos/" + repo.substring(repo.indexOf('/') + 1) + "issues";
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
		private String issueString;

		public Memento() {
		}

		public Memento(long taskId, String taskName, String issueString) {
			super();
			this.taskId = taskId;
			this.taskName = taskName;
			this.issueString = issueString;
		}

		@Override
		public String toString() {
			return "Añadida issue " + issueString + " a la tarea " + taskName;
		}
	}
}