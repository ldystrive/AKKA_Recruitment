package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import cn.fdu.akka.recruitment.common.*;

import static cn.fdu.akka.recruitment.FSM.Applicant.State;
import static cn.fdu.akka.recruitment.FSM.Applicant.State.*;
import static cn.fdu.akka.recruitment.FSM.Applicant.Data;
import static cn.fdu.akka.recruitment.FSM.Applicant.Uninitialized.*;

public class Applicant extends AbstractFSM<State, Data>{

	{
		startWith(Init, Uninitialized);

		when(
			Init,
			matchEvent(
				Resume.class,
				Uninitialized.class,
					(resume, uninitialized) -> {
						System.out.println("when Init match Resume: " + resume);
						return stay();}));


		whenUnhandled(
			matchAnyEvent(
				(event, state) -> {
					System.out.println("unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
					return stay();
				}));
		initialize();
	}

	enum State{Init, WaitingForInterview, WaitingForNegotiation, WaitingForOffer, End}
	interface Data {}
	enum Uninitialized implements Data {
		Uninitialized
	}



}

