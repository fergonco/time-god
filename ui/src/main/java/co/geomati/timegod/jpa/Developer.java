package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Developer {

	@Id
	private String name;

	private String password;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
