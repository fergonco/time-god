package co.geomati.timegod.ui.callbacks;

import java.io.IOException;
import java.io.InputStream;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;

import co.geomati.timegod.jpa.Taxonomy;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Callback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GetTaxonomyCallback extends AbstractCallBack implements Callback {

	public void messageReceived(WebsocketBus bus, JsonElement payload) {
		EntityManager em = DBUtils.getEntityManager();
		String taxonomyType = payload.getAsString();
		Taxonomy taxonomy = em.find(Taxonomy.class, taxonomyType);
		if (taxonomy == null) {
			try {
				InputStream input = this.getClass().getResourceAsStream(
						taxonomyType + "-taxonomy.json");
				String taxonomyContent = IOUtils.toString(input);
				input.close();
				taxonomy = new Taxonomy();
				taxonomy.setName(taxonomyType);
				taxonomy.setContent(taxonomyContent);
				em.getTransaction().begin();
				em.persist(taxonomy);
				em.getTransaction().commit();
			} catch (IOException e) {
				// TODO
			}
		}

		bus.broadcast("updated-taxonomy",
				new JsonParser().parse(taxonomy.getContent()));

	}
}
