package cn.fdu.akka.recruitment.common;

import java.util.Date;

public final class Interview {
	final private Resume resume;
	final private Date date;
	final private String place;

	public Interview(Resume resume, Date data, String place) {
		this.date = data;
		this.place = place;
		this.resume = resume;
	}

	public Interview(Interview interview) {
		this(interview.resume, interview.date, interview.place);
	}

	public Interview(Resume resume) {
		this.resume = resume;
		this.date = new Date();
		this.place = "zj";
	}

	public Resume getResume(){
		return resume;
	}

	public String toString() {
		return "Interview_" + resume + "_(" + date + ")_" + place;
	}

}
