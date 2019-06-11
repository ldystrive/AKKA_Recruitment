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

	public String getComName(){return comName;}

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

	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		Position position = (Position) o;
		return name.equals(position.name) && comName.equals(position.comName);
	}

}
