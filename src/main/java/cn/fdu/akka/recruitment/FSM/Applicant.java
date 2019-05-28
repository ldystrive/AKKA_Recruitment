package cn.fdu.akka.recruitment.FSM;

import akka.actor.AbstractFSM;
import cn.fdu.akka.recruitment.Interface.ApplicantDataInterface;
import cn.fdu.akka.recruitment.common.*;
import cn.fdu.akka.recruitment.State.ApplicantState;
import cn.fdu.akka.recruitment.Data.ApplicantData;

public class Applicant extends AbstractFSM<ApplicantState, ApplicantDataInterface>{

	{
		startWith(ApplicantState.Init, ApplicantData.Uninitialized);

		when(
			ApplicantState.Init,
			matchEvent(
				Resume.class,
				ApplicantData.class,
					(resume, uninitialized) -> {
						System.out.println("when Init match Resume");
						return stay();
					}

			)
		);

	}

}

