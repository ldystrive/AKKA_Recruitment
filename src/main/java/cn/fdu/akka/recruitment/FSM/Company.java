package cn.fdu.akka.recruitment.FSM;


import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import javafx.geometry.Pos;
import cn.fdu.akka.recruitment.FSM.HR.HRFsm;

import static cn.fdu.akka.recruitment.FSM.Company.State;
import static cn.fdu.akka.recruitment.FSM.Company.State.*;
import static cn.fdu.akka.recruitment.FSM.Company.Data;

public class Company extends AbstractFSM<State, Data>{

	{
		startWith(Ready, new Data());
		when(Ready,
			matchEvent(
				Position.class,
				Data.class,
				(position, data) -> {
					final ActorRef positionOfCom = getContext().actorOf(Props.create(ComPos.class));
					System.out.println("upload a new position");
					positionOfCom.tell(position, getSelf());
					return stay();
				}
			)
		);
		initialize();
	}

	enum State{Ready}

	static final class Data{

	}
}
