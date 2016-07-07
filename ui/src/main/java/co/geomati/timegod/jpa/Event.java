package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Event {

	@Id
	@GeneratedValue
	private long id;

	private long timestamp;

	private String[] keywords;

	@ManyToOne
	private Poker poker;

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
}
