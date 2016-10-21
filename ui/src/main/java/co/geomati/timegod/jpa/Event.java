package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventSequence")
	@SequenceGenerator(name = "eventSequence")
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
