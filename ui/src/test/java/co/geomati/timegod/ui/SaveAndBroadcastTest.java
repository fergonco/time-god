package co.geomati.timegod.ui;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		manager.createQuery("DELETE FROM TimeSegment").executeUpdate();
		manager.createQuery("DELETE FROM LogEvent").executeUpdate();
		manager.createQuery("DELETE FROM Developer").executeUpdate();
		manager.createQuery("DELETE FROM Poker").executeUpdate();
		manager.getTransaction().commit();
	}

	@Test
	public void testAddDeveloper() throws CallbackException {
		Callback callback = new AddDeveloperCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		JsonElement developerMessage = parse("{\"name\" : 'fergonco'}");
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				developerMessage);

		assertEquals(developerMessage, getResponse(bus, "developer-added"));
	}

	@Test
	public void testRemoveDeveloper() throws CallbackException {
		testAddDeveloper();
		Callback callback = new RemoveDeveloperCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		JsonElement developerId = parse("'fergonco'");
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				developerId);

		assertEquals(developerId, getResponse(bus, "developer-removed"));
	}

	@Test
	public void testAddPoker() throws CallbackException {
		Callback callback = new AddPokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		JsonObject pokerMessage = (JsonObject) parse("{\"name\" : 'fua',\"developerName\" : 'pumukl',\"tasks\" : []}");
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				pokerMessage);

		JsonObject broadcastedPoker = (JsonObject) getResponse(bus,
				"poker-added");
		assertEquals(pokerMessage.get("name"), broadcastedPoker.get("name"));
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
		JsonObject pokerMessage = (JsonObject) parse("{\"pokerName\" : 'fua',\"developerName\" : 'pumukl'}");
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				pokerMessage);

		assertEquals(pokerMessage.get("pokerName"),
				getResponse(bus, "poker-removed"));
	}

	@Test
	public void testAddTask() throws CallbackException {
		testAddPoker();
		Callback callback = new AddTaskCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		JsonObject taskMessage = (JsonObject) parse("{\"pokerName\":\"fua\", \"developerName\" : 'pumukl', task:{\"name\" : \"t1\","
				+ "\"estimations\" : {},"
				+ "\"creationTime\" : 82734628,"
				+ "\"status\" : 0,"
				+ "\"commonEstimation\" : null}}");
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				taskMessage);

		JsonObject task = (JsonObject) getResponse(bus, "task-added");
		assertEquals(taskMessage.get("name"), task.get("name"));
		assertEquals(taskMessage.get("estimations"), task.get("estimations"));
		assertEquals(taskMessage.get("creationTime"), task.get("creationTime"));
	}

	@Test
	public void testRemoveTask() throws CallbackException {
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		Callback callback = new RemoveTaskCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				parse("{\"taskId\":\"" + taskId
						+ "\", \"developerName\" : 'pumukl'}"));

		assertEquals(taskId, ((JsonObject) getResponse(bus, "task-removed"))
				.get("taskId").getAsLong());
	}

	@Test
	public void testUpdateTaskUserCredits() throws CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		Callback callback = new ChangeTaskUserCreditsCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				parse("{\"userName\" : \"fergonco\"," + "\"taskId\" : \""
						+ taskId + "\","
						+ "\"credits\" : 12, \"developerName\" : 'pumukl'}"));

		JsonObject taskResponse = (JsonObject) getResponse(bus, "updated-task");
		assertEquals(12,
				taskResponse.get("task").getAsJsonObject().get("estimations")
						.getAsJsonObject().get("fergonco").getAsInt());
	}

	@Test
	public void testUpdateTaskCommonCredits() throws CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		Callback callback = new ChangeTaskCommonCreditsCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				parse("{\"taskId\" : \"" + taskId + "\","
						+ "\"credits\" : 22, \"developerName\" : 'pumukl'}"));

		JsonObject taskResponse = (JsonObject) getResponse(bus, "updated-task");
		assertEquals(
				22,
				taskResponse.get("task").getAsJsonObject()
						.get("commonEstimation").getAsInt());
	}

	@Test
	public void testGetDevelopers() throws CallbackException {
		testAddDeveloper();
		Callback callback = new GetDevelopersCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, "eventName", null);
		JsonArray list = (JsonArray) getResponse(bus, "updated-developer-list");
		assertEquals(1, list.size());
	}

	@Test
	public void testGetPokers() throws CallbackException {
		testAddPoker();
		Callback callback = new GetPokersCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, "eventName", null);
		JsonArray list = (JsonArray) getResponse(bus, "updated-poker-list");
		assertEquals(1, list.size());
	}

	@Test
	public void testGetPoker() throws CallbackException {
		testAddPoker();
		Callback callback = new GetPokerCallback();
		WebsocketBus bus = mock(WebsocketBus.class);
		callback.messageReceived(mock(Caller.class), bus, "eventName",
				parse("\"fua\""));
		JsonObject poker = (JsonObject) getResponse(bus, "updated-poker");
		assertEquals("fua", poker.get("name").getAsString());
	}

	private JsonElement parse(String json) {
		return new JsonParser().parse(json);
	}

	private HttpServletResponse testTimeReport(String message)
			throws IOException, ServletException {
		TimeReportServlet servlet = new TimeReportServlet();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		Reader reader = new StringReader(message);
		when(request.getReader()).thenReturn(new BufferedReader(reader));

		servlet.doPost(request, response);

		return response;
	}

	@Test
	public void postTimeReport() throws ServletException, IOException,
			CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).setStatus(201);
	}

	@Test
	public void postTimeReportWrongDeveloper() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"wrong\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportWrongTaskId() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:2836234,"//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportWrongTimes() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now + 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportCategoryWithoutKeyword() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportKeywordsWithoutCategory()
			throws ServletException, IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"this_belongs_to_no_category\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportRepeatedCategory() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"motivacion_normal\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportMissingParameter() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";
		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());

		message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";
		response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());

		message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";
		response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());

		message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";
		response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());

		message = "{" //
				+ "taskId:" + taskId + ","//
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\""//
				+ "}";
		response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

	@Test
	public void postTimeReportInvalidJSON() throws ServletException,
			IOException, CallbackException {
		testAddDeveloper();
		testAddTask();
		long taskId = ((JsonObject) lastResponse).get("task").getAsJsonObject()
				.get("id").getAsLong();
		long now = new Date().getTime();
		String message = "{" //
				+ "taskId:" + taskId //
				+ "timeStart: " + (now - 1000) + ","//
				+ "timeEnd: " + now + ","//
				+ "developerName: \"fergonco\","//
				+ "keywords:[\"interrupciones_ninguna\", "//
				+ "\"urgencia_hay_presion\", "//
				+ "\"motivacion_mucha\", "//
				+ "\"satisfaccion_mucha\""//
				+ "]"//
				+ "}";

		HttpServletResponse response = testTimeReport(message);
		verify(response).sendError(eq(400), anyString());
	}

}
