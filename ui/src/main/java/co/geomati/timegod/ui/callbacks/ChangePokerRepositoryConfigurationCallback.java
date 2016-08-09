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
		em.getTransaction().begin();
		String apiRepository = updatePokerMessage.get("apiRepository")
				.getAsString();
		String webRepository = updatePokerMessage.get("webRepository")
				.getAsString();
		apiRepository = inputCheck(apiRepository);
		webRepository = inputCheck(webRepository);
		poker.setApiRepository(apiRepository);
		poker.setWebRepository(webRepository);
		em.getTransaction().commit();

		log(eventName, payload,
				new Memento(pokerName, oldAPIRepository, oldWebRepository,
						poker.getAPIRepository(), poker.getWebRepository()));

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
		private String newAPIRepository;
		private String newWebRepository;

		public Memento() {
		}

		public Memento(String pokerName, String oldAPIRepository,
				String oldWebRepository, String newAPIRepository,
				String newWebRepository) {
			super();
			this.pokerName = pokerName;
			this.oldAPIRepository = oldAPIRepository;
			this.oldWebRepository = oldWebRepository;
			this.newAPIRepository = newAPIRepository;
			this.newWebRepository = newWebRepository;
		}

		@Override
		public String toString() {
			return "en poker "
					+ pokerName
					+ "\n" //
					+ " API de: " + oldAPIRepository + "\n      a: "
					+ newAPIRepository
					+ "\n" //
					+ " Web de: " + oldWebRepository + "\n      a: "
					+ newWebRepository;
		}
	}

}