package cn.fdu.akka.recruitment.Logger;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private static class InnerLogger {
		private static Logger LOG = new Logger();
	}

	private static final String logDir = "F:/Git/AKKA_Recruitment/log";
	private boolean hasnotice = false;
	private PrintStream printStream;

	private Logger() {
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd_hh.mm.ss");
		final String logFileName = logDir + File.separator + "log-" + ft.format(new Date()) + ".txt";
		File file = new File(logDir);
		file.mkdirs();
		file = new File(logFileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			printStream = new PrintStream(new FileOutputStream(logFileName, true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Log("Logger init successfully");
		LogSep();
	}

	public static Logger getInstance() {
		return InnerLogger.LOG;
	}

	public void TranslateError(String message) {
		System.out.println("[ERROR] An error occurred during Translation:" + message);
		printStream.println("[ERROR] An error occurred during Translation:" + message);
		throw new AssertionError();
	}

	public void TranslateWarning(String message) {
		System.out.println("[Warning]While Translation:" + message);
		printStream.println("[Warning]While Translation:" + message);
	}

	public void TranslateLog(String message) {
		System.out.println("[Log]While Translation:" + message);
		printStream.println("[Warning]While Translation:" + message);
	}

	public void Log(String message) {
		System.out.println("[Log]" + message);
		printStream.println("[Log]" + message);
	}

	public void LogSep() {
		Log("----------------------------------------");
	}

	public void Warning(String message) {
		System.out.println("[Warning]" + message);
		printStream.println("[Warning]" + message);
	}

	public void Error(String message) {
		System.out.println("[Error]" + message);
		printStream.println("[Error]" + message);
		throw new AssertionError();
	}

	public void Notice(String message) {
		if (hasnotice) {
			Error("There must be one Notice in the system!!!");
			throw new AssertionError();
		}
		hasnotice = true;
		for (int i = 0; i <= 10; i++) {
			System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
			printStream.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
		}

		System.out.println("[Notice]" + message);
		printStream.println("[Notice]" + message);
		for (int i = 0; i <= 10; i++) {
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			printStream.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}

	}
}
