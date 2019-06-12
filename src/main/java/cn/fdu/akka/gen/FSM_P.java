package cn.fdu.akka.gen;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Map;
import java.util.HashMap;

public class FSM_P {
	public static enum Gen_State {
		state_Uinit0,
		state_Uinit1,
		state_Init,
		state_Wait,
		state_WaitTea,
		state_WaitCoffee
	}

	public final static class Gen_Data {
		final private Map<String, String> map;
		final private Map<String, ActorRef> refMap;

		public Gen_Data() {
			map = new HashMap<String, String>();
			refMap = new HashMap<String, ActorRef>();
			map.put("next", "Tea");
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

	public static class PFSM extends AbstractFSM<Gen_State, Gen_Data> {

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
							Event_buy.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data.getRef("VM").tell(new Event_coin(""), getSelf());

									flag = true;
									return goTo(Gen_State.state_Wait).using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_Wait,
					matchEvent(
							Event_gotcoin.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag && (data.get("next").equals("Tea"))) {
									data.getRef("VM").tell(new Event_ChooseTea(""), getSelf());

									flag = true;
									return goTo(Gen_State.state_WaitTea).using(data);
								}
								if (!flag && (data.get("next").equals("Coffee"))) {
									data.getRef("VM").tell(new Event_ChooseCoffee(""), getSelf());

									flag = true;
									return goTo(Gen_State.state_WaitCoffee).using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_WaitTea,
					matchEvent(
							Event_Tea.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data.getRef("Top").tell(new Event_gotTea(""), getSelf());

									data = data.set("next", "Coffee");

									flag = true;
									return goTo(Gen_State.state_Init).using(data);
								}
								return stay();
							}
					)
			);

			when(Gen_State.state_WaitCoffee,
					matchEvent(
							Event_Coffee.class,
							Gen_Data.class,
							(event, data) -> {
								boolean flag = false;
								if (!flag) {
									data.getRef("Top").tell(new Event_gotCoffee(""), getSelf());

									data = data.set("next", "Tea");

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
								System.out.println("In P FSM: unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
								return stay();
							}
					)
			);

			initialize();
		}
	}
}