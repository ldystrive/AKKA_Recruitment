package cn.fdu.akka.gen;

import akka.actor.ActorRef;

public class ActorEvent {
	String name;
	ActorRef ref;

	public ActorEvent(ActorRef ref, String name) {
		this.ref = ref;
		this.name = name;	}
}