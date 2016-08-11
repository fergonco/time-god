package co.geomati.timegod.jpa;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Task {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	private String[] keywords;

	private String[] issues;

	private long creationTime;

	@OneToMany
	private ArrayList<Estimation> estimations;

	@OneToMany
	private ArrayList<TimeSegment> timeSegments;

	@ManyToOne
	private Poker poker;

	private Integer commonEstimation;

	public String getName() {
		return name;
	}

	public Poker getPoker() {
		return poker;
	}

	public ArrayList<Estimation> getEstimations() {
		return estimations;
	}

	public ArrayList<TimeSegment> getTimeSegments() {
		return timeSegments;
	}

	public Integer getCommonEstimation() {
		return commonEstimation;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEstimations(ArrayList<Estimation> estimations) {
		this.estimations = estimations;
	}

	public void setCommonEstimation(Integer commonEstimation) {
		this.commonEstimation = commonEstimation;
	}

	public void setPoker(Poker poker) {
		this.poker = poker;
	}

	public long getId() {
		return id;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public void setTimeSegments(ArrayList<TimeSegment> timeSegments) {
		this.timeSegments = timeSegments;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String[] getIssues() {
		return issues;
	}

	public void setIssues(String[] issues) {
		this.issues = issues;
	}
}