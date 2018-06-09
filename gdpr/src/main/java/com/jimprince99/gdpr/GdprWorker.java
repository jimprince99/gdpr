package com.jimprince99.gdpr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.framer.FramingException;

public class GdprWorker implements Runnable {
	private String inputFilename = "";
	private String outputFilename = "";
	private File inputFile;
	private File outputFile;
	private Pcap pcapInputStream = null;
	private PcapOutputStream outputStream = null;
	private String resultString = "";
	private Logger logger = null;

	/*
	 * Initialisation
	 * 
	 */
	GdprWorker(String inputFileName, Logger logger) {
		this.inputFilename = inputFileName;
		this.logger = logger;
	}

	/*
	 * filename must end in .pcap
	 */
	protected boolean filenameValid(String filename) {
		filename = filename.toLowerCase();
		return filename.endsWith(".pcap");
	}

	protected boolean fileExists(String filename) {
		File f = new File(filename);
		return f.exists();
	}

	public String getResultString() {
		return resultString;
	}

	protected Pcap getInputStream() {

//		if (!this.inputFilename.toLowerCase().endsWith(".pcap")) {
//			severeLogger("Invalid filename = " + this.inputFilename);
//
//			return null;
//		}
		this.inputFile = new File(this.inputFilename);

		try {
			this.pcapInputStream = Pcap.openStream(this.inputFile);
		} catch (FileNotFoundException e) {
			severeLogger("FileNotFound for filename=" + this.inputFilename, e);
			return null;
		} catch (IOException e) {
			severeLogger("Unable to open pcap file " + this.inputFilename, e);
			return null;
		}
		return this.pcapInputStream;
	}

	protected String getOutputFilename() {
		logger.info("infilename=" + this.inputFilename);
		String[] parts = this.inputFilename.split("\\.");
		if (parts.length != 2) {
			severeLogger("Too many . in filename");
			return null;
		}
		this.outputFilename = parts[0] + "_gdpr." + parts[1];
		logger.info("returning=" + this.outputFilename);
		return this.outputFilename;
	}

	/*
	 * generate an output stream based on the input filename
	 */
	protected PcapOutputStream getOutputStream() {
		this.outputFilename = getOutputFilename();
		if (this.outputFilename == null) {
			logger.info("output filename = null");
			return null;
		}

		this.outputFile = new File(this.outputFilename);
		if (this.outputFile == null) {
			severeLogger("Unable to open ouput stream = " + this.outputFilename);
			return null;
		}

		if (this.pcapInputStream == null) {
			this.resultString = "Input stream is null";
			severeLogger("Input stream is null");
		}
		try {
			this.outputStream = this.pcapInputStream.createOutputStream(new FileOutputStream(this.outputFile));
		} catch (IllegalArgumentException e) {
			severeLogger("Unable to open ouput stream = " + this.outputFilename + " ", e);
			return null;
		} catch (FileNotFoundException e) {
			severeLogger("Unable to find ut stream = " + this.outputFilename + " ", e);
			return null;
		}
		return this.outputStream;
	}

	public void run() {

		// get input file
		if (getInputStream() == null) {
			return;
		}

		// get output file
		if (getOutputStream() == null) {
			return;
		}

		// read packets
		GdprPacketHandler packetHandler = new GdprPacketHandler();
		packetHandler.setOutputStream(this.outputStream);
		packetHandler.setLogger(this.logger);
		
		try {
			this.pcapInputStream.loop(packetHandler);
		} catch (FramingException e) {
			severeLogger("Framing Exception ", e);
		} catch (IOException e) {
			severeLogger("Input Exception ", e);
		} finally {
			this.pcapInputStream.close();
			try {
				this.outputStream.flush();
			} catch (IOException e) {
				severeLogger("Output stream Exception ", e);
			}
			try {
				this.outputStream.close();
			} catch (IOException e) {
				severeLogger("Output stream Exception ", e);
			}
		}
		this.resultString = packetHandler.getResultString();
		return; 
	}
	
	private void severeLogger(String message, Exception e) {
		this.resultString = message;
		logger.severe(message + " " + e);
	}
	
	private void severeLogger(String message) {
		this.resultString = message;
		logger.severe(message);
	}
}
