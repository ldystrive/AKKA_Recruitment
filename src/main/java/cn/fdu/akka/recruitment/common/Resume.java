package cn.fdu.akka.recruitment.common;

import akka.actor.ActorRef;

public final class Resume {

	private String name;
	private String position;
	private final ActorRef hr;

	public Resume(String name, String position, ActorRef hr) {
		this.name = name;
		this.position = position;
		this.hr = hr;
	}

	public String toString() {
		return name + '_' + position;
	}
}
