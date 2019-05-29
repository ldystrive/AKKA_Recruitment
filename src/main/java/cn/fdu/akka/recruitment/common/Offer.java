package cn.fdu.akka.recruitment.common;

public final class Offer {
	public Resume resume;

	public Offer(Resume resume) {
		this.resume = resume;
	}

	public Offer(Offer o) {
		this(o.resume);
	}

	public String toString() {
		return "Offer_" + resume;
	}
}
