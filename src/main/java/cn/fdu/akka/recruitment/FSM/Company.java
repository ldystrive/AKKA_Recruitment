package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.HR.HRFsm;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.Future;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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
					System.out.println("upload a new position " + position.getName());
					positionOfCom.tell(position, getSelf());
					return stay().using(data.addPosition(position, positionOfCom));
				})
			.event( // resume state query
				Resume.class,
				Data.class,
					(resume, data)->{
						ActorRef posRef = data.getPositionRef(resume.getPosition().getName());
						final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
						Future<Object> future = Patterns.ask(posRef, resume.getName(), timeout);
						ActorRef comapp =  (ActorRef) Await.result(future, timeout.duration());
						sender().tell(comapp, self());
						return stay();
					}
			)
		);
		initialize();
	}

	enum State{Init, Ready}


	static final class Data{
		private final ActorRef HR;
		private final HashMap<String, ActorRef> map;
		Data() {
			HR = null;
			map = new HashMap<>();
		}
		Data(ActorRef hr) {
			this.HR = hr;
			this.map = new HashMap<>();
		}
		Data(Data d) {
			this.HR = d.HR;
			this.map = d.map;
		}
		Data addActorRef(ActorRef a) {
			Data d = new Data(a);
			return d;
		}
		ActorRef getActorRef() {return HR;}

		Data addPosition(Position p, ActorRef ref){
			Data d = new Data(this);
			d.map.put(p.getName(), ref);
			return d;
		}

		ActorRef getPositionRef(String p){
			try {
				return map.get(p);
			} catch (Exception e){
				System.out.println(e);
			}
			return null;
		}
		ActorRef getPositionRef(Position p){
			try {
				return map.get(p.getName());
			} catch (Exception e){
				System.out.println(e);
			}
			return null;
		}
	}
}
