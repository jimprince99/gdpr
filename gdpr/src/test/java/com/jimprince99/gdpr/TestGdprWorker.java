package com.jimprince99.gdpr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.BeforeClass;
import org.junit.Test;

import io.pkts.Pcap;

public class TestGdprWorker {
	private static FileHandler fileTxt;
	private static SimpleFormatter formatterTxt;
	private static Logger logger = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        getLogger();
	}
	
	/*
	 * test file access
	 */
	@Test
	public void test001() {
		GdprWorker worker = new GdprWorker("src/test/resources/a1-edited.pcap", logger, false);
		assertNotNull(worker);
		
		assertTrue(worker.filenameValid("src/test/resources/a1-edited.pcap"));
		assertFalse(worker.filenameValid(""));
		assertFalse(worker.filenameValid("src/test/resources/a1-edited.duff"));
	}

	/*
	 * test getters access
	 */
	@Test
	public void test002() {
		GdprWorker worker = new GdprWorker("src/test/resources/a1-edited.pcap", logger,false);
		assertNotNull(worker);
		
		assertEquals("", worker.getResultString());
	}
	
	/*
	 * test open dummy file
	 */
	@Test
	public void test003() {
		GdprWorker worker = new GdprWorker("src/test/resources/a1-edited.pcap", logger, false);
		assertNotNull(worker);
		
		assertTrue(worker.fileExists("src/test/resources/a1-edited.pcap"));
		assertFalse(worker.fileExists(""));
		assertFalse(worker.fileExists("src/test/resources/a1-edited.duff"));
	}
	
	/*
	 * test get input stream
	 */
	@Test
	public void test004() {
		GdprWorker worker = new GdprWorker("src/test/resources/a1-edited.pcap", logger, false);
		assertNotNull(worker);
		
		Pcap pcap = worker.getInputStream();
		
		assertNotNull(pcap);
	}
	
	/*
	 * test getOutputFilename
	 */
	@Test
	public void test005() {
		GdprWorker worker1 = new GdprWorker("bob", logger, false);
		assertNotNull(worker1);
		assertNull(worker1.getOutputFilename());
		
		GdprWorker worker2 = new GdprWorker("src/test/resources/a1-edited.pcap", logger, false);
		assertNotNull(worker2);
		assertNotNull(worker2.getOutputFilename());

	}
	
	/*
	 * test getInputStream
	 */
	@Test
	public void test006() {
		GdprWorker worker1 = new GdprWorker("bob", logger, false);
		assertNotNull(worker1);
		assertNull(worker1.getOutputStream());
		
		GdprWorker worker2 = new GdprWorker("src/test/resources/a1-edited.pcap", logger, false);
		assertNotNull(worker2);
		worker2.getInputStream();
		assertNotNull(worker2.getOutputStream());

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

        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
	}
	
}
