package cn.fdu.akka.recruitment.XML;

import cn.fdu.akka.recruitment.Logger.Logger;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class SCXmlParser {
	private Node dataModel;
	private Node eventModel;
	private Node scxml;
	private Node initialState;
	private List<Node> state;
	private List<Node> data;
	private List<Node> event;
	private Document document;
	private FilePrinter filePrinter = new FilePrinter("F:/Git/AKKA_Recruitment/src/main/java/cn/fdu/akka/gen", false);
	private String xmlFileName;
	private String fsmFileName;

	private void initParser(String fileName) throws Exception {
		File file = new File(fileName);
		xmlFileName = file.getName();
		xmlFileName = xmlFileName.substring(0, xmlFileName.lastIndexOf('.'));
		fsmFileName = "FSM_" + xmlFileName + ".java";
		Logger logger = Logger.getInstance();
		logger.Log("Init scxml parser with file: " + file.getName());
		FileInputStream xmlInputStream = new FileInputStream(fileName);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		document = documentBuilder.parse(xmlInputStream);

	}

	public SCXmlParser(String fileName) {
		try {
			initParser(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void initSCXml() {
		Logger logger = Logger.getInstance();
		scxml = document.getDocumentElement();
		NodeList nodeList = scxml.getChildNodes();
		state = new ArrayList<>();
		data = new ArrayList<>();
		event = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			switch (nodeList.item(i).getNodeName()) {
				case "state":
					state.add(nodeList.item(i));
					break;
				case "eventmodel":
					if (eventModel == null)
						eventModel = nodeList.item(i);
					else {
						logger.TranslateError("Multiple definition of eventmodel");
					}
					break;
				case "datamodel":
					if (dataModel == null)
						dataModel = nodeList.item(i);
					else {
						logger.TranslateError("Multiple definition of datamodel");
					}
					break;
			}
		}
		if (eventModel == null)
			logger.TranslateError("No definition of eventmodel");
		if (dataModel == null)
			logger.TranslateError("No definition of eventmodel");
		if (state.isEmpty())
			logger.TranslateError("No definition of state");
		String sInitialState = ((Element) scxml).getAttribute("initialstate");
		for (Node node : state)
			if (((Element) node).getAttribute("id").equals(sInitialState)) {
				initialState = node;
			}
		nodeList = dataModel.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals("data")) {
				data.add(nodeList.item(i));
			}
		nodeList = eventModel.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals("event")) {
				event.add(nodeList.item(i));
			}
		if (initialState == null)
			logger.TranslateError("No such initial state found");

		logger.Log("Fsm name: " + xmlFileName);
		logger.Log("Initial state:" + ((Element) initialState).getAttribute("id"));
		logger.Log("State count: " + state.size());
		logger.Log("Data count: " + data.size());
		logger.Log("Event count: " + event.size());
		logger.Log("Parse scxml successfully");
	}

	private void genFSMHead() {
		Logger logger = Logger.getInstance();
		logger.Log("generate head..");
		filePrinter.print(fsmFileName,
				"package cn.fdu.akka.gen;\n\n" +
				"import akka.actor.AbstractFSM;\n" +
						"import akka.actor.ActorRef;\n" +
						"import akka.actor.Props;\n\n" +
						"import java.util.Map;\n" +
						"import java.util.HashMap;\n\n"+
						"public class " + "FSM_" + xmlFileName + " {\n"
		);
		logger.Log("done");
	}

	private void genFSMState() {
		Logger logger = Logger.getInstance();
		logger.Log("generate state..");
		filePrinter.print(fsmFileName, "\tpublic static enum Gen_State {\n");
		for (int i = 0; i < state.size(); i++) {
			filePrinter.print(fsmFileName,
					"\t\tstate_" + ((Element) state.get(i)).getAttribute("id") + (i == state.size() - 1 ? "" : ",") + "\n"
			);
		}
		filePrinter.print(fsmFileName, "\t}\n");
		logger.Log("done");
	}

	private void genFSMData() {
		Logger logger = Logger.getInstance();
		logger.Log("generate data..");
		filePrinter.print(fsmFileName, "\n\tpublic final static class Gen_Data {\n" +
				"\t\tfinal private Map<String, String> map;\n" +
				"\t\tfinal private Map<String, ActorRef> refMap;\n" +
				"\n" +
				"\t\tpublic Gen_Data() {\n" +
				"\t\t\tmap = new HashMap<String, String>();\n" +
				"\t\t\trefMap = new HashMap<String, ActorRef>();\n"
		);
		NodeList nodeList = dataModel.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals("data")) {
				Element element = (Element) nodeList.item(i);
				filePrinter.print(fsmFileName, "\t\t\tmap.put(\"" + element.getAttribute("id") + "\", \"" + element.getAttribute("expr") + "\");\n");
			}
		filePrinter.print(fsmFileName, "\t\t}\n" +
				"\n" +
				"\t\tpublic Gen_Data(Map<String, String> mp, Map<String, ActorRef> rm) {\n" +
				"\t\t\tmap = new HashMap<String, String>(mp);\n" +
				"\t\t\trefMap = new HashMap<String, ActorRef>(rm);\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tpublic String get(String k) {\n" +
				"\t\t\treturn map.get(k);\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tpublic Gen_Data set(String k, String v) {\n" +
				"\t\t\tGen_Data d = new Gen_Data(map, refMap);\n" +
				"\t\t\td.map.put(k, v);\n" +
				"\t\t\treturn d;\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tpublic ActorRef getRef(String k) {\n" +
				"\t\t\treturn refMap.get(k);\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tpublic Gen_Data setRef(String k, ActorRef v) {\n" +
				"\t\t\tGen_Data d = new Gen_Data(map, refMap);\n" +
				"\t\t\td.refMap.put(k, v);\n" +
				"\t\t\treturn d;\n" +
				"\t\t}\n" +
				"\t}\n"

		);
		logger.Log("done");
	}

	private void genExpr(Node trans) {
		genExpr(trans, "");
	}

	private void genExpr(Node trans, String preindent) {
		String indent = preindent + "\t\t\t\t\t\t\t\t";
		NodeList nodeList = trans.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			switch (nodeList.item(i).getNodeName()) {
				//((Element)nodeList.item(i))
				case "log":
					filePrinter.print(fsmFileName, indent + "System.out.println(\"" + ((Element) nodeList.item(i)).getAttribute("expr") + "\");\n\n");
					break;
				case "show":
					filePrinter.print(fsmFileName, indent + "System.out.println(data.get(\"" + ((Element) nodeList.item(i)).getAttribute("id") + "\"));\n\n");
					break;
				case "new":
					filePrinter.print(fsmFileName, indent + "data = data.setRef(\"" + ((Element) nodeList.item(i)).getAttribute("name") +
							"\", getContext().actorOf(Props.create(FSM_" +((Element) nodeList.item(i)).getAttribute("class")+
							"."+ ((Element) nodeList.item(i)).getAttribute("class") + "FSM.class)));\n\n");
					break;
				case "send":
					filePrinter.print(fsmFileName, indent + "data.getRef(\"" + ((Element) nodeList.item(i)).getAttribute("name") + "\").tell(" + "new ActorEvent(" +
							(((Element) nodeList.item(i)).getAttribute("ref").equals("#self")
									? "getSelf()" : "data.getRef(\"" + ((Element) nodeList.item(i)).getAttribute("ref") + "\")") + ", \"" +
							(((Element) nodeList.item(i)).hasAttribute("refname")
									? ((Element) nodeList.item(i)).getAttribute("refname") : ((Element) nodeList.item(i)).getAttribute("ref")) + "\"), getSelf());\n\n");
					break;
				case "tell":
					filePrinter.print(fsmFileName, indent + "data.getRef(\"" + ((Element) nodeList.item(i)).getAttribute("name") +
							"\").tell(" + "new Event_" + ((Element) nodeList.item(i)).getAttribute("event") + "(\"" +
							((Element) nodeList.item(i)).getAttribute("expr") + "\"), getSelf());\n\n");
					break;
				case "assign":
					filePrinter.print(fsmFileName, indent + "data = data.set(\"" + ((Element) nodeList.item(i)).getAttribute("id") + "\", \"" +
							((Element) nodeList.item(i)).getAttribute("expr") + "\");\n\n");
					break;
				case "interface":
					filePrinter.print(fsmFileName,
							indent + "//@interface " + ((Element) nodeList.item(i)).getAttribute("name") + "\n" +
									indent + "{\n" +
									indent + "\t//todo：\n" +
									indent + "\t//write code here\n" +
									indent + "}\n\n"
					);
					break;

			}
		}
	}

	private void genFSMTrans() {
		Logger logger = Logger.getInstance();
		filePrinter.print(fsmFileName, "\n\tpublic static class " + xmlFileName + "FSM extends AbstractFSM<Gen_State, Gen_Data> {\n\n\t\t{\n");
		filePrinter.print(fsmFileName, "\t\t\tstartWith(Gen_State.state_" + ((Element) initialState).getAttribute("id") + ", new Gen_Data());\n\n");
		for (Node s : state) {
			NodeList nodeList = s.getChildNodes();
			String statename = ((Element) s).getAttribute("id");
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i).getNodeName().equals("receive")) {
					Element trans = (Element) nodeList.item(i);
					String target = trans.getAttribute("target");
					filePrinter.print(fsmFileName,
							"\t\t\twhen(Gen_State.state_" + statename + ",\n" +
									"\t\t\t\t\tmatchEvent(\n" +
									"\t\t\t\t\t\t\tActorEvent.class,\n" +
									"\t\t\t\t\t\t\tGen_Data.class,\n" +
									"\t\t\t\t\t\t\t(event, data) -> {\n"
					);
					filePrinter.print(fsmFileName, "\t\t\t\t\t\t\t\tdata = data.setRef(event.name, event.ref);\n\n");
					genExpr(nodeList.item(i));
					if (!target.equals("#self")) {
						filePrinter.print(fsmFileName,
								"\t\t\t\t\t\t\t\treturn goTo(Gen_State.state_" + target + ").using(data);\n" +
										"\t\t\t\t\t\t\t}\n" +
										"\t\t\t\t\t)\n" +
										"\t\t\t);\n\n"
						);
					} else {
						filePrinter.print(fsmFileName,
								"\t\t\t\t\t\t\t\treturn stay().using(data);\n" +
										"\t\t\t\t\t\t\t}\n" +
										"\t\t\t\t\t)\n" +
										"\t\t\t);\n\n"
						);
					}
				}
			}
			Map<String, List<Element>> map = new HashMap<>();

			for (int i = 0; i < nodeList.getLength(); i++)
				if (nodeList.item(i).getNodeName().equals("transition")) {
					Element trans = (Element) nodeList.item(i);
					String event = trans.getAttribute("event");
					String target = trans.getAttribute("target");
					map.computeIfAbsent(event, k -> new ArrayList<>());
					map.get(event).add(trans);
				}
			for (Map.Entry<String, List<Element>> entry : map.entrySet()) {
				String event = entry.getKey();
				List<Element> list = entry.getValue();
				filePrinter.print(fsmFileName,
						"\t\t\twhen(Gen_State.state_" + statename + ",\n" +
								"\t\t\t\t\tmatchEvent(\n" +
								"\t\t\t\t\t\t\tEvent_" + event + ".class,\n" +
								"\t\t\t\t\t\t\tGen_Data.class,\n" +
								"\t\t\t\t\t\t\t(event, data) -> {\n"
				);
				filePrinter.print(fsmFileName, "\t\t\t\t\t\t\t\tboolean flag = false;\n");
				for (Element element : list) {
					String target = element.getAttribute("target");
					if (element.hasAttribute("cond")) {
						String cond = element.getAttribute("cond");
						cond = cond.replaceAll("\\$\\$", "data.getRef(\"");
						cond = cond.replaceAll("##", "\")");
						cond = cond.replaceAll("\\$", "data.get(\"");
						cond = cond.replaceAll("#", "\")");
						cond = cond.replace('\'', '\"');
						filePrinter.print(fsmFileName,
								"\t\t\t\t\t\t\t\tif (!flag && (" + cond + ")) {\n"
						);
					} else
						filePrinter.print(fsmFileName, "\t\t\t\t\t\t\t\tif (!flag) {\n");
					genExpr(element, "\t");
					if (!target.equals("#self")) {
						filePrinter.print(fsmFileName,
								"\t\t\t\t\t\t\t\t\tflag = true;\n" +
										"\t\t\t\t\t\t\t\t\treturn goTo(Gen_State.state_" + target + ").using(data);\n" +
										"\t\t\t\t\t\t\t\t}\n"

						);
					} else {
						filePrinter.print(fsmFileName,
								"\t\t\t\t\t\t\t\t\tflag = true;\n" +
										"\t\t\t\t\t\t\t\t\treturn stay().using(data);\n" +
										"\t\t\t\t\t\t\t\t}\n"
						);
					}

				}
				filePrinter.print(fsmFileName, "\t\t\t\t\t\t\t\treturn stay();\n");
				filePrinter.print(fsmFileName, "\t\t\t\t\t\t\t}\n" +
						"\t\t\t\t\t)\n" +
						"\t\t\t);\n\n");

			}
		}
		filePrinter.print(fsmFileName, "\t\t\twhenUnhandled(\n" +
				"\t\t\t\t\tmatchAnyEvent(\n" +
				"\t\t\t\t\t\t\t(event, state) -> {\n" +
				"\t\t\t\t\t\t\t\tSystem.out.println(\"In "+xmlFileName +" FSM: unhandled, event:\" + event + \" stateName:\" + stateName() + \" state:\" + state);\n" +
				"\t\t\t\t\t\t\t\treturn stay();\n" +
				"\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t)\n" +
				"\t\t\t);\n\n"
		);
		filePrinter.print(fsmFileName, "\t\t\tinitialize();\n\t\t}\n\t}\n");
	}

	private void genFSMTail() {
		Logger logger = Logger.getInstance();
		logger.Log("generate tail..");
		filePrinter.print(fsmFileName, "}");
		logger.Log("done");
	}

	private void genEvent() {
		Logger logger = Logger.getInstance();
		NodeList nodeList = eventModel.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals("event")) {
				String name = ((Element) nodeList.item(i)).getAttribute("name");
				logger.Log("generate event: " + name);
				filePrinter.print("Event_" + name + ".java",
						"package cn.fdu.akka.gen;\n\n" +

								"public class " + "Event_" + name + " {\n" +
						"\tString message;\n\n" +
						"\tpublic Event_" + name + "(String s) {\n" +
						"\t\tthis.message = s;\n" +
						"\t}\n" +
						"}"
				);
				logger.Log("done");
			}
		filePrinter.print("ActorEvent.java",
				"package cn.fdu.akka.gen;\n\n" +
						"import akka.actor.ActorRef;\n" +
				"\n" +
				"public class ActorEvent {\n" +
				"\tString name;\n" +
				"\tActorRef ref;\n\n" +
				"\tpublic ActorEvent(ActorRef ref, String name) {\n" +
				"\t\tthis.ref = ref;\n" +
				"\t\tthis.name = name;" +
				"\t}\n" +
				"}"
		);
	}


	public void init() {
		initSCXml();
		Logger logger = Logger.getInstance();
		logger.LogSep();
		logger.Log("Start to generate file: " + fsmFileName);
		genFSMHead();
		genFSMState();
		genFSMData();
		genFSMTrans();
		genFSMTail();
		logger.Log("Finish generating file: " + fsmFileName);
		logger.LogSep();
		logger.Log("Start to generate All the Event");
		genEvent();
		logger.Log("Finish generating All the Event");
		logger.LogSep();
	}
}
