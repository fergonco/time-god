package co.geomati.timegod.ui.callbacks;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.ui.DBUtils;
import co.geomati.websocketBus.Caller;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChangePokerRepositoryConfigurationCallback extends
		AbstractLoggingCallback implements LoggingCallback {

	public void messageReceived(Caller caller, WebsocketBus bus,
			String eventName, JsonElement payload) {
		JsonObject updatePokerMessage = payload.getAsJsonObject();

		String pokerName = updatePokerMessage.get("pokerName").getAsString();
		EntityManager em = DBUtils.getEntityManager();
		Poker poker = em.find(Poker.class, pokerName);

		String[] oldIssueRepository = poker.getIssueRepositories();
		String oldWikiRepository = poker.getWikiRepository();

		String[] issueRepositories = GSON.fromJson(
				updatePokerMessage.get("issueRepositories").getAsJsonArray(),
				String[].class);
		String wikiRepository = updatePokerMessage.get("wikiRepository")
				.getAsString();
		for (int i = 0; i < issueRepositories.length; i++) {
			issueRepositories[i] = inputCheck(issueRepositories[i]);
		}
		wikiRepository = inputCheck(wikiRepository);

		em.getTransaction().begin();
		poker.setIssueRepositories(issueRepositories);
		poker.setWikiRepository(wikiRepository);
		em.getTransaction().commit();

		log(eventName,
				payload,
				new Memento(pokerName, oldIssueRepository, oldWikiRepository,
						poker.getIssueRepositories(), poker.getWikiRepository()));

		bus.broadcast("updated-poker", GSON.toJsonTree(poker));
	}

	private String inputCheck(String repository) {
		repository = repository.trim();
		if (!repository.endsWith("/")) {
			repository += "/";
		}

		return repository;
	}

	public String getEventName() {
		return "change-poker-repository-configuration";
	}

	@Override
	protected Class<?> getMementoClass() {
		return Memento.class;
	}

	public class Memento {
		private String pokerName;
		private String[] oldIssueRepositories;
		private String oldWikiRepository;
		private String[] newIssueRepositories;
		private String newWikiRepository;

		public Memento() {
		}

		public Memento(String pokerName, String[] oldIssueRepositories,
				String oldWikiRepository, String[] newIssueRepositories,
				String newWikiRepository) {
			super();
			this.pokerName = pokerName;
			this.oldIssueRepositories = oldIssueRepositories;
			this.oldWikiRepository = oldWikiRepository;
			this.newIssueRepositories = newIssueRepositories;
			this.newWikiRepository = newWikiRepository;
		}

		@Override
		public String toString() {
			return "en poker "
					+ pokerName
					+ "\n" //
					+ " Wiki de: "
					+ oldWikiRepository
					+ "\n      a: "
					+ newWikiRepository
					+ "\n" //
					+ " Issue de: "
					+ StringUtils
							.join(Arrays.asList(oldIssueRepositories), ",")
					+ "\n      a: "
					+ StringUtils
							.join(Arrays.asList(newIssueRepositories), ",");
		}
	}

}