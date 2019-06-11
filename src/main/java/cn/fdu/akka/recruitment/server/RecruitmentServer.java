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
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.Future;

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

	public RecruitmentServer() {
		this.recruitment = null;
	}

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
	public Route createRoute() {
		return concat(
				path("positionsquery",() -> concat(
						get(() ->{
							System.out.println("get");
							final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
							CompletionStage<ArrayList<Position>> pos = ask(recruitment, new GetPositions(), timeout).thenApply(ArrayList.class::cast);
							return completeOKWithFuture(pos, Jackson.marshaller());
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
								companies.put(companyname, system.actorOf(Props.create(Company.class)));
								return complete(StatusCodes.ACCEPTED, "done");
							}}))
				)),
				path("resume", () -> concat(
						post(() -> parameter("applicant", applicant ->
							parameter("company", company -> {
								Resume resume = new Resume(applicant, company, null, null);
								if (applicants.containsKey(resume)) {
									return complete(StatusCodes.ACCEPTED, "existed");
								} else {
									applicants.put(resume.toString(), system.actorOf(Props.create(Applicant.class)));
									return complete(StatusCodes.ACCEPTED, "done");
								}
							})
						))
				)),
				path("applicantstatus", () -> concat (
						get(() -> parameter("applicant", applicant ->
								parameter("company", company -> {
									Resume resume = new Resume(applicant, company, null, null);
									if (applicants.containsKey(resume)) {

									} else return complete(StatusCodes.ACCEPTED, "not existed");
								})))
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
