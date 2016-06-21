package co.geomati.timegod.ui;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import co.geomati.timegod.ui.callbacks.AddDeveloperCallback;
import co.geomati.timegod.ui.callbacks.AddPokerCallback;
import co.geomati.timegod.ui.callbacks.AddTaskCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskCommonCreditsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskUserCreditsCallback;
import co.geomati.timegod.ui.callbacks.GetDevelopersCallback;
import co.geomati.timegod.ui.callbacks.GetPokerCallback;
import co.geomati.timegod.ui.callbacks.GetPokersCallback;
import co.geomati.timegod.ui.callbacks.RemoveDeveloperCallback;
import co.geomati.timegod.ui.callbacks.RemovePokerCallback;
import co.geomati.timegod.ui.callbacks.RemoveTaskCallback;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SaveAndBroadcastTest {

	@BeforeClass
	public static void setup() {
		DBUtils.JPA_CONF_NAME = "testing";
	}

	private JsonElement lastResponse;

	@After
	public void testTearDown() {
		EntityManager manager = DBUtils.getEntityManager();
		manager.getTransaction().begin();
		manager.createQuery("DELETE FROM Task").executeUpdate();
		manager.createQuery("DELETE FROM Estimation").executeUpdate();
		manager.createQuery("DELETE FROM Developer").executeUpdate();
		manager.createQuery("DELETE FROM Poker").executeUpdate();
		manager.getTransaction().commit();
	}

	@Test
	public void testAddDeveloper() throws CallbackException {
		Callback callback = new AddDeveloperCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus,
				parse("{\"name\" : 'fergonco'}"));

		assertEquals(1,
				((JsonArray) getResponse(bus, "updated-developer-list")).size());
	}

	@Test
	public void testRemoveDeveloper() throws CallbackException {
		testAddDeveloper();
		Callback callback = new RemoveDeveloperCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, parse("'fergonco'"));

		assertEquals(0,
				((JsonArray) getResponse(bus, "updated-developer-list")).size());
	}

	@Test
	public void testAddPoker() throws CallbackException {
		Callback callback = new AddPokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus,
				parse("{\"name\" : 'fua',\"tasks\" : []}"));

		assertEquals(1,
				((JsonArray) getResponse(bus, "updated-poker-list")).size());
	}

	private JsonElement getResponse(WebsocketBus bus, String messageName) {
		ArgumentCaptor<JsonElement> captor = ArgumentCaptor
				.forClass(JsonElement.class);
		verify(bus).broadcast(eq(messageName), captor.capture());
		lastResponse = captor.getValue();
		return captor.getValue();
	}

	@Test
	public void testRemovePoker() throws CallbackException {
		testAddPoker();
		Callback callback = new RemovePokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, parse("'fua'"));

		assertEquals(0,
				((JsonArray) getResponse(bus, "updated-poker-list")).size());
	}

	@Test
	public void testAddTask() throws CallbackException {
		testAddPoker();
		Callback callback = new AddTaskCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus,
				parse("{\"pokerName\":\"fua\", task:{\"name\" : \"t1\","
						+ "\"estimations\" : {},"
						+ "\"creationTime\" : 82734628,"
						+ "\"commonEstimation\" : null}}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(1, poker.get("tasks").getAsJsonArray().size());
	}

	@Test
	public void testRemoveTask() throws CallbackException {
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("tasks").getAsJsonArray()
				.get(0).getAsJsonObject().get("id").getAsLong();
		Callback callback = new RemoveTaskCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus,
				parse("{\"taskId\":\"" + taskId + "\"}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(0, poker.get("tasks").getAsJsonArray().size());
	}

	@Test
	public void testUpdateTaskUserCredits() throws CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("tasks").getAsJsonArray()
				.get(0).getAsJsonObject().get("id").getAsLong();
		Callback callback = new ChangeTaskUserCreditsCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus,
				parse("{\"userName\" : \"fergonco\"," + "\"taskId\" : \""
						+ taskId + "\"," + "\"credits\" : 12}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(12,
				poker.get("tasks").getAsJsonArray().get(0).getAsJsonObject()
						.get("estimations").getAsJsonObject().get("fergonco")
						.getAsInt());
	}

	@Test
	public void testUpdateTaskCommonCredits() throws CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("tasks").getAsJsonArray()
				.get(0).getAsJsonObject().get("id").getAsLong();
		Callback callback = new ChangeTaskCommonCreditsCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(
				mock(Caller.class),
				bus,
				parse("{\"taskId\" : \"" + taskId + "\"," + "\"credits\" : 22}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(22, poker.get("tasks").getAsJsonArray().get(0)
				.getAsJsonObject().get("commonEstimation").getAsInt());
	}

	@Test
	public void testGetDevelopers() throws CallbackException {
		testAddDeveloper();
		Callback callback = new GetDevelopersCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, null);
		JsonArray list = (JsonArray) getResponse(bus, "updated-developer-list");
		assertEquals(1, list.size());
	}

	@Test
	public void testGetPokers() throws CallbackException {
		testAddPoker();
		Callback callback = new GetPokersCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, null);
		JsonArray list = (JsonArray) getResponse(bus, "updated-poker-list");
		assertEquals(1, list.size());
	}

	@Test
	public void testGetPoker() throws CallbackException {
		testAddPoker();
		Callback callback = new GetPokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, parse("\"fua\""));
		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals("fua", poker.get("name").getAsString());
	}

	private JsonElement parse(String json) {
		System.out.println(json);
		return new JsonParser().parse(json);
	}
}
