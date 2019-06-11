package cn.fdu.akka.recruitment.common;

import akka.actor.ActorRef;
import javafx.geometry.Pos;

public class Position {

    private String name;
    private String comName;
    private final ActorRef companyRef;

    public ActorRef getCompanyRef(){
        return this.companyRef;
    }

    public Position () {
        this.companyRef = null;
    }

    public Position(String name, String comName, ActorRef companyRef) {
        this.name = name;
        this.comName = comName;
        this.companyRef = companyRef;
    }

    public String getName() {
        return name;
    }

    public Position(String name, String comName) {
        this.name = name;
        this.comName = comName;
        this.companyRef = null;
    }

    public Position(Position position) {
        this.name = position.name;
        this.comName = position.comName;
        this.companyRef = position.companyRef;
    }

    public String toString() {
        return name + '_' + comName;
    }
}
