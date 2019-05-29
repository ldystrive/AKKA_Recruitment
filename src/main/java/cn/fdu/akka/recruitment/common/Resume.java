package cn.fdu.akka.recruitment.common;

import akka.actor.ActorRef;
import cn.fdu.akka.recruitment.FSM.Applicant;

public final class Resume {

	private String name;
	private Position position;
	private final ActorRef hrRef;
	private final ActorRef applicantRef;

	public Resume(String name, Position position, ActorRef hrRef, ActorRef applicantRef) {
		this.name = name;
		this.position = position;
		this.hrRef = hrRef;
		this.applicantRef = applicantRef;
	}

	public String toString() {
		return name + '_' + position;
	}
}
