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
					// Always agree
					getSender().tell(new CompanyOpinion(true), getSelf());
					return goTo(WaitingForNegociate).using(data.addInterview(interview));

				})
		);
		when(
			WaitingForNegociate,
			matchEvent(
				Negotiation.class,
				Data.class,
				(negotiation, data) -> {
					getSender().tell(new CompanyOpinion(true), getSelf());
					Resume resume = data.getInterview().getResume();
					Offer offer = new Offer(resume);
					getSender().tell(offer, getSelf());
					return goTo(End);
				}
			)
		);
		initialize();
	}

	enum State{
		WaitingForInterview,
		WaitingForNegociate,
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

	}


}
