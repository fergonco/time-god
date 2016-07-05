package co.geomati.timegod.ui.callbacks;

import co.geomati.timegod.jpa.Task;

public class TaskUpdatedMessage {

	private String pokerName;
	private Task task;

	public TaskUpdatedMessage(String name, Task task) {
		this.pokerName = name;
		this.task = task;
	}

}
