package cn.fdu.akka.recruitment;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import akka.actor.ActorRef;


import cn.fdu.akka.recruitment.FSM.Applicant;

import cn.fdu.akka.gen.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

public class VMTest extends JUnitSuite{

	private static ActorSystem system;

	@BeforeClass
	public static void setup() { system = ActorSystem.create("VMTest");}

	@AfterClass
	public static void tearDown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testVM(){
		new TestKit(system) {
			{
				final ActorRef tActor = system.actorOf(Props.create(FSM_Top.TopFSM.class));
				tActor.tell(new Event_init(""),getRef());
				tActor.tell(new Event_buy(""), getRef());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tActor.tell(new Event_buy(""), getRef());
				 try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tActor.tell(new Event_buy(""), getRef());

			}
		};
	}


}