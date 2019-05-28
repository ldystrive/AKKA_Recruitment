package cn.fdu.akka.recruitment.common;


import java.util.Date;

public class  Negotiation{
	private Resume resume;
	private Date date;
	private String place;

	public Negotiation(Resume resume, Date data, String place) {
		this.date = data;
		this.place = place;
		this.resume = resume;
	}

	public Negotiation(Resume resume) {
		this.resume = resume;
		this.date = new Date();
		this.place = "zj";
	}

	public String toString() {
		return "Negotiation_" + resume + "_" + date + "_" + place;
	}

}
