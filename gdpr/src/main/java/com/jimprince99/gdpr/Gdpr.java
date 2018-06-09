package com.jimprince99.gdpr;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Gdpr {

	private static String filename = null;
	private static FileHandler fileTxt;
	private static SimpleFormatter formatterTxt;
	private static Logger logger = null;

	public static void main(String[] args) {
        getLogger();

		getArgs(args);
		logger.info("This filename : " + filename);
		GdprWorker gdpr = new GdprWorker(filename, logger);

		Thread gdrpThread = new Thread(gdpr, "gdpr1");
		gdrpThread.start();
		
		try {
			gdrpThread.join();
		} catch (InterruptedException e) {
		}
		
		String result = gdpr.getResultString();
		if (isEmpty(result) != null) {
			logger.info(result);
			System.exit(-1);
		} else {
			System.exit(0);
		}
	}
	
	private static void getLogger() {
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        try {
			fileTxt = new FileHandler("gdpr,log");
		} catch (SecurityException e) {
			System.err.println("SecurityException: Unable to open logger");
		} catch (IOException e) {
			System.err.println("IOException: Unable to open logger");
		}
        Handler[] handlers = logger.getHandlers();
        for(Handler handler : handlers) {
            logger.removeHandler(handler);
        }

        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
	}

	private static void getArgs(String[] args) {
        logger.setLevel(Level.SEVERE);

		if (args.length > 2) {
			commandOptions();
		}
		for (String arg : args) {
			switch (arg) {
			case "-v":
		        logger.setLevel(Level.WARNING);
		        System.out.println("logger= + logger.getLevel()");
				break;
				
			case "-vv":
		        logger.setLevel(Level.INFO);
		        System.out.println("setting logging to finest");
				System.out.println("logger = " + logger.toString());

				break;

			default:
				filename = arg;
			}
		}
	}

	private static void commandOptions() {
		System.err.println("grdp : -v <filename>");
		System.err.println("       -v verbose logging");
		System.err.println("       -vv very verbose logging");
	}
	
	private static Predicate<String> isEmpty(String result) {
		return p -> p != null && p != "";
	}
}
