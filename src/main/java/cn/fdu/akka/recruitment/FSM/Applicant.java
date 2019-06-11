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
				})
			.event(
					Query.class,
					Data.class,
					(query, data) -> {
						getSender().tell("Init", getSelf());
						return stay().using(data);
					}
			)
		);

		when(
			WaitingForInterview,
			matchEvent(
				Interview.class,
				Data.class,
				(interview, data) -> {
					System.out.println("WaitingForInterview matchEvent Interview, interview:" + interview);
					return goTo(AlreadyCheckedInterview).using(data.addElement(interview));
				})
			.event(
					Query.class,
					Data.class,
					(query, data) -> {
						getSender().tell("Waiting for interview", getSelf());
						return stay().using(data);
					}
			)
		);

		when(
			AlreadyCheckedInterview,
			matchEvent(
				Opinion.class,
				Data.class,
				(opinion, data) -> {
					if (opinion.getOpinion()) {
						data.getInterview().getResume().getHrRef().tell(new ApplicantOpinion(true), getSelf());
						return goTo(WaitingForNegotiation).using(data);
					} else {
					    //TODO
						data.getInterview().getResume().getHrRef().tell(new ApplicantOpinion(true), getSelf());
						return goTo(WaitingForNegotiation).using(data);
					}
				}
			)
			.event(
					Query.class,
					Data.class,
					(query, data) -> {
						getSender().tell("Waiting for checking interview:" + data.getInterview().toString(), getSelf());
						return stay().using(data);
					}
			)
		);

		when(
			WaitingForNegotiation,
			matchEvent(
				Negotiation.class,
				Data.class,
				(negotiation, data) -> {
					System.out.println("WaitingForNegotiation matchEvent Negotiation, Negotiation:" + negotiation);
					// Always agree.
//					getSender().tell(new ApplicantOpinion(true), getSelf());
					return goTo(AlreadyCheckedNegotiation).using(data.addElement(negotiation));
				})
			.event(
					Query.class,
					Data.class,
					(query, data) -> {
						getSender().tell("Waiting for negotiation", getSelf());
						return stay().using(data);
					}
			)
		);

		when(
			AlreadyCheckedNegotiation,
			matchEvent(
				Opinion.class,
				Data.class,
				(opinion, data) -> {
					if (opinion.getOpinion()) {
						data.getInterview().getResume().getHrRef().tell(new ApplicantOpinion(true), getSelf());
						return goTo(WaitingForOffer).using(data);
					} else {
						data.getInterview().getResume().getHrRef().tell(new ApplicantOpinion(false), getSelf());
						return stay().using(data);
					}
				}
			)
			.event(
					Query.class,
					Data.class,
					(query, data) -> {
						getSender().tell("Waiting for checking negotiation:" + data.getNegotiation(), getSelf());
						return stay().using(data);
					}
			)
		);

		when(
			WaitingForOffer,
			matchEvent(
				Offer.class,
				Data.class,
				(offer, data) -> {
					System.out.println("WaitingForOffer matchEvent Offer, offer:" + offer);
					return goTo(End).using(data.addElement(offer));
				})
			.event(
					Query.class,
					Data.class,
					(query, data) -> {
						getSender().tell("Waiting for offer", getSelf());
						return stay().using(data);
					}
			)
		);

		when(
			End,
			matchEvent(
			        Query.class,
					Data.class,
					(query, data) -> {
			        	getSender().tell("Offer!" + data.getOffer(), getSelf());
			        	return stay().using(data);
					}
			)
			.anyEvent(
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

	enum State{
		Init,
		WaitingForInterview,
		AlreadyCheckedInterview,
		WaitingForNegotiation,
		AlreadyCheckedNegotiation,
		WaitingForOffer,
		End
	}

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

		public Resume getResume()
		{
			return (Resume)data.get(0);
		}

		public Interview getInterview() {
			return (Interview)data.get(1);
		}

		public Negotiation getNegotiation() {
			return (Negotiation)data.get(2);
		}

		public Offer getOffer() {
			return (Offer)data.get(3);
		}

		@Override
		public String toString() {
			return "Data: " + data;
		}
	}
}
