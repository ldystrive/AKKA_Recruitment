package cn.fdu.akka.recruitment.Events;

import cn.fdu.akka.recruitment.common.Resume;

public class ApplicantEvents {
	public class GetResume{
		private Resume resume;

		public GetResume(Resume resume) {
			this.resume = resume;
		}

		public Resume getResume() {
			return resume;
		}

		public String toString() {
			return "GetResume_" + resume;
		}
	}

}
