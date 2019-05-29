package cn.fdu.akka.recruitment.FSM;

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
					//resume.getHrRef().tell(resume, getSelf());
					resume.getHrRef().tell(resume.toString(), getSelf());
					return goTo(WaitingForInterview).using(data.addResume(resume));
				}));

		when(
			WaitingForInterview,
			matchEvent(
				Interview.class,
				Data.class,
				(interview, data) -> {
					System.out.println("WaitingForInterview matchEvent Interview, interview:" + interview);
					return goTo(WaitingForNegotiation).using(data.addInterview(interview));
				}));

		when(
			WaitingForNegotiation,
			matchEvent(
				Negotiation.class,
				Data.class,
				(negotiation, data) -> {
					System.out.println("WaitingForNegotiation matchEvent Negotiation, Negotiation:" + negotiation);
					return goTo(WaitingForOffer).using(data.addNegotiation(negotiation));
				}));

		when(
			WaitingForOffer,
			matchEvent(
				Offer.class,
				Data.class,
				(offer, data) -> {
					System.out.println("WaitingForOffer matchEvent Offer, offer:" + offer);
					return goTo(End).using(data.addOffer(offer));
				}));

		when(
			End,
			matchAnyEvent(
				(event, state) -> {
					System.out.println("End");
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
		public Resume resume;
		public Interview interview;
		public Negotiation negotiation;
		public Offer offer;

		public Data() {}

		public Data(Data d) {
			this.resume = d.resume;
			this.interview = d.interview;
			this.negotiation = d.negotiation;
			this.offer = d.offer;
		}

		public Data addResume(Resume r) {
			Data d = new Data(this);
			d.resume = new Resume(r);
			return d;
		}

		public Data addInterview(Interview i) {
			Data d = new Data(this);
			d.interview = new Interview(i);
			return d;
		}

		public Data addNegotiation(Negotiation n) {
			Data d = new Data(this);
			d.negotiation = new Negotiation(n);
			return d;
		}

		public Data addOffer(Offer o) {
			Data d = new Data(this);
			d.offer = new Offer(o);
			return d;
		}
	}
}
