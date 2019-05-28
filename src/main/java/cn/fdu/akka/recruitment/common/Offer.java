package cn.fdu.akka.recruitment.common;

public class Offer {
	Resume resume;

	public Offer(Resume resume) {
		this.resume = resume;
	}

	public String toString() {
		return "Offer_" + resume;
	}
}
