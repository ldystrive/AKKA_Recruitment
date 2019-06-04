package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import javafx.geometry.Pos;
import cn.fdu.akka.recruitment.FSM.HR.HRFsm;

public class HRManager {

    public static enum _State {
        Uninit,
        Ready,
        End
    }

    public final static class Data{
        final public Position position;

        public Data(){
            this.position = new Position();
        }

        public Data(Data d) {
            this.position = new Position(d.position);
        }

        public Data(Position position) {
            this.position = position;
        }

        public Data addPosition(Position position) {
            Data d = new Data(position);
            return d;
        }
    }


    public static class HRManagerFsm extends AbstractFSM<_State, Data> {
        {
            startWith(_State.Uninit, new Data());

            when(_State.Uninit,
                    matchEvent(
                            Position.class,
                            Data.class,
                            (position, data) -> {
                                System.out.println("HRM when Uninit match Position:" + position);
                                position.getCompanyRef().tell("SUCCESS!", getSelf());
                                return goTo(_State.Ready).using(data.addPosition(position));
                            }
                    ).anyEvent(
                            (event, state) -> {
                                System.out.println("HRM Uninit, event:" + event);
                                return stay();
                            }
                    )
            );

            when(_State.Ready,
                    matchEvent(
                            Resume.class,
                            Data.class,
                            (resume, data) -> {
                                System.out.println("HRM when Ready match Resume:" + resume);
                                final ActorRef hr = getContext().actorOf(Props.create(HRFsm.class));
                                hr.tell(resume, getSelf());
                                return stay();
                            }
                    ).event(
                            Stop.class,
                            Data.class,
                            (stop, data) -> {
                                System.out.println("HRM when Ready match Stop");
                                return goTo(_State.End);
                            }
                    )
            );

            when(_State.End,
                    matchAnyEvent(
                            (event, state) -> {
                                System.out.println("HRM End");
                                return stay();
                            }
                    )
            );

            whenUnhandled(
			matchAnyEvent(
				(event, state) -> {
					System.out.println("HRM unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
					return stay();
				}));

		    initialize();
        }
    }
}
