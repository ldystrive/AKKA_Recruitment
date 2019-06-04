package cn.fdu.akka.recruitment;


import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import akka.actor.ActorRef;


import cn.fdu.akka.recruitment.FSM.Applicant;


import cn.fdu.akka.recruitment.FSM.HRCompany;
import cn.fdu.akka.recruitment.common.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;



public class FSMTest extends JUnitSuite{

	private static ActorSystem system;

	@BeforeClass
	public static void setup() { system = ActorSystem.create("FSMTest");}

	@AfterClass
	public static void tearDown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testHRCompany(){
		new TestKit(system) {
			{
				final ActorRef tActor = system.actorOf(Props.create(HRCompany.HRCompanyFsm.class));
				final ActorRef probe = getRef();
				System.out.println("HRCompany test start");
				Resume resume = new Resume("abc", new Position("HR"), probe, null);
				tActor.tell(resume, probe);
//				expectMsg(resume.toString());

				Position position = new Position("HR");
				tActor.tell(position, probe);

//				system.stop(tActor);
			}
		};
	}

	@Test
	public void testApplicantActor() {
		new TestKit(system) {
			{
				final ActorRef tActor = system.actorOf(Props.create(Applicant.class));
				final ActorRef probe = getRef();
				System.out.println("Applicant test start");
				Resume resume = new Resume("abc", new Position("HR"), probe, null);
				tActor.tell(resume, probe);
				expectMsg(resume.toString());

				Interview interview = new Interview(resume);
				tActor.tell(interview, probe);

				Negotiation negotiation = new Negotiation(resume);
				tActor.tell(negotiation, probe);

				Offer offer = new Offer(resume);
				tActor.tell(offer, probe);

				system.stop(tActor);
			}
		};

	}

}
