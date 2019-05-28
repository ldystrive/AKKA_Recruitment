package cn.fdu.akka.recruitment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class ActorTest {
	public static void main(String [] args) {
		System.out.println("actor dependency test");

		final ActorSystem system = ActorSystem.create("akkatest");

		system.terminate();

	}
}
