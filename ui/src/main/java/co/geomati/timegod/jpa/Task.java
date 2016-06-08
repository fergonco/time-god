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

	@OneToMany
	private ArrayList<Estimation> estimations;

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

}