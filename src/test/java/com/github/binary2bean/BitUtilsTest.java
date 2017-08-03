package com.github.binary2bean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BitUtilsTest {

	private static final byte[] BYTES = new byte[] { 0b00101011, 0b00101011 };

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		int n1 = Utils.getBitsAsInt( BYTES, 0, 7 );
		int n2 = Utils.getBitsAsInt( BYTES, 7, 4 );
		int n3 = Utils.getBitsAsInt( BYTES, 11, 5 );

		Assert.assertEquals( 21, n1 );
		Assert.assertEquals( 9, n2 );
		Assert.assertEquals( 11, n3 );
	}

}
