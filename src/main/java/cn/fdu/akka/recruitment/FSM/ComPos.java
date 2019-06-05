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
			Ready,
			matchEvent(
				Position.class,
				Data.class,
    			((position, data) -> {
				    final ActorRef ComInterview = getContext().actorOf(Props.create(ComApplicant.class));
				    ActorRef hr = data.getHRRef();
				    Position p = new Position(position.getName(), hr, ComInterview);
				    hr.tell(p, getSelf());
				    return stay().using(data.addPosition(position, ComInterview));
			    }))
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

	enum State{ Init, Ready }
	static final class Data{
		private final HashMap<String, ActorRef> map;

		public Data() {
			map = new HashMap<String, ActorRef>();
		}

		public Data(Data d) {
			this.map = new HashMap<String, ActorRef>(d.map);
		}

		public Data addPosition(Position pos, ActorRef actor) {
			Data d = new Data(this);
			d.map.put(pos.toString(), actor);
			return d;
		}

		public Data addHRRef(ActorRef actor) {
			Data d = new Data(this);
			d.map.put("HRRef", actor);
			return d;
		}

		public ActorRef getHRRef() {
			return map.get("HRRef");
		}

		public ActorRef getActor(Position pos) {
			return this.map.get(pos.toString());
		}

	}

}
