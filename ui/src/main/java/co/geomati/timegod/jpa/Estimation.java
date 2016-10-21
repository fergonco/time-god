package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Estimation {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estimationSequence")
	@SequenceGenerator(name = "estimationSequence")
	private long id;

	@OneToOne
	private Developer developer;

	private Integer value;

	public Integer getValue() {
		return value;
	}

	public Developer getDeveloper() {
		return developer;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}
}
