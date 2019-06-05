package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import cn.fdu.akka.recruitment.common.*;
import scala.App;

public class HR {

    public static enum _State {
        Uninit,
        Interview,
        Negotiation,
        Offer,
        End
    }

    public final static class Data {
        final private Resume resume;
        final private boolean applicantReady;
        final private boolean companyReady;

        public Data() {
            this.resume = null;
            this.applicantReady = false;
            this.companyReady = false;
        }

        public Data(Resume resume, boolean applicantReady, boolean companyReady) {
            this.resume = resume;
            this.applicantReady = applicantReady;
            this.companyReady = companyReady;
        }

        public boolean getApplicantReady(){
            return this.applicantReady;
        }

        public boolean getCompanyReady(){
            return this.companyReady;
        }

        public Data setApplicantReady(boolean applicantReady){
            return new Data(this.resume, applicantReady, this.companyReady);
        }

        public Data setCompanyReady(boolean companyReady) {
            return new Data(this.resume, this.applicantReady, companyReady);
        }

        public Data addResume(Resume resume) {
            return new Data(resume, false, false);
        }

        public Data init() {
            return new Data(this.resume, false, false);
        }

        public Resume getResume() {
            return this.resume;
        }
    }

    public static class HRFsm extends AbstractFSM<_State, Data> {
        {
            startWith(_State.Uninit, new Data());

            when(_State.Uninit,
                    matchEvent(
                            Resume.class,
                            Data.class,
                            (resume, data) -> {
                                System.out.println("HR Init with Resume:" + resume);
                                resume.getApplicantRef().tell(new Interview(resume), getSelf());
                                resume.getPosition().getCompanyRef().tell(new Interview(resume), getSelf());
                                return goTo(_State.Interview).using(data.addResume(resume));
                            }
                    ).anyEvent(
                            (event, state) -> {
                                System.out.println("HR Uninit, event:" + event);
                                return stay();
                            }
                    )
            );

            when(_State.Interview,
                    matchEvent(
                            ApplicantOpinion.class,
                            Data.class,
                            (opinion, data) -> {
                                System.out.println("Interview Applicant Opinion is " + opinion.getOpinion());
                                if (opinion.getOpinion()) {
                                    if (data.getCompanyReady()) {
                                        return goTo(_State.Negotiation).using(new Data().addResume(data.getResume()));
                                    } else {
                                        return stay().using(data.setApplicantReady(true));
                                    }
                                } else {
                                    Resume resume = data.getResume();
                                    resume.getApplicantRef().tell(new Interview(resume), getSelf());
                                    resume.getPosition().getCompanyRef().tell(new Interview(resume), getSelf());
                                    return stay().using(data.init());
                                }
                            }
                    ).event(
                            CompanyOpinion.class,
                            Data.class,
                            (opinion, data) -> {
                                System.out.println("Interview Company Opinion is " + opinion.getOpinion());
                                if (opinion.getOpinion()) {
                                    if (data.getApplicantReady()) {
                                        return goTo(_State.Negotiation).using(new Data().addResume(data.getResume()));
                                    } else {
                                        return stay().using(data.setCompanyReady(true));
                                    }
                                } else {
                                    Resume resume = data.getResume();
                                    resume.getApplicantRef().tell(new Interview(resume), getSelf());
                                    resume.getPosition().getCompanyRef().tell(new Interview(resume), getSelf());
                                    return stay().using(data.init());
                                }
                            }
                    )
            );

            when(_State.Negotiation,
                    matchEvent(
                            ApplicantOpinion.class,
                            Data.class,
                            (opinion, data) -> {
                                System.out.println("Negotiation Applicant Opinion is " + opinion);
                                if (opinion.getOpinion()) {
                                    if (data.getCompanyReady()) {
                                        return goTo(_State.Offer).using(new Data().addResume(data.getResume()));
                                    } else {
                                        return stay().using(data.setApplicantReady(true));
                                    }
                                } else {
                                    Resume resume = data.getResume();
                                    resume.getApplicantRef().tell(new Interview(resume), getSelf());
                                    resume.getPosition().getCompanyRef().tell(new Interview(resume), getSelf());
                                    return stay().using(data.init());
                                }
                            }
                    ).event(
                            CompanyOpinion.class,
                            Data.class,
                            (opinion, data) -> {
                                System.out.println("Negotiation Company Opinion is " + opinion);
                                if (opinion.getOpinion()) {
                                    if (data.getApplicantReady()) {
                                        return goTo(_State.Offer).using(new Data().addResume(data.getResume()));
                                    } else {
                                        return stay().using(data.setCompanyReady(true));
                                    }
                                } else {
                                    Resume resume = data.getResume();
                                    resume.getApplicantRef().tell(new Interview(resume), getSelf());
                                    resume.getPosition().getCompanyRef().tell(new Interview(resume), getSelf());
                                    return stay().using(data.init());
                                }
                            }
                    )
            );

            when(_State.Offer,
                    matchEvent(
                            Offer.class,
                            Data.class,
                            (offer, data) -> {
                                Resume resume = offer.getResume();
                                resume.getApplicantRef().tell(offer, getSelf());
                                return goTo(_State.End);
                            }
                    )
            );

            when(_State.End,
                    matchAnyEvent(
                            (event, state) -> {
                                System.out.println("HR End");
                                return stay();
                            }
                    )
            );

            whenUnhandled(
	    		matchAnyEvent(
	    			(event, state) -> {
	    				System.out.println("unhandled, event:" + event + " stateName:" + stateName() + " state:" + state);
	    				return stay();
	    			}));

		    initialize();
        }
    }
}
