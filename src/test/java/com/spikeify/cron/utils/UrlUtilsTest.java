package com.spikeify.cron.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UrlUtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void testGetFullUrlWithoutRootUrl()
	{
		UrlUtils.getFullUrl(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFullUrlWithoutRootUrl_2()
	{
		UrlUtils.getFullUrl("", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFullUrlWithoutRootUrl_3()
	{
		UrlUtils.getFullUrl("  ", null);
	}

	@Test
	public void testGetFullUrl()
	{
		assertEquals("http://spiekeify.com", UrlUtils.getFullUrl("http://spiekeify.com", null));
		assertEquals("http://spiekeify.com", UrlUtils.getFullUrl("http://spiekeify.com/", null));

		assertEquals("http://spiekeify.com/", UrlUtils.getFullUrl("http://spiekeify.com", "/"));
		assertEquals("http://spiekeify.com/test", UrlUtils.getFullUrl("http://spiekeify.com", "test"));
		assertEquals("http://spiekeify.com/test", UrlUtils.getFullUrl("http://spiekeify.com", "/test"));

		assertEquals("http://spiekeify.com/", UrlUtils.getFullUrl("http://spiekeify.com/", "/"));
		assertEquals("http://spiekeify.com/test", UrlUtils.getFullUrl("http://spiekeify.com/", "test"));
		assertEquals("http://spiekeify.com/test", UrlUtils.getFullUrl("http://spiekeify.com/", "/test"));
	}

	@Test
	public void testGetFullUrl_2()
	{
		assertEquals("http://spiekeify.com", UrlUtils.getFullUrl("http://spiekeify.com", "http://spiekeify.com"));
		assertEquals("http://spiekeify.com", UrlUtils.getFullUrl("http://spiekeify.com/", "http://spiekeify.com"));

		assertEquals("http://spiekeify.com/test.html", UrlUtils.getFullUrl("http://spiekeify.com", "http://spiekeify.com/test.html"));
		assertEquals("http://spiekeify.com/test/test.html", UrlUtils.getFullUrl("http://spiekeify.com", "http://spiekeify.com/test/test.html"));
	}
}