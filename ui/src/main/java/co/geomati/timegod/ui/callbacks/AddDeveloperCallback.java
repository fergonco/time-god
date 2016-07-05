package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;

public class AddDeveloperCallback extends AbstractCallBack implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			JsonElement payload) {
		Developer developer = GSON.fromJson(payload.getAsJsonObject(),
				Developer.class);
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(developer);
		em.getTransaction().commit();
		bus.broadcast("developer-added", GSON.toJsonTree(developer));
	}
}