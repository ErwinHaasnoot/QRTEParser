/**
 * 
 */
package com.qrtengine.parser.JUnit;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.ArrayList;

import com.qrtengine.parser.CSVParser;


/**
 * @author Erwin
 * Class for testing the parseLine function of the parser
 */
public class CSVParserTest {
	
	private ArrayList<String> expected;
	private String input;
	
	@Before
	public void before() {
		expected = new ArrayList<String>();
		input = "";
	}
	/*
	 * testparseCSVLine tests the parse CSV Line method. Should be able to handle different kinds of string and field delimiters, character escapes etc.
	 */
	@Test
	public void testSimpleElement() {
		String input = "oneElement";
		expected.add(input);
		assertEquals("Failed to parse the most basic test, are you kidding me?", expected, CSVParser.parseCSVLineNoException(input));
	}
	@Test
	public void testSimpleNumberElement() {
		String input = "1234567890";
		expected.add(input);
		assertEquals("Failed single element number only test", expected, CSVParser.parseCSVLineNoException(input));
	}
	@Test
	public void testSingleMixedElement() {
		String input = "1abcdefghijklmnopqrstuvwxyz1234567890";
		expected.add(input);
		assertEquals("Failed to parse the most basic test, are you kidding me?", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testEscapedSimpleElement() {
		String input = "one\\Element";
		expected.add(input);
		assertEquals("Failed single element test with escaped escape character", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testQuotedEscapeSimpleElement() {

		input = "\"o,ne\\Element\"";
		expected.add("o,ne\\Element");
		assertEquals("Failed string delimited single element test", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testTwoElements() {

		input = "two,elements";
		expected.add("two");		
		expected.add("elements");
		assertEquals("Failed simple two element test, wtf?", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testTwoElementsNumberOnly() {

		input = "234234,234234";
		expected.add("234234");		
		expected.add("234234");
		assertEquals("Failed simple number-only two element test, wtf?", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testTwoElementsMixed() {

		input = "tw2342o,elea24ments";
		expected.add("tw2342o");		
		expected.add("elea24ments");
		assertEquals("Failed simple mixed two element test, wtf?", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testTwoElementEscapedEscape() {

		input = "t\\wo,\\elements";
		expected.add("t\\wo");		
		expected.add("\\elements");
		assertEquals("Failed two element test with both elements having escaped characters", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testSingleElementWithEscapedQuote() {
		input = "\"\"";
		expected.add("");
		assertEquals("Failed single element with escaped quote test", expected, CSVParser.parseCSVLineNoException(input));
	}
	

	
	@Test
	public void testQuotedTwoElement() {

		input = "\"o,neElement\",\"element\"";
		expected.add("o,neElement");
		expected.add("element");		
		assertEquals("Failed string delimited two element test", expected, CSVParser.parseCSVLineNoException(input));
		
	}
	

	
	@Test
	public void testQuotedEscapeTwoElement() {

		input = "\"t,wo\\Element\",\"\"";
		expected.add("t,wo\\Element");
		expected.add("");
		assertEquals("Failed string delimited with escape two element test", expected, CSVParser.parseCSVLineNoException(input));
	}
	

	
	@Test
	public void testOneQuotedOneUnquoted() {
		input = "\\\"";
		expected.add(input);
		assertEquals("Failed single element with escaped quote test", expected, CSVParser.parseCSVLineNoException(input));
	}	

	
	@Test
	public void testEscapedEscape() {
		input = "\\\\";
		expected.add(input);
		assertEquals("Failed single element with escaped escape test", expected, CSVParser.parseCSVLineNoException(input));
	}
	@Test
	public void testTrailingComma() {
		input = "bllabla,";
		expected.add("bllabla");
		expected.add("");

		assertEquals("Trailing comma handled incorrectly", expected, CSVParser.parseCSVLineNoException(input));
	}
	

	@Test
	public void testMultipleTrailingComma() {
		input = "bllabla,,,,,sdf,,,";
		expected.add("bllabla");
		expected.add("");
		expected.add("");
		expected.add("");
		expected.add("");
		expected.add("sdf");
		expected.add("");
		expected.add("");
		expected.add("");

		assertEquals("Trailing comma", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testDifferentFieldDelimiter() {
		input = "adsf;asdf";
		expected.add("adsf;asdf");
		//expected.add("asdf");
		assertEquals("Different Field Delimiter", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testJSONStringParse() {
		input = "\"{\"\"TimingBlock\"\":[\"\"InitPre[OnsetTime]\"\",\"\"InitPre[OffsetTime]\"\"]}\"";
		expected.add("{\"TimingBlock\":[\"InitPre[OnsetTime]\",\"InitPre[OffsetTime]\"]}");
		//expected.add("asdf");
		assertEquals("Different JSON string parse", expected, CSVParser.parseCSVLineNoException(input));
	}
	
	@Test
	public void testDifficultString() {
		input = " ,\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/537.4\"";
		expected.add(" ");
		expected.add(input);
		//expected.add("asdf");
		assertEquals("Different JSON string parse", expected, CSVParser.parseCSVLineNoException(input));
	}
		
		
	

}





