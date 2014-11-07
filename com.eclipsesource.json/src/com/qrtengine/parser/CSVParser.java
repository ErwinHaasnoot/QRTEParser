package com.qrtengine.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CSVParser {
	
	private String source;
	private BufferedReader sourceBuffer;
	private FileInputStream sourceStream;
	private ArrayList<String> columns;
	private int currentLine;
	
	public CSVParser(String sourcePath, boolean columnLine) throws Exception {
		this.source = sourcePath;
		init();		
		
	}
	
	private void init() throws Exception {		
		//Create bufferedReader, throws FNF exception if file does not exist
		initBuffer(this.source);
		
		//Set iterator int to current line = 0
		this.currentLine = 0;
		//Reads first line of given file to set columns
		this.columns = CSVParser.parseCSVLine(this.getNextLine());
	}
	
	private void initBuffer(String sourcePath) throws FileNotFoundException {
		// TODO Auto-generated method stub
		this.sourceStream = new FileInputStream(sourcePath);
		this.sourceBuffer = new BufferedReader(new InputStreamReader(this.sourceStream));
	}
	
	public void resetIterator() {
		try {
			init();			
		} catch (Exception e) {
			System.out.println("lolol");
		}
	}

	private String getNextLine() throws IOException{
		String out = this.sourceBuffer.readLine();
		//System.out.println("Read line: " + out);
		this.currentLine += 1;
		return out;
	}
	//Parses String that was input as though it's a csv string.
	
	public static ArrayList<String> parseCSVLine(String csv, char fieldDelimiter, char stringDelimiter) throws IOException {
		ArrayList<String> out = new ArrayList<String>();
		char c = 0;
		char escapeCharacter = '\\';
		String currentString = "";
		
		boolean stringOpen = false;
		for(int i = 0; i < csv.length(); i++) {
			c = csv.charAt(i);
			//System.out.println("Found char: " + c + ", ignoreNext? " + ignoreNext + ", string open? " + stringOpen);
			if( ! stringOpen && c == fieldDelimiter) {
				//Found field delimiter, close string
				
				out.add(currentString);
				currentString = "";
				//System.out.println("Ended field");
			} else if (c == stringDelimiter) {
				//Found quote, need to determine whether it is a closing quote or simply an escape quote
				if(stringOpen) {
					int countQuotes = 1;
					String quotes = "";
					
					while(i+countQuotes < csv.length() && csv.charAt(i+countQuotes) == stringDelimiter) {
						countQuotes += 1;
						if(countQuotes % 2 == 0) {
							quotes += stringDelimiter;
						}
					}
					if(countQuotes % 2 == 1) {
						//stringDelimiter
						stringOpen = false;
					}

					currentString += quotes;
					
					i += countQuotes - 1;
				} else {
					stringOpen = true;
				}
			} else if ( c == escapeCharacter) {
				i += 1;
				if(i < csv.length()) {
					currentString = currentString + c + csv.charAt(i);					
				} else {
					throw new IndexOutOfBoundsException("No character was escaped");
				}
			} else {
				currentString += c;
			}
		}
		
		if(csv.length() > 0) {
			out.add(currentString);
		}
		
		//Check whether proper CSV string was given
		if(stringOpen) {
			throw new IOException();
		}
		//System.out.println(csv + " : " + out.size());
		return out;
	}
	
	//Overloaded Method for csv, defaults to field delimiter ',' and string delimiter '"'
	public static ArrayList<String> parseCSVLine(String csv) throws IOException {
		return parseCSVLine(csv, ',', '"');
	}
	
	//Overloaded Method for csv, defaults to string delimiter '"'
	public static ArrayList<String> parseCSVLine(String csv, char fieldDelimiter) throws IOException {
		return parseCSVLine(csv, fieldDelimiter, '"');
	}
	
	public static ArrayList<String> parseCSVLineNoException(String csv) {
		try {
			return parseCSVLine(csv, ',', '"');
			
		} catch(Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

	
	//Parse CSV Method, Iterator style return Map<String,String>
	public HashMap<String, String> nextLine() throws IOException {
		HashMap<String, String> out = new HashMap<String, String>();
		String next = this.getNextLine();
		if(next != null) {
			ArrayList<String> current = CSVParser.parseCSVLine(next);
			
			if(current.size() != this.columns.size()) {
				throw new IOException("Columns and Values Row not same size");
			}
			
			for(int i = 0; i < current.size(); i++) {
				out.put(this.columns.get(i),current.get(i));
			}
			return out;
		} else {
			return null;
		}
	}
	
	public ArrayList<String> getColumns() {
		return this.columns;
	}	
	
	//write csv, string array?
	
	public static boolean writeLine(String target, ArrayList<String> columns, HashMap<String, String> values, boolean addColumns) throws IOException {
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(target, !addColumns)));
		if(addColumns) {
			out.println(CSVParser.array2String(columns,","));
		}
		out.println(CSVToString(columns, values));
		out.close();
		
		return false;
	}
	
	public static String array2String(ArrayList<String> input, String glue) {
		return array2String(input, glue, 0, input.size());
	}
	
	public static String array2String(Iterable<String> input, String glue, int start, int end) {
		String tGlue = "";
		StringBuilder out = new StringBuilder();
		int count = 0;
		for(String s : input) {
			if(count >= start && count < end) {
				out.append(tGlue + s);
				tGlue = glue;				
			}
			count += 1;
		}
		
		return out.toString();
		
	}
	
	public static void writeHeader(ArrayList<String> header, String target) throws IOException {

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(target, false)));
		out.println(CSVParser.array2String(header,","));
		out.close();
	}
	
	private static String CSVToString(ArrayList<String> columns, HashMap<String, String> values) {
		String glue = "";
		StringBuilder out = new StringBuilder();
		String val;
		for(int i = 0; i < columns.size(); i++) {
			val = values.get(columns.get(i));
			
			if(val.length() > 1 && val.charAt(0) == '"' && val.charAt(val.length() -1 ) == '"') {
				val = val.substring(1, val.length()- 1);
			}
			if(val.contains("\"") || val.contains(",")) {
				out.append(glue + "\"" + val.replace("\"", "\"\"") + "\"");
			} else {
				out.append(glue + val);				
			}
			glue = ",";
		}
		return out.toString();		
	}
	
	public String getOutName() {
		
		return this.getNameWithoutFiletypeAndPath() + "_out.csv";
		
	}
	
	public String getJSONName() {
		return this.getNameWithoutFiletypeAndPath() + ".json";
	
	}
	
	public String getPath() {
		String[] pathtokens = this.source.replace(Pattern.quote("/"),Pattern.quote(File.separator)).split(Pattern.quote(File.separator));
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < pathtokens.length - 1; i++) {
			out.append(pathtokens[i]).append(File.separator);
		}
		
		return out.toString();
	}
	
	private String getNameWithoutFiletypeAndPath() {
		
		String[] pathtokens = this.source.replace(Pattern.quote("/"),Pattern.quote(File.separator)).split(Pattern.quote(File.separator));
		
		String[] tokens = pathtokens[pathtokens.length - 1].split(Pattern.quote("."));
		StringBuilder out = new StringBuilder();
		int correctForFileType = (tokens[tokens.length - 1] == "csv")? 2 : 1;
		for(int i = 0; i < tokens.length - correctForFileType; i++) {
			out.append(tokens[i]);
		}
		return out.toString();
	}
}
