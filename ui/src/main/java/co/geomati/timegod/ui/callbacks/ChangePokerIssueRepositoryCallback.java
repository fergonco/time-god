package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangePokerIssueRepositoryCallback extends AbstractLoggingCallback
		implements LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updatePokerMessage = payload.getAsJsonObject();
		String pokerName = updatePokerMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);
		String oldIssueRepository = poker.getIssueRepository();
		em.getTransaction().begin();
		String issueRepository = updatePokerMessage.get("issueRepository")
				.getAsString();
		issueRepository = issueRepository.trim();
		if (!issueRepository.endsWith("/")) {
			issueRepository += "/";
		}
		poker.setIssueRepository(issueRepository);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(pokerName, oldIssueRepository,
				poker.getIssueRepository()));

		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}

	public String getEventName() {
		return "change-poker-issueRepository";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String oldIssueRepository;
		private String newIssueRepository;

		public Memento() {
		}

		public Memento(String pokerName, String oldIssueRepository,
				String newIssueRepository) {
			this.pokerName = pokerName;
			this.oldIssueRepository = oldIssueRepository;
			this.newIssueRepository = newIssueRepository;
		}

		@Override
		public String toString() {
			return "en poker " + pokerName + "\n" //
					+ " de: " + oldIssueRepository + "\n" //
					+ " a : " + newIssueRepository;
		}
	}

}