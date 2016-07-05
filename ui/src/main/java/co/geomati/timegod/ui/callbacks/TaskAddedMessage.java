package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Task;

public class TaskAddedMessage {

	private String pokerName;
	private Task task;

	public TaskAddedMessage(String name, Task task) {
		this.pokerName = name;
		this.task = task;
	}

}
