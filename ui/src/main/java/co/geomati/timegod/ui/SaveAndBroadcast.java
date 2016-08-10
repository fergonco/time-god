package co.geomati.timegod.ui;

import java.util.HashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import co.geomati.timegod.ui.callbacks.AddDeveloperCallback;
import co.geomati.timegod.ui.callbacks.AddPokerCallback;
import co.geomati.timegod.ui.callbacks.AddTaskCallback;
import co.geomati.timegod.ui.callbacks.AddTaskIssueCallback;
import co.geomati.timegod.ui.callbacks.AssociateTaskIssueCallback;
import co.geomati.timegod.ui.callbacks.ChangePokerKeywordsCallback;
import co.geomati.timegod.ui.callbacks.ChangePokerRepositoryConfigurationCallback;
import co.geomati.timegod.ui.callbacks.ChangePokerTotalCreditsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskCommonCreditsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskKeywordsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskNameCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskUserCreditsCallback;
import co.geomati.timegod.ui.callbacks.DissociateTaskIssueCallback;
import co.geomati.timegod.ui.callbacks.GetDevelopersCallback;
import co.geomati.timegod.ui.callbacks.GetPokerCallback;
import co.geomati.timegod.ui.callbacks.GetPokersCallback;
import co.geomati.timegod.ui.callbacks.GetTaxonomyCallback;
import co.geomati.timegod.ui.callbacks.LoggingCallback;
import co.geomati.timegod.ui.callbacks.ProxyCallback;
import co.geomati.timegod.ui.callbacks.RemoveDeveloperCallback;
import co.geomati.timegod.ui.callbacks.RemovePokerCallback;
import co.geomati.timegod.ui.callbacks.RemoveTaskCallback;
import co.geomati.timegod.ui.callbacks.ReportTaskTimesCallback;
import co.geomati.timegod.ui.callbacks.SetPokerEventCallback;
import co.geomati.websocketBus.WebsocketBus;

@WebListener
public class SaveAndBroadcast implements ServletContextListener {

	private static final LoggingCallback[] loggingCallbacks = new LoggingCallback[] {
			new AddPokerCallback(), new ChangePokerKeywordsCallback(),
			new ChangePokerRepositoryConfigurationCallback(),
			new ChangePokerTotalCreditsCallback(), new RemovePokerCallback(),
			new AddTaskCallback(), new RemoveTaskCallback(),
			new ChangeTaskNameCallback(), new ChangeTaskUserCreditsCallback(),
			new ChangeTaskCommonCreditsCallback(),
			new ChangeTaskKeywordsCallback(), new ReportTaskTimesCallback(),
			new AddTaskIssueCallback(), new DissociateTaskIssueCallback(),
			new AssociateTaskIssueCallback(), new SetPokerEventCallback() };

	public void contextInitialized(ServletContextEvent sce) {
		WebsocketBus bus = WebsocketBus.INSTANCE;

		bus.addListener("add-developer", new AddDeveloperCallback());
		bus.addListener("remove-developer", new RemoveDeveloperCallback());
		bus.addListener("get-developers", new GetDevelopersCallback());
		bus.addListener("get-pokers", new GetPokersCallback());
		bus.addListener("get-poker", new GetPokerCallback());
		bus.addListener("get-taxonomy", new GetTaxonomyCallback());
		bus.addListener("proxy", new ProxyCallback());

		HashMap<String, LoggingCallback> loggingCallbackRegistry = new HashMap<String, LoggingCallback>();
		for (LoggingCallback loggingCallback : loggingCallbacks) {
			bus.addListener(loggingCallback.getEventName(), loggingCallback);
			loggingCallbackRegistry.put(loggingCallback.getEventName(),
					loggingCallback);
		}

		sce.getServletContext().setAttribute("logging-call-back-registry",
				loggingCallbackRegistry);

	}

	public void contextDestroyed(ServletContextEvent sce) {
		// noop
	}
}
