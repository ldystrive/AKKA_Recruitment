package cn.fdu.akka.recruitment.common;

import cn.fdu.akka.recruitment.FSM.Applicant;

public class Opinion {
    final private boolean opinion;

    public Opinion(){
        opinion = false;
    }

    public Opinion(boolean opinion) {
        this.opinion = opinion;
    }

    public boolean getOpinion() {
        return this.opinion;
    }

    public Opinion setOpinion(boolean opinion) {
        return new Opinion(opinion);
    }
}
