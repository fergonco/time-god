package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class TimeSegment {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timeSegmentSequence")
	@SequenceGenerator(name = "timeSegmentSequence")
	private long id;

	private long start;
	private long end;
	private String[] keywords;

	@ManyToOne
	private Developer developer;

	public void setStart(long start) {
		this.start = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}

	public long getId() {
		return id;
	}
}
