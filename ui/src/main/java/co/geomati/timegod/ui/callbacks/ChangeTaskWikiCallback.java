package co.geomati.timegod.ui.callbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import difflib.DiffUtils;
import difflib.Patch;

public class ChangeTaskWikiCallback extends AbstractLoggingCallback implements
		Callback, LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updateTaskMessage = payload.getAsJsonObject();
		long taskId = updateTaskMessage.get("taskId").getAsLong();
		EntityManager em = DBUtils.getEntityManager();
		Task task = em.find(Task.class, taskId);
		String oldWiki = task.getWiki();
		em.getTransaction().begin();
		task.setWiki(updateTaskMessage.get("wiki").getAsString());
		em.getTransaction().commit();

		log(eventName, payload, new Memento(taskId, task.getName(), oldWiki,
				task.getWiki()));

		bus.broadcast("updated-task", GSON.toJsonTree(new TaskUpdatedMessage(
				task.getPoker().getName(), task)));
	}

	public String getEventName() {
		return "change-task-wiki";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private long taskId;
		private String taskName;
		private String diff;

		public Memento() {
		}

		public Memento(long taskId, String taskName, String oldWiki,
				String newWiki) {
			super();
			this.taskId = taskId;
			this.taskName = taskName;
			ArrayList<String> oldWikiLines = toLines(oldWiki);
			ArrayList<String> newWikiLines = toLines(newWiki);
			Patch<String> patch = DiffUtils.diff(oldWikiLines, newWikiLines);
			List<String> unifiedDiff = DiffUtils.generateUnifiedDiff(
					"previous", "new", oldWikiLines, patch, 10);
			diff = StringUtils.join(unifiedDiff, "\n");
		}

		private ArrayList<String> toLines(String oldWiki) {
			String oldWiki2 = oldWiki;
			ArrayList<String> oldWikiLines = new ArrayList<String>();
			Collections.addAll(oldWikiLines,
					oldWiki2.split(Pattern.quote("\n")));
			return oldWikiLines;
		}

		@Override
		public String toString() {
			return "Se cambió la wiki de " + taskName + " a\n" + diff;
		}
	}

}