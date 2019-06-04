package cn.fdu.akka.recruitment.common;

import akka.actor.ActorRef;

public class Position {

    private String name;
    private final ActorRef hrRef;
    private final ActorRef companyRef;

    public Position(String name, ActorRef hrRef, ActorRef companyRef) {
        this.name = name;
        this.hrRef = hrRef;
        this.companyRef = companyRef;
    }

    public Position(String name) {
        this.name = name;
        this.hrRef = null;
        this.companyRef = null;
    }

    public String toString() {
        return name;
    }
}
