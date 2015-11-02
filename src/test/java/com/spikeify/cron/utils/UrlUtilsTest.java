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
		assertEquals("http://iddiction.com", UrlUtils.getFullUrl("http://iddiction.com", null));
		assertEquals("http://iddiction.com", UrlUtils.getFullUrl("http://iddiction.com/", null));

		assertEquals("http://iddiction.com/", UrlUtils.getFullUrl("http://iddiction.com", "/"));
		assertEquals("http://iddiction.com/test", UrlUtils.getFullUrl("http://iddiction.com", "test"));
		assertEquals("http://iddiction.com/test", UrlUtils.getFullUrl("http://iddiction.com", "/test"));

		assertEquals("http://iddiction.com/", UrlUtils.getFullUrl("http://iddiction.com/", "/"));
		assertEquals("http://iddiction.com/test", UrlUtils.getFullUrl("http://iddiction.com/", "test"));
		assertEquals("http://iddiction.com/test", UrlUtils.getFullUrl("http://iddiction.com/", "/test"));
	}

	@Test
	public void testGetFullUrl_2()
	{
		assertEquals("http://iddiction.com", UrlUtils.getFullUrl("http://iddiction.com", "http://iddiction.com"));
		assertEquals("http://iddiction.com", UrlUtils.getFullUrl("http://iddiction.com/", "http://iddiction.com"));

		assertEquals("http://iddiction.com/test.html", UrlUtils.getFullUrl("http://iddiction.com", "http://iddiction.com/test.html"));
		assertEquals("http://iddiction.com/test/test.html", UrlUtils.getFullUrl("http://iddiction.com", "http://iddiction.com/test/test.html"));
	}
}