package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.HRManager.HRManagerFsm;
import akka.actor.ActorRef;
import javafx.geometry.Pos;

import java.util.HashMap;

public class HRCompany {

    public static enum HrState {
        Ready
    }

    public final static class Data{
        final HashMap<String, ActorRef> map;

        public Data() {
            map = new HashMap<String, ActorRef>();
        }

        public Data(Data d) {
            this.map = new HashMap<String, ActorRef>(d.map);
        }

        public Data addPosition(Position position, ActorRef actor) {
            Data d = new Data(this);
            d.map.put(position.toString(), actor);
            System.out.println("Position: " + position + " hrm: " + actor);
            return d;
        }

        public ActorRef getActor(Position position) {
            return this.map.get(position.toString());
        }
    }


    public static class HRCompanyFsm extends AbstractFSM<HrState, Data>{
        {
            startWith(HrState.Ready, new Data());

            when(HrState.Ready,
                    matchEvent(
                            Resume.class,
                            Data.class,
                            (resume, data) -> {
                                System.out.println("HRC when Ready match Resume:" + resume);
                                final ActorRef hrm = data.getActor(resume.getPosition());
                                System.out.println("hrm:" + hrm);
                                if (hrm != null) hrm.tell(resume, getSelf());
                                else System.out.println("HRC not find position:" + resume.getPosition());
                                return stay();
                            }
                    ).event(
                            Position.class,
                            Data.class,
                            (position, data) -> {
                                System.out.println("HRC when Ready match Position:" + position);
                                final ActorRef hrm = getContext().actorOf(Props.create(HRManagerFsm.class));
                                hrm.tell(position, getSelf());
                                return stay().using(data.addPosition(position, hrm));
                            }
                    ));

            whenUnhandled(
                    matchAnyEvent(
                            (event, state) -> {
                                System.out.println("HRC unhandled, event:" + event + " stateName:" + stateName() + "state:" + state);
                                return stay();
                            }
                    )
            );

            initialize();
        }
    }
}
