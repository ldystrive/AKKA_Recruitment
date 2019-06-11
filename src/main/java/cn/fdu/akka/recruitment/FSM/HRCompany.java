package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import akka.dispatch.sysmsg.Suspend;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.HRManager.HRManagerFsm;
import akka.actor.ActorRef;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HRCompany {

    public static enum HrState {
        Ready
    }

    public final static class Data{
        final HashMap<String, ActorRef> map;
        final HashMap<String, ActorRef> pmap;

        public Data() {
            map = new HashMap<>();
            pmap = new HashMap<>();
        }

        public Data(Data d) {
            this.map = new HashMap<>(d.map);
            this.pmap = new HashMap<>(d.pmap);
        }

        public Data addPosition(Position position, ActorRef actor, ActorRef pactor) {

            Data d = new Data(this);
            d.map.put(position.toString(), actor);
            d.pmap.put(position.toString(), pactor);
            System.out.println("Position: " + position + " hrm: " + actor);
            return d;
        }

        public boolean cantainsPosition(Position position){
            return this.map.containsKey(position.toString());
        }

        public ActorRef getActor(Position position) {
            return this.map.get(position.toString());
        }

        public ActorRef getPactor(Position position) {
            return this.pmap.get(position.toString());
        }

        public List<String> getPositions(){
            System.out.println(map.keySet());
            return new ArrayList<>(map.keySet());
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
                                final ActorRef pac = data.getPactor(resume.getPosition());
                                System.out.println("hrm:" + hrm);
                                if (hrm != null) hrm.tell(resume.setPosition(resume.getPosition().setCompanyRef(pac)), getSelf());
                                else System.out.println("HRC not find position:" + resume.getPosition());
                                return stay().using(data);
                            }
                    ).event(
                            Position.class,
                            Data.class,
                            (position, data) -> {
                                if(data.cantainsPosition(position)) { return stay().using(data);}
                                System.out.println("HRC when Ready match Position:" + position);
                                final ActorRef hrm = getContext().actorOf(Props.create(HRManagerFsm.class));
                                hrm.tell(position, getSelf());
                                System.out.println(position.getName());
                                return stay().using(data.addPosition(position, hrm, position.getCompanyRef()));
                            })
                    .event(
                            Query.class,
                            Data.class,
                           (query, data) -> {
                                System.out.println("query positions");
                                getSender().tell(data.getPositions(), getSelf());
                                return stay().using(data);
                           })
            );

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
