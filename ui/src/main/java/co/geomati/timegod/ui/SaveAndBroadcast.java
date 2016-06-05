package co.geomati.timegod.ui;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Estimation;
import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.jpa.Task;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBusHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@WebListener
public class SaveAndBroadcast implements ServletContextListener {

	private static Gson GSON;
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Task.class, new TaskMapping());
		GSON = builder.create();
	}

	public int getArrayIndex(JsonArray array, String name) {
		for (int i = 0; i < array.size(); i++) {
			String testName = array.get(i).getAsJsonObject().get("name")
					.getAsString();
			if (testName.equals(name)) {
				return i;
			}
		}
		assert false;
		return -1;
	}

	private <T> void removeEntity(Class<T> clazz, JsonElement payload) {
		EntityManager em = DBUtils.getEntityManager();
		T entity = em.find(clazz, payload.getAsString());
		em.getTransaction().begin();
		em.remove(entity);
		em.getTransaction().commit();

	}

	public void contextInitialized(ServletContextEvent sce) {
		WebsocketBusHandler.addListener("add-developer", new Callback() {

			public void messageReceived(JsonElement payload) {
				Developer developer = GSON.fromJson(payload.getAsJsonObject(),
						Developer.class);
				EntityManager em = DBUtils.getEntityManager();
				em.getTransaction().begin();
				em.persist(developer);
				em.getTransaction().commit();
				sendDevelopers();
			}
		});
		WebsocketBusHandler.addListener("remove-developer", new Callback() {

			public void messageReceived(JsonElement payload) {
				removeEntity(Developer.class, payload);
				sendDevelopers();
			}
		});
		WebsocketBusHandler.addListener("add-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				Poker poker = GSON.fromJson(payload.getAsJsonObject(),
						Poker.class);
				EntityManager em = DBUtils.getEntityManager();
				em.getTransaction().begin();
				em.persist(poker);
				em.getTransaction().commit();
				sendPokers();
			}
		});
		WebsocketBusHandler.addListener("remove-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				removeEntity(Poker.class, payload);
				sendPokers();
			}
		});
		WebsocketBusHandler.addListener("add-task-to-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				JsonObject addTaskMessage = payload.getAsJsonObject();
				Task task = GSON.fromJson(addTaskMessage.get("task"),
						Task.class);
				String pokerName = addTaskMessage.get("pokerName")
						.getAsString();
				EntityManager em = DBUtils.getEntityManager();
				Poker poker = em.find(Poker.class, pokerName);
				task.setPoker(poker);
				em.getTransaction().begin();
				em.persist(task);
				poker.getTasks().add(task);
				em.getTransaction().commit();

				WebsocketBusHandler.broadcast("updated-poker",
						GSON.toJsonTree(task.getPoker()));
			}

		});
		WebsocketBusHandler.addListener("remove-task", new Callback() {

			public void messageReceived(JsonElement payload) {
				JsonObject removeTaskMessage = payload.getAsJsonObject();
				String taskName = removeTaskMessage.get("taskName")
						.getAsString();
				EntityManager em = DBUtils.getEntityManager();
				Task task = em.find(Task.class, taskName);
				Poker poker = task.getPoker();
				em.getTransaction().begin();
				poker.getTasks().remove(task);
				em.remove(task);
				em.getTransaction().commit();
				WebsocketBusHandler.broadcast("updated-poker",
						GSON.toJsonTree(task.getPoker()));
			}
		});
		WebsocketBusHandler.addListener("change-task-user-credits",
				new Callback() {

					public void messageReceived(JsonElement payload) {
						JsonObject updateTaskMessage = payload
								.getAsJsonObject();
						String taskName = updateTaskMessage.get("taskName")
								.getAsString();
						EntityManager em = DBUtils.getEntityManager();
						Task task = em.find(Task.class, taskName);
						em.getTransaction().begin();
						Estimation estimation = new Estimation();
						String developerName = updateTaskMessage
								.get("userName").getAsString();
						estimation.setDeveloper(em.find(Developer.class,
								developerName));
						estimation.setValue(updateTaskMessage.get("credits")
								.getAsInt());
						em.persist(estimation);
						task.getEstimations().add(estimation);
						em.getTransaction().commit();
						WebsocketBusHandler.broadcast("updated-poker",
								GSON.toJsonTree(task.getPoker()));
					}
				});
		WebsocketBusHandler.addListener("change-task-common-credits",
				new Callback() {

					public void messageReceived(JsonElement payload) {
						JsonObject updateTaskMessage = payload
								.getAsJsonObject();
						String taskName = updateTaskMessage.get("taskName")
								.getAsString();
						EntityManager em = DBUtils.getEntityManager();
						Task task = em.find(Task.class, taskName);
						em.getTransaction().begin();
						task.setCommonEstimation(updateTaskMessage.get(
								"credits").getAsInt());
						em.getTransaction().commit();
						WebsocketBusHandler.broadcast("updated-poker",
								GSON.toJsonTree(task.getPoker()));
					}
				});
		WebsocketBusHandler.addListener("get-developers", new Callback() {

			public void messageReceived(JsonElement payload) {
				sendDevelopers();
			}
		});
		WebsocketBusHandler.addListener("get-pokers", new Callback() {

			public void messageReceived(JsonElement payload) {
				sendPokers();
			}
		});
		WebsocketBusHandler.addListener("get-poker", new Callback() {

			public void messageReceived(JsonElement payload) {
				EntityManager em = DBUtils.getEntityManager();
				Poker poker = em.find(Poker.class, payload.getAsString());
				WebsocketBusHandler.broadcast("updated-poker",
						GSON.toJsonTree(poker));
			}
		});
	}

	protected void sendDevelopers() {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<Developer> query = em.createQuery(
				"SELECT d FROM Developer d", Developer.class);
		List<Developer> results = query.getResultList();
		WebsocketBusHandler.broadcast("updated-developer-list",
				GSON.toJsonTree(results));
	}

	protected void sendPokers() {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<Poker> query = em.createQuery("SELECT p FROM Poker p",
				Poker.class);
		List<Poker> results = query.getResultList();
		WebsocketBusHandler.broadcast("updated-poker-list",
				GSON.toJsonTree(results));
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// noop
	}

}
