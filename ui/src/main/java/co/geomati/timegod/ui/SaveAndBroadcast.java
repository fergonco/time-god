package co.geomati.timegod.ui;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import co.geomati.timegod.ui.callbacks.AddDeveloperCallback;
import co.geomati.timegod.ui.callbacks.AddPokerCallback;
import co.geomati.timegod.ui.callbacks.AddTaskCallback;
import co.geomati.timegod.ui.callbacks.ChangePokerKeywordsCallback;
import co.geomati.timegod.ui.callbacks.ChangePokerTotalCreditsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskCommonCreditsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskKeywordsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskNameCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskUserCreditsCallback;
import co.geomati.timegod.ui.callbacks.ChangeTaskWikiCallback;
import co.geomati.timegod.ui.callbacks.GetDevelopersCallback;
import co.geomati.timegod.ui.callbacks.GetPokerCallback;
import co.geomati.timegod.ui.callbacks.GetPokersCallback;
import co.geomati.timegod.ui.callbacks.GetTaxonomyCallback;
import co.geomati.timegod.ui.callbacks.RemoveDeveloperCallback;
import co.geomati.timegod.ui.callbacks.RemovePokerCallback;
import co.geomati.timegod.ui.callbacks.RemoveTaskCallback;
import co.geomati.timegod.ui.callbacks.ReportTaskTimesCallback;
import co.geomati.timegod.ui.callbacks.SetPokerEventCallback;
import co.geomati.websocketBus.WebsocketBus;

@WebListener
public class SaveAndBroadcast implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		WebsocketBus bus = WebsocketBus.INSTANCE;
		bus.addListener("add-developer", new AddDeveloperCallback());
		bus.addListener("remove-developer", new RemoveDeveloperCallback());
		bus.addListener("add-poker", new AddPokerCallback());
		bus.addListener("change-poker-keywords",
				new ChangePokerKeywordsCallback());
		bus.addListener("add-poker-event", new SetPokerEventCallback());
		bus.addListener("change-poker-totalCredits",
				new ChangePokerTotalCreditsCallback());
		bus.addListener("remove-poker", new RemovePokerCallback());
		bus.addListener("add-task-to-poker", new AddTaskCallback());
		bus.addListener("remove-task", new RemoveTaskCallback());
		bus.addListener("change-task-name", new ChangeTaskNameCallback());
		bus.addListener("change-task-user-credits",
				new ChangeTaskUserCreditsCallback());
		bus.addListener("change-task-common-credits",
				new ChangeTaskCommonCreditsCallback());
		bus.addListener("change-task-wiki", new ChangeTaskWikiCallback());
		bus.addListener("change-task-keywords",
				new ChangeTaskKeywordsCallback());
		bus.addListener("report-task-time", new ReportTaskTimesCallback());
		bus.addListener("get-developers", new GetDevelopersCallback());
		bus.addListener("get-pokers", new GetPokersCallback());
		bus.addListener("get-poker", new GetPokerCallback());

		bus.addListener("get-taxonomy", new GetTaxonomyCallback());

	}

	public void contextDestroyed(ServletContextEvent sce) {
		// noop
	}
}
