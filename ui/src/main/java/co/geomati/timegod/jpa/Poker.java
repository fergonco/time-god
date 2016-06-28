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

	private String[] keywords;

	private int totalCredits;

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
}
