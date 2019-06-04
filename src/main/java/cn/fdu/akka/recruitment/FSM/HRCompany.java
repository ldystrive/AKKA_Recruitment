package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.HRManager.HRManagerFsm;
import akka.actor.ActorRef;

public class HRCompany {

    public static enum HrState {
        Ready
    }

    public static interface Data{}

    public static class ResumenData implements Data{
        public Resume resume;
        public ResumenData(Resume resume) {
            this.resume = resume;
        }
    }

    public static class PositionData implements Data{
        public Position position;
        public PositionData(Position position) {
            this.position = position;
        }
    }

    public static enum Uninitialized implements Data {
        Uninitialized
    }

    public static class HRCompanyFsm extends AbstractFSM<HrState, Data>{
        {
            startWith(HrState.Ready, Uninitialized.Uninitialized);

            when(HrState.Ready,
                    matchEvent(
                            Resume.class,
                            Uninitialized.class,
                            (resume, uninitialized) -> {
                                System.out.println("when Ready match Resume:" + resume);
                                return stay();
                            }
                    ).event(
                            Position.class,
                            Uninitialized.class,
                            (position, uninitialized) -> {
                                System.out.println("when Ready match Position:" + position);
                                final ActorRef hrm = getContext().actorOf(Props.create(HRManagerFsm.class));
                                hrm.tell(position, getSelf());
                                return stay();
                            }
                    ));

            whenUnhandled(
                    matchAnyEvent(
                            (event, state) -> {
                                System.out.println("unhandled, event:" + event + " stateName:" + stateName() + "state:" + state);
                                return stay();
                            }
                    )
            );

            initialize();
        }
    }
}
