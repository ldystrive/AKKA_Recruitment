package cn.fdu.akka.recruitment.common;

public final class Offer {
	public final Resume resume;

	public Offer(Resume resume) {
		this.resume = resume;
	}

	public Offer(Offer o) {
		this(o.resume);
	}

	public String toString() {
		return "Offer_" + resume;
	}

	public Resume getResume() {
		return this.resume;
	}
}
