package cn.fdu.akka.recruitment.server;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import cn.fdu.akka.recruitment.FSM.Company;
import cn.fdu.akka.recruitment.FSM.HRCompany;
import cn.fdu.akka.recruitment.common.Position;
import cn.fdu.akka.recruitment.common.Query;
import cn.fdu.akka.recruitment.common.Resume;
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.Future;
import scala.concurrent.Await;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static akka.pattern.PatternsCS.ask;

import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.FSM.*;

public class RecruitmentServer extends AllDirectives{


	private final ActorRef recruitment;
	private static final List<ActorRef> hr = new ArrayList<>();
	private static final HashMap<String, ActorRef> companies = new HashMap<>();
	private static final HashMap<String, ActorRef> applicants = new HashMap<>();
	private static final ActorSystem system = ActorSystem.create("routes");

	public static void main(String [] args) throws Exception{

		final Http http = Http.get(system);
		final ActorMaterializer materializer = ActorMaterializer.create(system);

		//In order to access all directives we need an instance where the routes are define.
		RecruitmentServer app = new RecruitmentServer(system);

		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
		final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
				ConnectHttp.toHost("0.0.0.0", 8080), materializer);

		System.out.println("Server online at http://0.0.0.0:8080/\nPress RETURN to stop...");
		System.in.read(); // let it run until user presses return

		binding
				.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
				.thenAccept(unbound -> system.terminate()); // and shutdown when done
	}

	private static ActorRef getComApplicant(String comName, String posName, String resumeName){
		final ActorRef com = companies.get(comName);
		Position p = new Position(posName, comName, null);
		Resume resume = new Resume(resumeName, p, null, null);
		final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
		Future<Object> future = Patterns.ask(com, resume, timeout);
		ActorRef comApplicant = null;
		try {
			comApplicant = (ActorRef) Await.result(future, timeout.duration());
		} catch (Exception e){
			System.out.println(e);
		}
		return comApplicant;

	}

	private static String FSMStateQuery(ActorRef fsm){
		final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
		Future<Object> future = Patterns.ask(fsm, new Query(), timeout);
		String state;
		try {
			state = (String) Await.result(future, timeout.duration());
		}catch (Exception e){
			System.out.println(e);
			state = "false";
		}
		return state;
	}


	public Route createRoute() {
		return concat(
				path("positionsquery",() -> concat(
						get(() ->{
							System.out.println("get");
							final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
							Future<Object> future = Patterns.ask(hr.get(0), new Query(), timeout);
							List<Position> poslist = new ArrayList<>();
							try {
								poslist = (ArrayList<Position>)(Await.result(future, timeout.duration()));
							} catch (Exception e) {
								System.out.println(e);
							}
							StringBuilder str = new StringBuilder();
							for (Position a : poslist){
								str.append(a.toString() + '\n');
							}
							return complete(StatusCodes.ACCEPTED, str.toString());
						})
				)),
				path("companyquery",()->concat(
						get(()->{
							return complete(StatusCodes.OK, companies.keySet(), Jackson.marshaller());
						})
				)),
				path("newcompany", ()->concat(
						post(()->parameter( "companyname", companyname->{
							if(companies.containsKey(companyname)){
								return complete(StatusCodes.ACCEPTED, "existed");
							} else{
								final ActorRef com = system.actorOf(Props.create(Company.class));
								com.tell(hr.get(0), ActorRef.noSender());
								companies.put(companyname, com);
								return complete(StatusCodes.ACCEPTED, "done");
							}}))
				)),
				path("newposition", ()->(
						post(()->parameter("comname", comname->
								parameter("pos", pos->{
									if(companies.containsKey(comname)){
										final ActorRef com = companies.get(comname);
										Position p = new Position(pos, comname);
										com.tell(p, ActorRef.noSender());
										return complete(StatusCodes.ACCEPTED, "done");
									}else{
										return complete(StatusCodes.ACCEPTED, comname + " not found");
									}})))
				)),
				path("companyResumeQuery", ()->(
						post(()->parameter("resumeName",resumeName->
								parameter("positionName",positionName->
										parameter("comName", comName->{
											if(companies.containsKey(comName)){
												ActorRef comApplicant = getComApplicant(comName, positionName, resumeName);
												String state = FSMStateQuery(comApplicant);
												return complete(StatusCodes.OK, state);
											} else{
												return complete(StatusCodes.ACCEPTED, "not found");
											}}))))
				)),
				path("companyOpinion", ()->(
						post(()->parameter("resumeName",resumeName->
								parameter("positionName",positionName->
										parameter("comName", comName->
										parameter("opinion", opinion->{
											if(companies.containsKey(comName)){
												ActorRef comApplicant = getComApplicant(comName, positionName, resumeName);
												Boolean op = opinion.equals("1");
												comApplicant.tell(op, ActorRef.noSender());
												String state = FSMStateQuery(comApplicant);
												return complete(StatusCodes.OK, state);
											} else{
												return complete(StatusCodes.ACCEPTED, "not found");
											}}))))))),
				path("applicantopinion", ()->(
						post(() -> parameter("applicant", applicant ->
								parameter("company", company ->
										parameter("position", position ->
												parameter("opinion", opinion -> {
													final Resume resume = new Resume(applicant, new Position(position, company, null), hr.get(0), null);
													if (applicants.containsKey(resume.toString())) {
														ActorRef appRef = applicants.get(resume.toString());
														Boolean op = opinion.equals("1");
														appRef.tell(op, ActorRef.noSender());
														String state = FSMStateQuery(appRef);
														return complete(StatusCodes.OK, state);
													} else {
														return complete(StatusCodes.ACCEPTED, "no found");
													}
												})))))
						)),
				path("resumestate", () -> concat(
						post(() -> parameter("applicant", applicant ->
								parameter("company", company ->
										parameter("postition", position -> {
											final Resume resume = new Resume(applicant, new Position(position, company, null), hr.get(0), null);
											if (applicants.containsKey(resume.toString())) {
												ActorRef appRef = applicants.get(resume.toString());
												String state = FSMStateQuery(appRef);
												return complete(StatusCodes.OK, state);
											} else {
												return complete(StatusCodes.ACCEPTED, "not found");
											}
										}))))
				)),
				path("resume", () -> concat(
						post(() -> parameter("applicant", applicant ->
							parameter("company", company ->
								parameter("position", position -> {
									final Resume resume = new Resume(applicant, new Position(position, company, null), hr.get(0), null);
									if(applicants.containsKey(resume.toString())) {
										return complete(StatusCodes.ACCEPTED, "existed");
									} else {
										final ActorRef appl = system.actorOf(Props.create(Applicant.class));
										applicants.put(resume.toString(), appl);
										appl.tell(resume.setAppRef(appl), ActorRef.noSender());
										return complete(StatusCodes.ACCEPTED, "done");
									}
								}))))
				))
		);
	}



	private RecruitmentServer(final ActorSystem system){
		recruitment = system.actorOf(Recruitment.props(), "recruitment");
		hr.add(system.actorOf(Props.create(HRCompany.HRCompanyFsm.class)));
	}

	static class GetPositions{}


	static class Recruitment extends AbstractActor{

		private final LoggingAdapter log = Logging.getLogger(context().system(), this);


		static Props props() {
			return Props.create(Recruitment.class);
		}

		@Override
		public Receive createReceive(){
			return receiveBuilder()
					.match(GetPositions.class, m->{
						final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
						Future<Object> future = Patterns.ask(hr.get(0), new Query(), timeout);
						sender().tell(future, self());
					})
					.matchAny(o->log.info("Invalid message"))
					.build();
		}
	}
}
