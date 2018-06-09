package com.jimprince99.gdpr;

import java.util.HashMap;
import java.util.Map;

/*
 * Singleton to store original MSISDNs and the replacement MSISDNs
 */
public class Msisdn {
	private static Msisdn instance;
	private Map<String, String> storedMsisdns = new HashMap<>();

	private Msisdn() {
	}

	public static synchronized Msisdn getInstance() {
		if (instance == null) {
			instance = new Msisdn();
		}
		return instance;
	}

	public String getMsisdn(String currentMsisdn) {
		String newMsisdn = "";
		if (storedMsisdns.containsKey(currentMsisdn)) {
			newMsisdn = storedMsisdns.get(currentMsisdn);
		} else {
			newMsisdn = getNewNumber();
			storedMsisdns.put(currentMsisdn, newMsisdn);
		}

		if (currentMsisdn.startsWith("+")) {
			newMsisdn = "+44" + newMsisdn.replaceFirst("0", "");
		}
		return newMsisdn;
	}

	private String getNewNumber() {
		return "07700" + (int) (Math.random() * 999998 + 1);
	}
}
