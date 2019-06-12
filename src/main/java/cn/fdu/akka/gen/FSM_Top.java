package cn.fdu.akka.gen;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Map;
import java.util.HashMap;

public class FSM_Top {
	public static enum Gen_State {
		state_Uinit,
		state_Init
	}

	public final static class Gen_Data {
		final private Map<String, String> map;
		final private Map<String, ActorRef> refMap;

		public Gen_Data() {
			map = new HashMap<String, String>();
			refMap = new HashMap<String, ActorRef>();
		}

		public Gen_Data(Map<String, String> mp, Map<String, ActorRef> rm) {
			map = new HashMap<String, String>(mp);
			refMap = new HashMap<String, ActorRef>(rm);
		}

		public String get(String k) {
			return map.get(k);
		}

		public Gen_Data set(String k, String v) {
			Gen_Data d = new Gen_Data(map, refMap);
			d.map.put(k, v);
			return d;
		}

		public ActorRef getRef(String k) {
			return refMap.get(k);
		}

		public Gen_Data setRef(String k, ActorRef v) {
			Gen_Data d = new Gen_Data(map, refMap);
			d.refMap.put(k, v);
			return d;
		}
	}

	public static class TopFSM extends AbstractFSM<Gen_State, Gen_Data> {

		{
			startWith(Gen_State.state_Uinit, new Gen_Data());

			when(Gen_State.state_Uinit,
					matchEvent(
							Event_init.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data = data.setRef("P", getContext().actorOf(Props.create(FSM_P.PFSM.class)));

									data = data.setRef("VM", getContext().actorOf(Props.create(FSM_VM.VMFSM.class)));

									data.getRef("VM").tell(new ActorEvent(data.getRef("P"), "P"), getSelf());

									data.getRef("P").tell(new ActorEvent(data.getRef("VM"), "VM"), getSelf());

									data.getRef("VM").tell(new ActorEvent(getSelf(), "Top"), getSelf());

									data.getRef("P").tell(new ActorEvent(getSelf(), "Top"), getSelf());

									flag = true;
									return goTo(Gen_State.state_Init).using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_Init,
					matchEvent(
							Event_gotTea.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									System.out.println("Tea!!");

									flag = true;
									return stay().using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_Init,
					matchEvent(
							Event_gotCoffee.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									System.out.println("Coffee!!");

									flag = true;
									return stay().using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_Init,
					matchEvent(
							Event_buy.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									System.out.println("Buy!!");

									data.getRef("P").tell(new Event_buy(""), getSelf());

									flag = true;
									return stay().using(data);
								}
								return stay();
							}
					)
			);

			whenUnhandled(
					matchAnyEvent(
							(event, state) -> {
								System.out.println("In Top FSM: unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
								return stay();
							}
					)
			);

			initialize();
		}
	}
}