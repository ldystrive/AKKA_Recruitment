package cn.fdu.akka.recruitment.XML;

import cn.fdu.akka.recruitment.Logger.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FilePrinter {
	private Map<String, PrintStream> printStreamMap;
	private String BaseURL;
	private Logger logger = Logger.getInstance();
	private boolean append;

	FilePrinter(String BaseURL, boolean append) {
		this.BaseURL = BaseURL;
		this.append = append;
		File file = new File(BaseURL);
		if (file.mkdirs())
			logger.Log("Create dirs:" + BaseURL);
		printStreamMap = new HashMap<>();
	}

	void print(String fileName, Object o) {
		if (!printStreamMap.containsKey(fileName)) {
			File file = new File(BaseURL + File.separator + fileName);
			if (!file.exists()) {
				try {
					if (file.createNewFile())
						logger.Log("Successfully Create File:" + file.getAbsolutePath());
					else
						logger.Warning("Failed to Create File:" + file.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				printStreamMap.put(fileName, new PrintStream(new FileOutputStream(file, append), true));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
		printStreamMap.get(fileName).print(o);
	}
}
