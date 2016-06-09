package co.geomati.timegod.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TimeSegment {

	@Id
	@GeneratedValue
	private long id;

	private long start;
	private long end;
	private String[] keywords;

	public void setStart(long start) {
		this.start = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
}
