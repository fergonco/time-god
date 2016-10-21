package co.geomati.timegod.ui.callbacks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

public class ProxyCallback extends AbstractCallback implements Callback {

	public void messageReceived(final Caller caller, WebsocketBus bus, String eventName, JsonElement payload)
			throws CallbackException {
		final JsonObject proxyMessage = (JsonObject) payload;

		new Thread(new Runnable() {
			public void run() {
				try {
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet get = new HttpGet(proxyMessage.get("url").getAsString());
					String user = System.getenv("TIMEGOD_GITHUB_API_USER");
					String password = System.getenv("TIMEGOD_GITHUB_API_PASSWORD");
					String encoding = new String(Base64.encodeBase64((user + ":" + password).getBytes()));
					get.addHeader("Authorization", "Basic " + encoding);
					HttpResponse response = client.execute(get);
					if (response.getStatusLine().getStatusCode() == 200) {
						InputStream responseStream = response.getEntity().getContent();
						String body = IOUtils.toString(responseStream);
						responseStream.close();
						JsonElement proxiedResponse = new JsonParser().parse(body);
						JsonObject responseJSON = new JsonObject();
						responseJSON.add("response", proxiedResponse);
						responseJSON.add("context", proxyMessage.get("context"));
						caller.send(proxyMessage.get("event-name").getAsString(), responseJSON);
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
