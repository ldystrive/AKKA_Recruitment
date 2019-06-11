package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.HR.HRFsm;

import static cn.fdu.akka.recruitment.FSM.Company.State;
import static cn.fdu.akka.recruitment.FSM.Company.State.*;
import static cn.fdu.akka.recruitment.FSM.Company.Data;

public class Company extends AbstractFSM<State, Data>{

	{
		startWith(Init, new Data());

		when(
			Init,
			matchEvent(
				ActorRef.class,
				Data.class,
				(actorRef, data) -> {
					return goTo(Ready).using(data.addActorRef(actorRef));
				}
			)
		);

		when(Ready,
			matchEvent(
				Position.class,
				Data.class,
				(position, data) -> {
					final ActorRef positionOfCom = getContext().actorOf(Props.create(ComPos.class));
					positionOfCom.tell(data.getActorRef(), getSelf());
					System.out.println("upload a new position");
					positionOfCom.tell(position, getSelf());
					
					return stay();
				}
			)
		);
		initialize();
	}

	enum State{Init, Ready}


	static final class Data{
		private final ActorRef HR;
		Data() {HR = null;}
		Data(ActorRef hr) {
			this.HR = hr;
		}
		Data(Data d) {
			this.HR = d.HR;
		}
		Data addActorRef(ActorRef a) {
			Data d = new Data(a);
			return d;
		}
		ActorRef getActorRef() {return HR;}

	}
}
