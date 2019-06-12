package cn.fdu.akka.recruitment;

import cn.fdu.akka.recruitment.Logger.Logger;
import cn.fdu.akka.recruitment.XML.SCXmlParser;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

public class SCXMLTest extends JUnitSuite {
	@Test
	public void testSCXML(){
		Logger logger=Logger.getInstance();
		try {
			String dir="F:/Git/AKKA_Recruitment/test/";
			SCXmlParser scXmlParser=new SCXmlParser(dir+"Top.xml");
			scXmlParser.init();
			scXmlParser=new SCXmlParser(dir+"P.xml");
			scXmlParser.init();
			scXmlParser=new SCXmlParser(dir+"VM.xml");
			scXmlParser.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			logger.Log("Test End");
		}
	}
}
