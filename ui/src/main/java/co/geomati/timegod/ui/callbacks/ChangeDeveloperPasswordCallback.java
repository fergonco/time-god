package co.geomati.timegod.ui.callbacks;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.EntityManager;

import org.apache.commons.codec.binary.Hex;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.CallbackException;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

public class ChangeDeveloperPasswordCallback implements Callback {

	public void messageReceived(Caller caller, WebsocketBus bus, String eventName, JsonElement payload)
			throws CallbackException {
		JsonObject updateMessage = payload.getAsJsonObject();
		String developerName = updateMessage.get("developerName").getAsString();
		String newpassword = updateMessage.get("newPassword").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Developer developer = em.find(Developer.class, developerName);
		em.getTransaction().begin();
		try {
			developer.setPassword(Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(newpassword.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			throw new CallbackException("No se pudo cambiar el password");
		}
		em.getTransaction().commit();
	}

}