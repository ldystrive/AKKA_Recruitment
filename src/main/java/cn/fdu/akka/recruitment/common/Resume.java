package cn.fdu.akka.recruitment.common;

import akka.actor.ActorRef;
import cn.fdu.akka.recruitment.FSM.Applicant;

public final class Resume {

	private String name;
	private String position;
	private final ActorRef hrRef;
	private final ActorRef applicantRef;

	public Resume(String name, String position, ActorRef hrRef, ActorRef applicantRef) {
		this.name = name;
		this.position = position;
		this.hrRef = hrRef;
		this.applicantRef = applicantRef;
	}

	public Resume(Resume r) {
		this(r.name, r.position, r.hrRef, r.applicantRef);
	}

	public ActorRef getHrRef() { return hrRef; }

	public ActorRef getApplicantRef() { return applicantRef; }

	public String getPosition() { return position; }

	public Resume setHrRef(ActorRef hr) {
		return new Resume(this.name, this.position, hr, this.applicantRef);
	}

	public String toString() {
		return name + '_' + position;
	}
}
