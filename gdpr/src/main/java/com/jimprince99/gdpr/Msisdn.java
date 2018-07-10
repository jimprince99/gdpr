package com.jimprince99.gdpr;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
	
	public String getMsisdn(String currentMsisdn, boolean partial) {
		String newMsisdn = "";

		if (storedMsisdns.containsKey(currentMsisdn)) {
			newMsisdn = storedMsisdns.get(currentMsisdn);
		} else {
			newMsisdn = getNewNumber(currentMsisdn, partial);
			storedMsisdns.put(currentMsisdn, newMsisdn);
		}
		// add in the original +44
		if (currentMsisdn.startsWith("+")) {
			newMsisdn = currentMsisdn.substring(0, 3) + newMsisdn.replaceFirst("0", "");
		}

		return newMsisdn;
	}

	public String getMsisdn(String currentMsisdn) {
		return getMsisdn(currentMsisdn, false);
	}

	private String getNewNumber(String origMsisdn, boolean partial) {
		if (partial) {
			String substring = origMsisdn.substring(Math.max(origMsisdn.length() - 3, 0));
			return "07700" + ((int) (Math.random() * 998 + 1)) + substring;
		}else {
			return "07700" + (int) (Math.random() * 999998 + 1);
		}
	}
}
