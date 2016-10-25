package co.geomati.timegod.ui.callbacks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.jpa.Taxonomy;
import co.geomati.timegod.jpa.TimeSegment;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReportTaskTimesCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public static final String EVENT_NAME = "report-task-time";

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) throws CallbackException {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = getOrThrow(updateTaskMessage, "taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		if (task == null) {
			throw new CallbackException("Task not found: " + taskId);
		}
		long timeStart = getOrThrow(updateTaskMessage, "timeStart").getAsLong();
		long timeEnd = getOrThrow(updateTaskMessage, "timeEnd").getAsLong();
		if (timeEnd < timeStart) {
			throw new CallbackException("End before start: " + timeStart + ">"
					+ timeEnd);
		}
		String[] keywords = GSON.fromJson(
				getOrThrow(updateTaskMessage, "keywords").getAsJsonArray(),
				String[].class);
		checkKeywords(keywords);
		String developerName = getOrThrow(updateTaskMessage, "developerName")
				.getAsString();
		Developer developer = em.find(Developer.class, developerName);
		if (developer == null) {
			throw new CallbackException("Developer not found: " + developerName);
		}

		em.getTransaction().begin();
		TimeSegment timeSegment = new TimeSegment();
		timeSegment.setStart(timeStart);
		timeSegment.setEnd(timeEnd);
		timeSegment.setKeywords(keywords);
		timeSegment.setDeveloper(developer);
		em.persist(timeSegment);
		task.getTimeSegments().add(timeSegment);
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(task.getPoker().getName(), task.getName(),
						(int) (timeEnd - timeStart)));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	private JsonElement getOrThrow(JsonObject updateTaskMessage,
			String propertyName) throws CallbackException {
		JsonElement ret = updateTaskMessage.get(propertyName);
		if (ret == null) {
			throw new CallbackException("Missing property: " + propertyName);
		}
		return ret;
	}

	public String getEventName() {
		return EVENT_NAME;
	}

	private void checkKeywords(String[] keywords) throws CallbackException {
		EntityManager em = DBUtils.getEntityManager();
		String taxonomyContent = em.find(Taxonomy.class, "time").getContent();
		JsonObject taxonomy = (JsonObject) new JsonParser()
				.parse(taxonomyContent);
		List<String> keywordList = new ArrayList<String>();
		Collections.addAll(keywordList, keywords);
		process(taxonomy, keywordList);
		if (keywordList.size() > 0) {
			throw new CallbackException(
					"Keyword does not belong to a category: "
							+ keywordList.get(0));
		}
	}

	private void process(JsonObject taxonomyNode, List<String> keywords)
			throws CallbackException {
		String type = taxonomyNode.get("type").getAsString();
		if ("sequence".equals(type)) {
			JsonArray children = taxonomyNode.get("children").getAsJsonArray();
			for (int i = 0; i < children.size(); i++) {
				process((JsonObject) children.get(i), keywords);
			}
		} else if ("choice".equals(type)) {
			JsonArray children = taxonomyNode.get("children").getAsJsonArray();
			boolean found = false;
			for (int i = 0; i < children.size(); i++) {
				String keyword = ((JsonObject) children.get(i)).get("name")
						.getAsString();
				if (keywords.contains(keyword)) {
					if (found) {
						throw new CallbackException(
								"two keywords from same category: "
										+ taxonomyNode.get("name")
												.getAsString());
					} else {
						found = true;
						keywords.remove(keyword);
					}
				}
			}
			if (!found) {
				throw new CallbackException("no keyword from the category: "
						+ taxonomyNode.get("name").getAsString());
			}
		}
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String taskName;
		private int total;

		public Memento() {
		}

		public Memento(String pokerName, String taskName, int total) {
			super();
			this.pokerName = pokerName;
			this.taskName = taskName;
			this.total = total;
		}

		@Override
		public String toString() {
			return "Reportó " + (total / (60 * 60 * 1000.0))
					+ "h para la tarea " + taskName + " de " + pokerName;
		}
	}
}