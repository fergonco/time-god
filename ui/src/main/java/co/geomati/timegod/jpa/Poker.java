package co.geomati.timegod.jpa;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Poker {

	@Id
	private String name;

	@OneToMany(mappedBy = "poker")
	private ArrayList<Task> tasks;

	@OneToMany(mappedBy = "poker")
	private ArrayList<Event> events;

	private String[] keywords;
	private int totalCredits;
	private String apiRepository;
	private String webRepository;

	public String getName() {
		return name;
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public void setTotalCredits(int totalCredits) {
		this.totalCredits = totalCredits;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public int getTotalCredits() {
		return totalCredits;
	}

	public String getAPIRepository() {
		return apiRepository;
	}

	public void setApiRepository(String apiRepository) {
		this.apiRepository = apiRepository;
	}

	public String getWebRepository() {
		return webRepository;
	}

	public void setWebRepository(String webRepository) {
		this.webRepository = webRepository;
	}

}
