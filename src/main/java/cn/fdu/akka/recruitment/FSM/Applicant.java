package cn.fdu.akka.recruitment.FSM;

import java.util.LinkedList;
import java.util.List;

import akka.actor.AbstractFSM;
import cn.fdu.akka.recruitment.common.*;

import static cn.fdu.akka.recruitment.FSM.Applicant.State;
import static cn.fdu.akka.recruitment.FSM.Applicant.State.*;
import static cn.fdu.akka.recruitment.FSM.Applicant.Data;

public class Applicant extends AbstractFSM<State, Data>{

	{
		startWith(Init, new Data());

		when(
			Init,
			matchEvent(
				Resume.class,
				Data.class,
				(resume, data) -> {
					System.out.println("Init matchEvent Resume: " + resume);
					resume.getHrRef().tell(resume, getSelf());
//					resume.getHrRef().tell(resume.toString(), getSelf());
					return goTo(WaitingForInterview).using(data.addElement(resume));
				}));

		when(
			WaitingForInterview,
			matchEvent(
				Interview.class,
				Data.class,
				(interview, data) -> {
					System.out.println("WaitingForInterview matchEvent Interview, interview:" + interview);
					// Always agree.
					getSender().tell(new ApplicantOpinion(true), getSelf());
					return goTo(WaitingForNegotiation).using(data.addElement(interview));
				}));

		when(
			WaitingForNegotiation,
			matchEvent(
				Negotiation.class,
				Data.class,
				(negotiation, data) -> {
					System.out.println("WaitingForNegotiation matchEvent Negotiation, Negotiation:" + negotiation);
					// Always agree.
					getSender().tell(new ApplicantOpinion(true), getSelf());
					return goTo(WaitingForOffer).using(data.addElement(negotiation));
				}));

		when(
			WaitingForOffer,
			matchEvent(
				Offer.class,
				Data.class,
				(offer, data) -> {
					System.out.println("WaitingForOffer matchEvent Offer, offer:" + offer);
					return goTo(End).using(data.addElement(offer));
				}));

		when(
			End,
			matchAnyEvent(
				(event, state) -> {
					System.out.println("Applicant End");
					return stay();
				})
		);

		whenUnhandled(
			matchAnyEvent(
				(event, state) -> {
					System.out.println("unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
					return stay();
				}));
		initialize();
	}

	enum State{Init, WaitingForInterview, WaitingForNegotiation, WaitingForOffer, End}

	static final class Data{

		// resume, interview, negotiation, offer
		private final List<Object> data;

		public Data() {this.data = null;}

		public Data(List<Object> data) {
			this.data = data;
		}

		public List<Object> getData(){
			return data;
		}

		public Data addElement(Object r) {
			List<Object> newdata;
			if(data != null) {
				newdata = new LinkedList<>(data);
			}else{
				newdata = new LinkedList<>();
			}
			newdata.add(r);
			return new Data(newdata);
		}

		public Resume getResume() {
			return (Resume)data.get(0);
		}

		@Override
		public String toString() {
			return "Data: " + data;
		}
	}
}
