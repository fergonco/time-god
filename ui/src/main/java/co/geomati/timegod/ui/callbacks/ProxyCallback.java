package co.geomati.timegod.ui.callbacks;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class ProxyCallback extends AbstractCallback implements Callback {

	public void messageReceived(final Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) throws CallbackException {
		final JsonObject proxyMessage = (JsonObject) payload;

		new Thread(new Runnable() {
			public void run() {
				try {
					HttpClient client = new HttpClient();
					GetMethod get = new GetMethod(proxyMessage.get("url")
							.getAsString());
					String user = System.getenv("TIMEGOD_GITHUB_API_USER");
					String password = System
							.getenv("TIMEGOD_GITHUB_API_PASSWORD");
					String encoding = new String(Base64.encodeBase64((user
							+ ":" + password).getBytes()));
					get.addRequestHeader("Authorization", "Basic " + encoding);
					int status = client.executeMethod(get);
					if (status == 200) {
						String responseString = get.getResponseBodyAsString();

						JsonElement proxiedResponse = new JsonParser()
								.parse(responseString);
						JsonObject response = new JsonObject();
						response.add("response", proxiedResponse);
						response.add("context", proxyMessage.get("context"));
						caller.send(proxyMessage.get("event-name")
								.getAsString(), response);
					} else {
						caller.sendError("Proxy fail");
					}
				} catch (JsonSyntaxException e) {
					caller.sendError("Proxy fail");
				} catch (IOException e) {
					caller.sendError("Proxy fail");
				}
			}
		}).start();
	}
}
