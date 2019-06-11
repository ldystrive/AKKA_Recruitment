package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import akka.actor.ActorRef;

import java.util.HashMap;

import static cn.fdu.akka.recruitment.FSM.ComApplicant.State;
import static cn.fdu.akka.recruitment.FSM.ComApplicant.Data;
import static cn.fdu.akka.recruitment.FSM.ComApplicant.State.*;

public class ComApplicant extends AbstractFSM<State, Data>{

	{
		startWith(WaitingForInterview, new Data());
		when(
			WaitingForInterview,
			matchEvent(
				Interview.class,
				Data.class,
				(interview, data) -> {
					return goTo(AlreadyCheckedInterview).using(data.addInterview(interview));
				})
			.event(
				Query.class,
				Data.class,
				(q, data)->{
					getSender().tell("WaitingForInterview", getSelf());
					return stay().using(data);
				})
		);

		when(
			AlreadyCheckedInterview,
			matchEvent(
				Opinion.class,
				Data.class,
				(opinion, data) -> {
					data.getInterview().getResume().getHrRef().tell(new CompanyOpinion(true), getSelf());
					return goTo(WaitingForNegotiate).using(data);
				})
			.event(
				Query.class,
				Data.class,
				(q, data)->{
					getSender().tell("Waiting for checking interview:" + data.getInterview().toString(), getSelf());
					return stay().using(data);
				})
		);

		when(
			WaitingForNegotiate,
			matchEvent(
				Negotiation.class,
				Data.class,
				(negotiation, data) -> {
					return goTo(AlreadyCheckedNegotiation).using(data.addNegotiation(negotiation));
				}
			)
			.event(
				Query.class,
				Data.class,
				(q, data)->{
					getSender().tell("WaitingForNegotiate", getSelf());
					return stay().using(data);
				}));

		when(
			AlreadyCheckedNegotiation,
			matchEvent(
				Opinion.class,
				Data.class,
				(opinion, data) -> {
					data.getInterview().getResume().getHrRef().tell(new CompanyOpinion(true), getSelf());
//					getSender().tell(new CompanyOpinion(true), getSelf());
					return goTo(WaitingForOffer).using(data);
				})
			.event(
				Query.class,
				Data.class,
				(q, data)->{
					getSender().tell("Waiting for checking negotiation:" + data.getNegotiation().toString(), getSelf());
					return stay();
			}));

		when(
			WaitingForOffer,
			matchEvent(
				WaitingOffer.class,
				Data.class,
				(a, data)->{
					Resume resume = data.getInterview().getResume();
					Offer offer = new Offer(resume);
					data.getInterview().getResume().getHrRef().tell(offer, getSelf());
					return goTo(End).using(data);
				})
			.event(
				Query.class,
				Data.class,
				(q, data)->{
					getSender().tell("WaitingOffer", getSelf());
					return stay();
			})
		);

		when(
			End,
			matchEvent(
					Query.class,
					Data.class,
					(query, data)-> {
						getSender().tell("End", getSelf());
						return stay();
					}
			)
			.anyEvent(
				(event, state)->{
					System.out.println("Resume end");
					return stay();
				}
			)
		);

		initialize();
	}

	enum State{
		WaitingForInterview,
		AlreadyCheckedInterview,
		WaitingForNegotiate,
		AlreadyCheckedNegotiation,
		WaitingForOffer,
		End
	}

	static final class Data{
		private final HashMap<String, Object> map;

		public Data() {
			map = new HashMap<String, Object>();
		}

		public Data(HashMap<String, Object> h) {
			this.map = new HashMap<String, Object>(h);
		}

		public Data(Data d) {
			this.map = new HashMap<String, Object>(d.map);
		}

		public Resume getReume() {
			Resume r = (Resume) map.get("Resume");
			return r;
		}

		public Data addResume(Resume i) {
			Data d = new Data(this);
			d.map.put("Resume", i);
			return d;
		}

		public Data addInterview(Interview i) {
			Data d = new Data(this);
			d.map.put("Interview", i);
			return d;
		}

		public Interview getInterview(){
			return (Interview) map.get("Interview");
		}

		public Data addNegotiation(Negotiation n){
			Data d = new Data(this);
			d.map.put("Negotiation", n);
			return d;
		}

		public Negotiation getNegotiation(){
			return (Negotiation) map.get("Negotiation");
		}


	}
}
