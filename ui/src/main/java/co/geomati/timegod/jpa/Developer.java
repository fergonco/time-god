package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Developer {

	@Id
	private String name;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
