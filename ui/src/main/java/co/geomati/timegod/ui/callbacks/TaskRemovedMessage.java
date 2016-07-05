package co.geomati.timegod.ui.callbacks;


public class TaskRemovedMessage {

	private String pokerName;
	private long taskId;

	public TaskRemovedMessage(String name, long taskId) {
		this.pokerName = name;
		this.taskId = taskId;
	}

}
