package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.HRManager.HRManagerFsm;
import akka.actor.ActorRef;

import static cn.fdu.akka.recruitment.FSM.ComPos.Data;
import static cn.fdu.akka.recruitment.FSM.ComPos.State;
import static cn.fdu.akka.recruitment.FSM.ComPos.State.*;


import java.util.HashMap;

public class ComPos extends AbstractFSM<State, Data>{


	{
		startWith(Init, new Data());
		when(
			LoadPosition,
			matchEvent(
				Position.class,
				Data.class,
    			((position, data) -> {
				    //final ActorRef ComInterview = getContext().actorOf(Props.create(ComApplicant.class));
				    ActorRef hr = data.getHRRef();
				    Position position1 = new Position(position.getName(), position.getComName(), getSelf());
					hr.tell(position1, getSelf());
				    return stay().using(data.addPosition(position));
			    }))
		);

		when(
			Ready,
			matchEvent(
				Resume.class,
				Data.class,
				(resume, data)->{
					final ActorRef ComInterview = getContext().actorOf(Props.create(ComApplicant.class));
					Position p = resume.getPosition();
					Position position = new Position(p.getName(), p.getComName(), ComInterview);
					Resume resume1 = new Resume(resume.getName(), position, resume.getHrRef(), resume.getApplicantRef());
					resume.getHrRef().tell(resume1, self());
					return stay().using(data.addResume(resume1.getName(), ComInterview));
				}
			)
			.event( // resume query
				String.class,
				Data.class,
				(resumeName, data)->{
					sender().tell(data.getResumeRef(resumeName), self());
					return stay();
				}
			)
		);

		when(
			Init,
			matchEvent(
				ActorRef.class,
				Data.class,
				(actorRef, data) -> {
					return goTo(Ready).using(data.addHRRef(actorRef));
				}
			)
		);
		initialize();
	}

	enum State{ Init, LoadPosition, Ready }
	static final class Data{
		private final HashMap<String, ActorRef> map;
		private Position position;

		public Data() {
			map = new HashMap<String, ActorRef>();
		}

		public Data(Data d) {
			this.map = new HashMap<String, ActorRef>(d.map);
		}

		public Data addPosition(Position pos) {
			position = pos;
			return this;
		}

		public Data addHRRef(ActorRef actor) {
			Data d = new Data(this);
			d.map.put("__HRRef__", actor);
			return d;
		}

		public ActorRef getHRRef() {
			return map.get("__HRRef__");
		}

		public Data addResume(String name, ActorRef a){
			Data d = new Data(this);
			d.map.put(name, a);
			return d;
		}

		public ActorRef getResumeRef(String name){
			return map.get(name);
		}
	}
}
