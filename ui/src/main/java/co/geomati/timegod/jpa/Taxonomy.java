package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Taxonomy {

	@Id
	private String name;

	private String content;

	public String getContent() {
		return content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
