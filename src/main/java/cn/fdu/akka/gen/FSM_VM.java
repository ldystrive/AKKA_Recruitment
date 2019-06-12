package cn.fdu.akka.gen;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Map;
import java.util.HashMap;

public class FSM_VM {
	public static enum Gen_State {
		state_Uinit0,
		state_Uinit1,
		state_Init,
		state_Choose
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

	public static class VMFSM extends AbstractFSM<Gen_State, Gen_Data> {

		{
			startWith(Gen_State.state_Uinit0, new Gen_Data());

			when(Gen_State.state_Uinit0,
					matchEvent(
							ActorEvent.class,
							Gen_Data.class,
							(event, data) -> {
								data = data.setRef(event.name, event.ref);

								return goTo(Gen_State.state_Uinit1).using(data);
							}
					)
			);

			when(Gen_State.state_Uinit1,
					matchEvent(
							ActorEvent.class,
							Gen_Data.class,
							(event, data) -> {
								data = data.setRef(event.name, event.ref);

								return goTo(Gen_State.state_Init).using(data);
							}
					)
			);

			when(Gen_State.state_Init,
					matchEvent(
							Event_coin.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data.getRef("P").tell(new Event_gotcoin(""), getSelf());

									flag = true;
									return goTo(Gen_State.state_Choose).using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_Choose,
					matchEvent(
							Event_ChooseCoffee.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data.getRef("P").tell(new Event_Coffee(""), getSelf());

									flag = true;
									return goTo(Gen_State.state_Init).using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_Choose,
					matchEvent(
							Event_ChooseTea.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data.getRef("P").tell(new Event_Tea(""), getSelf());

									flag = true;
									return goTo(Gen_State.state_Init).using(data);
								}
								return stay();
							}
					)
			);

			whenUnhandled(
					matchAnyEvent(
							(event, state) -> {
								System.out.println("In VM FSM: unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
								return stay();
							}
					)
			);

			initialize();
		}
	}
}