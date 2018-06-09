package com.jimprince99.gdpr;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMsisdn {

	@Test
	public void test001() {
		
		Msisdn msisdn = Msisdn.getInstance();
		
		String newMsisdn = msisdn.getMsisdn("1234");
		assertNotNull(newMsisdn);
		
		String newMsisdn2 = msisdn.getMsisdn("1234");
		assertEquals(newMsisdn, newMsisdn2);
		
		String newMsisdn3 = msisdn.getMsisdn("1235");
		assertNotEquals(newMsisdn3, newMsisdn2);
		
		String newMsisdn4 = msisdn.getMsisdn("+1235");
		if (!newMsisdn4.startsWith("+44")) {
			fail("Not in international format");
		}
		
		System.out.println(newMsisdn + ", " + newMsisdn2+ ", " + newMsisdn3 + ", " + newMsisdn4);
		assertEquals(11,newMsisdn.length());
		assertEquals(11,newMsisdn2.length());
		assertEquals(11,newMsisdn3.length());
		
		
	}

}
