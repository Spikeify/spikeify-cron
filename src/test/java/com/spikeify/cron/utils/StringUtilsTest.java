package com.spikeify.cron.utils;

import net.trajano.commons.testing.UtilityClassTestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;


public class StringUtilsTest {

	@Test
	public void testDefinition() {

		UtilityClassTestUtil.assertUtilityClassWellDefined(StringUtils.class);
	}

	@Test
	public void testEquals() {

		Assert.assertTrue(StringUtils.equals(null, null));
		Assert.assertFalse(StringUtils.equals(null, ""));
		Assert.assertFalse(StringUtils.equals("", null));
		Assert.assertTrue(StringUtils.equals("", ""));

		Assert.assertTrue(StringUtils.equals("A", "A"));
		Assert.assertFalse(StringUtils.equals("A", "a"));
	}

	@Test
	public void testEqualsIgnoreCase() {

		Assert.assertTrue(StringUtils.equals(null, null, true));
		Assert.assertFalse(StringUtils.equals(null, "", true));
		Assert.assertFalse(StringUtils.equals("", null, true));
		Assert.assertTrue(StringUtils.equals("", "", true));

		Assert.assertTrue(StringUtils.equals("A", "A", true));
		Assert.assertTrue(StringUtils.equals("A", "a", true));
	}

	@Test
	public void testCompare() {

		Assert.assertEquals(0, StringUtils.compare(null, null));
		Assert.assertEquals(1, StringUtils.compare("", null));
		Assert.assertEquals(-1, StringUtils.compare(null, ""));
		Assert.assertEquals(0, StringUtils.compare("", ""));
		Assert.assertEquals(0, StringUtils.compare("a", "a"));
		Assert.assertEquals(1, StringUtils.compare("aa", "a"));
	}

	@Test
	public void testTrim() {

		Assert.assertNull(StringUtils.trim(null));
		Assert.assertEquals("", StringUtils.trim(""));
		Assert.assertEquals("a", StringUtils.trim(" a "));
	}

	@Test
	public void testTrimToNull() {

		Assert.assertNull(StringUtils.trimToNull(null));
		Assert.assertNull(StringUtils.trimToNull(""));
		Assert.assertEquals("a", StringUtils.trimToNull(" a "));
	}

	@Test
	public void testRemoveDoubleSpaces() {

		Assert.assertEquals(null, StringUtils.trimDoubleSpaces(null));
		Assert.assertEquals("", StringUtils.trimDoubleSpaces(""));
		Assert.assertEquals("", StringUtils.trimDoubleSpaces(" "));
		Assert.assertEquals("plast. vreć. 1x500 m", StringUtils.trimDoubleSpaces(" plast.   vreć.   1x500   m  "));
		Assert.assertEquals("", StringUtils.trimDoubleSpaces(" "));
		Assert.assertEquals("a", StringUtils.trimDoubleSpaces("  a   "));
		Assert.assertEquals("a a", StringUtils.trimDoubleSpaces("  a a  "));
		Assert.assertEquals("a b c", StringUtils.trimDoubleSpaces(" a    b     c  "));
	}

	@Test
	public void testRemoveSpaces() {

		Assert.assertEquals(null, StringUtils.trimInner(null));
		Assert.assertEquals("", StringUtils.trimInner(""));
		Assert.assertEquals("", StringUtils.trimInner(" "));
		Assert.assertEquals("a", StringUtils.trimInner("  a   "));
		Assert.assertEquals("aa", StringUtils.trimInner("  a a  "));
		Assert.assertEquals("abc", StringUtils.trimInner(" a    b     c  "));
	}

	@Test
	public void testTrimEnd() {

		Assert.assertEquals(null, StringUtils.trimEnd(null));
		Assert.assertEquals("", StringUtils.trimEnd(""));
		Assert.assertEquals("", StringUtils.trimEnd(" "));
		Assert.assertEquals("  a", StringUtils.trimEnd("  a   "));
		Assert.assertEquals("  a a", StringUtils.trimEnd("  a a  "));
		Assert.assertEquals(" a    b     c", StringUtils.trimEnd(" a    b     c  "));
	}

	@Test
	public void testTrimStart() {

		Assert.assertEquals(null, StringUtils.trimStart(null));
		Assert.assertEquals("", StringUtils.trimStart(""));
		Assert.assertEquals("", StringUtils.trimStart(" "));
		Assert.assertEquals("a   ", StringUtils.trimStart("  a   "));
		Assert.assertEquals("a a  ", StringUtils.trimStart("  a a  "));
		Assert.assertEquals("a    b     c  ", StringUtils.trimStart("              a    b     c  "));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoin_Fail() {

		try {
			Set<String> set = null;
			StringUtils.join(set, null);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals("Missing separator!", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testJoin() {

		Set<String> set = new HashSet<>();
		Assert.assertEquals("", StringUtils.join(set, ","));

		set.add("A");
		Assert.assertEquals("A", StringUtils.join(set, ","));

		set.add("B");
		Assert.assertEquals("A,B", StringUtils.join(set, ","));

		Assert.assertEquals("A, B", StringUtils.join(set, ", "));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoin_Fail2() {

		try {
			List<String> list = null;
			StringUtils.join(list, null);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals("Missing separator!", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testJoin2() {

		List<String> list = new ArrayList<>();
		Assert.assertEquals("", StringUtils.join(list, ","));

		list.add("A");
		Assert.assertEquals("A", StringUtils.join(list, ","));

		list.add("B");
		Assert.assertEquals("A,B", StringUtils.join(list, ","));

		Assert.assertEquals("A, B", StringUtils.join(list, ", "));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoin_Fail3() {

		try {
			String[] array = null;
			StringUtils.join(array, null);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals("Missing separator!", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testJoin3() {

		String[] array = new String[] {};
		Assert.assertEquals("", StringUtils.join(array, ","));

		array = new String[] {"A"};
		Assert.assertEquals("A", StringUtils.join(array, ","));

		array = new String[] {"A", "B"};
		Assert.assertEquals("A,B", StringUtils.join(array, ","));

		Assert.assertEquals("A, B", StringUtils.join(array, ", "));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoin_Fail4() {

		try {
			HashMap<String, String> set = null;
			StringUtils.join(set, null);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals("Missing separator!", e.getMessage());
			throw e;
		}
	}

	@Test
	public void testJoin4() {

		HashMap<String, String> set = new LinkedHashMap<>();
		Assert.assertEquals("", StringUtils.join(set, ","));

		set.put("A", "1");
		Assert.assertEquals("A=1", StringUtils.join(set, ","));

		set.put("B", "2");
		Assert.assertEquals("A=1,B=2", StringUtils.join(set, ","));

		Assert.assertEquals("A=1, B=2", StringUtils.join(set, ", "));
	}

	@Test
	public void getWordsFromTextTest() {

		List<String> output = StringUtils.getWords(null);
		Assert.assertEquals(0, output.size());

		output = StringUtils.getWords("");
		Assert.assertEquals(0, output.size());

		output = StringUtils.getWords("         ");
		Assert.assertEquals(0, output.size());

		output = StringUtils.getWords(" abra kadabra");
		Assert.assertEquals(2, output.size());
		Assert.assertEquals("abra", output.get(0));
		Assert.assertEquals("kadabra", output.get(1));


		output = StringUtils.getWords(" abra, ::(12313) kadabra!");
		Assert.assertEquals(2, output.size());
		Assert.assertEquals("abra", output.get(0));
		Assert.assertEquals("kadabra", output.get(1));

		output = StringUtils.getWords(" rdeče češnje rastejo na želvi");
		Assert.assertEquals(5, output.size());
		Assert.assertEquals("rdeče", output.get(0));
		Assert.assertEquals("češnje", output.get(1));
		Assert.assertEquals("rastejo", output.get(2));
		Assert.assertEquals("na", output.get(3));
		Assert.assertEquals("želvi", output.get(4));
	}

	@Test
	public void trimAllTest() {

		Assert.assertNull(StringUtils.trimAll(null, null));
		Assert.assertNull(StringUtils.trimAll(null, ""));
		Assert.assertNull(StringUtils.trimAll(null, "X"));

		Assert.assertEquals("", StringUtils.trimAll("", null));
		Assert.assertEquals("A", StringUtils.trimAll("A", null));

		Assert.assertEquals("aa", StringUtils.trimAll("AaAaA", "A"));
		Assert.assertEquals("AaAaA", StringUtils.trimAll("A-a-Aa-A", "-"));
	}

	@Test
	public void trimTextDownTest() {

		Assert.assertEquals("T", StringUtils.trimTextDown("Text", 1));

		Assert.assertEquals("Text", StringUtils.trimTextDown("Text", 4));
		Assert.assertEquals("Text", StringUtils.trimTextDown("Text", 5));

		Assert.assertEquals("Text", StringUtils.trimTextDown("Text to be trimmed down", 5));
		Assert.assertEquals("Text to be trimmed", StringUtils.trimTextDown("Text to be trimmed down", 22));
		Assert.assertEquals("Text to be trimmed down", StringUtils.trimTextDown("Text to be trimmed down", 25));
	}

	@Test
	public void toStringOrNullTest() {

		Assert.assertNull(StringUtils.toStringOrNull(null));
		Assert.assertEquals("", StringUtils.toStringOrNull(""));

		String test = "test";
		Assert.assertEquals("test", StringUtils.toStringOrNull(test));
	}

	@Test
	public void getListOfCharsTest() {

		List<String> list = StringUtils.asListOfChars(null);
		Assert.assertEquals(0, list.size());

		list = StringUtils.asListOfChars("a");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("a", list.get(0));

		list = StringUtils.asListOfChars("abc");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("a", list.get(0));
		Assert.assertEquals("b", list.get(1));
		Assert.assertEquals("c", list.get(2));
	}

	@Test
	public void isWordTest() {

		Assert.assertFalse(StringUtils.isWord(null));
		Assert.assertFalse(StringUtils.isWord(""));
		Assert.assertFalse(StringUtils.isWord("  "));
		Assert.assertFalse(StringUtils.isWord(" . "));
		Assert.assertFalse(StringUtils.isWord(" , "));
		Assert.assertFalse(StringUtils.isWord("test,me"));
		Assert.assertFalse(StringUtils.isWord("   !pussy-cat!  "));

		Assert.assertTrue(StringUtils.isWord("test"));
		Assert.assertTrue(StringUtils.isWord("   me  "));
		Assert.assertTrue(StringUtils.isWord("   me,  "));
		Assert.assertTrue(StringUtils.isWord("   me!  "));
		Assert.assertTrue(StringUtils.isWord("   !HELLO!  "));
		Assert.assertTrue(StringUtils.isWord("   Češka  "));
	}
}