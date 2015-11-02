package com.spikeify.cron.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssertTest {

	@Test
	public void testIsTrue()  {

		Assert.isTrue(true, "Some ex");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsTrueButFalse() {

		try {
			Assert.isTrue(false, "Bang");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Bang", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testIsNull() throws Exception {
		Assert.isNull(null, "Bla");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsNullButNot() {

		try {
			Assert.isNull(true, "Bang");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Bang", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testNotNull() throws Exception {
		Assert.notNull(true, "OK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullButNot() {

		try {
			Assert.notNull(null, "Bang");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Bang", e.getMessage());
			throw e;
		}
	}


	@Test
	public void testNotNullOrEmptyTrimmed() throws Exception {
		Assert.notNullOrEmptyTrimmed(" not empty ", "OK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotNullOrEmptyTrimmedButEmpty() throws Exception {
		try {
			Assert.notNullOrEmptyTrimmed("   ", "Bang");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Bang", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testIsFalse() throws Exception {

		Assert.isFalse(false, "Bla");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsFalseButIsNot() {

		try {
			Assert.isFalse(true, "Bang");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Bang", e.getMessage());
			throw e;
		}
	}
}