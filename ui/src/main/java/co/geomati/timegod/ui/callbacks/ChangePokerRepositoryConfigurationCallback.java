package co.geomati.timegod.ui.callbacks;

import javax.persistence.EntityManager;

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
		String oldAPIRepository = poker.getAPIRepository();
		String oldWebRepository = poker.getWebRepository();
		String oldWikiRepository = poker.getWikiRepository();
		em.getTransaction().begin();
		String apiRepository = updatePokerMessage.get("apiRepository")
				.getAsString();
		String webRepository = updatePokerMessage.get("webRepository")
				.getAsString();
		String wikiRepository = updatePokerMessage.get("wikiRepository")
				.getAsString();
		apiRepository = inputCheck(apiRepository);
		webRepository = inputCheck(webRepository);
		wikiRepository = inputCheck(wikiRepository);
		poker.setApiRepository(apiRepository);
		poker.setWebRepository(webRepository);
		poker.setWikiRepository(wikiRepository);
		em.getTransaction().commit();

		log(eventName, payload, new Memento(pokerName, oldAPIRepository,
				oldWebRepository, oldWikiRepository, poker.getAPIRepository(),
				poker.getWebRepository(), poker.getWikiRepository()));

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
		private String oldAPIRepository;
		private String oldWebRepository;
		private String oldWikiRepository;
		private String newAPIRepository;
		private String newWebRepository;
		private String newWikiRepository;

		public Memento() {
		}

		public Memento(String pokerName, String oldAPIRepository,
				String oldWebRepository, String oldWikiRepository,
				String newAPIRepository, String newWebRepository,
				String newWikiRepository) {
			super();
			this.pokerName = pokerName;
			this.oldAPIRepository = oldAPIRepository;
			this.oldWebRepository = oldWebRepository;
			this.oldWikiRepository = oldWikiRepository;
			this.newAPIRepository = newAPIRepository;
			this.newWebRepository = newWebRepository;
			this.newWikiRepository = newWikiRepository;
		}

		@Override
		public String toString() {
			return "en poker "
					+ pokerName
					+ "\n" //
					+ " Wiki de: " + oldWikiRepository
					+ "\n      a: "
					+ newWikiRepository
					+ "\n" //
					+ " API de: " + oldAPIRepository + "\n      a: "
					+ newAPIRepository
					+ "\n" //
					+ " Web de: " + oldWebRepository + "\n      a: "
					+ newWebRepository;
		}
	}

}