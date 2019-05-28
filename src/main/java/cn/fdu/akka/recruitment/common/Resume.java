package cn.fdu.akka.recruitment.common;

public final class Resume {

	private String name;
	private String position;

	public Resume(String name, String position) {
		this.name = name;
		this.position = position;
	}

	public String toString() {
		return name + '_' + position;
	}
}
