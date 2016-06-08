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
	public void testAddDeveloper() {
		Callback callback = new AddDeveloperCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("{\"name\" : 'fergonco'}"));

		assertEquals(1,
				((JsonArray) getResponse(bus, "updated-developer-list")).size());
	}

	@Test
	public void testRemoveDeveloper() {
		testAddDeveloper();
		Callback callback = new RemoveDeveloperCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("'fergonco'"));

		assertEquals(0,
				((JsonArray) getResponse(bus, "updated-developer-list")).size());
	}

	@Test
	public void testAddPoker() {
		Callback callback = new AddPokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus,
				parse("{\"name\" : 'fua',\"tasks\" : []}"));

		assertEquals(1,
				((JsonArray) getResponse(bus, "updated-poker-list")).size());
	}

	private JsonElement getResponse(WebsocketBus bus, String messageName) {
		ArgumentCaptor<JsonElement> captor = ArgumentCaptor
				.forClass(JsonElement.class);
		verify(bus).broadcast(eq(messageName), captor.capture());
		return captor.getValue();
	}

	@Test
	public void testRemovePoker() {
		testAddPoker();
		Callback callback = new RemovePokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("'fua'"));

		assertEquals(0,
				((JsonArray) getResponse(bus, "updated-poker-list")).size());
	}

	@Test
	public void testAddTask() {
		testAddPoker();
		Callback callback = new AddTaskCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus,
				parse("{\"pokerName\":\"fua\", task:{\"name\" : \"t1\","
						+ "\"estimations\" : {},"
						+ "\"commonEstimation\" : null}}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(1, poker.get("tasks").getAsJsonArray().size());
	}

	@Test
	public void testRemoveTask() {
		testAddTask();
		Callback callback = new RemoveTaskCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("{\"taskName\":\"t1\"}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(0, poker.get("tasks").getAsJsonArray().size());
	}

	@Test
	public void testUpdateTaskUserCredits() {
		testAddDeveloper();
		testAddTask();
		Callback callback = new ChangeTaskUserCreditsCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("{\"userName\" : \"fergonco\","
				+ "\"taskName\" : \"t1\"," + "\"credits\" : 12}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(12,
				poker.get("tasks").getAsJsonArray().get(0).getAsJsonObject()
						.get("estimations").getAsJsonObject().get("fergonco")
						.getAsInt());
	}

	@Test
	public void testUpdateTaskCommonCredits() {
		testAddDeveloper();
		testAddTask();
		Callback callback = new ChangeTaskCommonCreditsCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("{\"taskName\" : \"t1\","
				+ "\"credits\" : 22}"));

		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals(22, poker.get("tasks").getAsJsonArray().get(0)
				.getAsJsonObject().get("commonEstimation").getAsInt());
	}

	@Test
	public void testGetDevelopers() {
		testAddDeveloper();
		Callback callback = new GetDevelopersCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, null);
		JsonArray list = (JsonArray) getResponse(bus, "updated-developer-list");
		assertEquals(1, list.size());
	}

	@Test
	public void testGetPokers() {
		testAddPoker();
		Callback callback = new GetPokersCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, null);
		JsonArray list = (JsonArray) getResponse(bus, "updated-poker-list");
		assertEquals(1, list.size());
	}

	@Test
	public void testGetPoker() {
		testAddPoker();
		Callback callback = new GetPokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(bus, parse("\"fua\""));
		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals("fua", poker.get("name").getAsString());
	}

	private JsonElement parse(String json) {
		System.out.println(json);
		return new JsonParser().parse(json);
	}
}
