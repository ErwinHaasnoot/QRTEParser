package com.qrtengine.parser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.eclipsesource.json.*;

public class MarkupNodeFactory {

	private static QRTEParserLoggerI logger;
	private static JsonObject predefinedColumns;
	private static ArrayList<String> ignoreColumns;
	private static JsonObject topLevel;
	private static JsonObject bottomLevel;
	private static ArrayList<String> header;
	private static String QRTE_exitQuestions = "QRTE_exitQuestions";
	private static String QRTE_blockData = "QRTE_blockData";
	private static String QRTE_idData = "QRTE_idData";
	private static String QRTE_columns = "QRTE_columns";
	
	public static MarkupNode createNodeFromFile(String source) throws Exception {
		JsonObject json = JsonObject.readFrom(new FileReader(source));
		return new MarkupNode(json);
	}

	public static MarkupNode createNodeFromCSV(CSVParser parser, QRTEParserLoggerI _logger) {
		//String to contain the blockId defined by the QRTE User
		String blockId = "";
		
		logger = _logger;

		//Reset the parser iterator so we're starting fresh!
		parser.resetIterator();

		//Get the header
		header = parser.getColumns();

		init();		

		HashMap<String,String> data;
		HashMap<String, JsonObject> blocks = new HashMap<String, JsonObject>();
		
		logger.log("Initializing Markup creation from CSV file");
		
		int subjectId = 1;
		
		
		try {
			//Skip the second line:
			data = parser.nextLine();
			//Loop over each data line to get all the columns that were defined during the experiment for each participant
			//Basically, scan the data to see which columns and which blocks were defined
			while((data = parser.nextLine()) != null) {
				
				//System.out.println(QRTE_columns);
				//System.out.println(data.toString());

				//Get the defined columns
				JsonObject predefColumns = JsonObject.readFrom(data.get(QRTE_columns));
				
				//logger.log(predefColumns.toString());
					
				
				//Get all the exit questions
				String[] exitQuestions = data.get(QRTE_exitQuestions).split(";");
				
				logger.log("Found " + exitQuestions.length + " trial blocks for subject " + subjectId + ": " + Arrays.toString(exitQuestions),2);
				String exitQ = "";
				//Loop over each exit questions and find info about that specific block
				for(int i = 0; i < exitQuestions.length; i++) {
					exitQ = exitQuestions[i];

					logger.log(exitQ);
					blockId = "";
					int countTrials = 1;
					//get amount of trials in block + blockId
					while(data.containsKey(exitQ + "_1_TEXT(" + countTrials + ")")) {
						countTrials += 1;
						if(blockId == "") blockId = data.get(exitQ + "_2_TEXT(" + countTrials + ")");
					}
					if(blockId == "") {
						logger.log("ERROR: Could not find a BlockId for Exit Question: " + exitQ + 
								" for subject: " + subjectId + "\nCheck if the Loop & Merge starts at index 1 for that trial block");
						
					}
					
					if(!blocks.containsKey(exitQ)) {
						
						JsonObject tempObj = new JsonObject(bottomLevel);
						tempObj.add("jsonCol", new JsonArray());
						tempObj.get("jsonCol").asArray().add(exitQ + "_1_TEXT");
						
						tempObj.set("name", exitQ + "Id");
						blocks.put(exitQ, tempObj);
						
						ignoreColumns.add(exitQ + "_1_TEXT");
						ignoreColumns.add(exitQ + "_2_TEXT");
					}
					JsonValue blockColumns = predefColumns.get(blockId);
					
					//Check if blocks exist, if not - break the loop. This line is unlikely to contain much useful information (possibly a partial response or something)
					if(blockColumns == null) continue;
					Iterator<JsonValue> it = predefColumns.get(blockId).asArray().iterator();
					String s;
					while(it.hasNext()) {
						s = it.next().asString();
						predefinedColumns.set(s, s);						
						blocks.get(exitQ).get("columns").asObject().set(s, s);
					}
					blocks.get(exitQ).set("amount",Math.max(countTrials - 1, blocks.get(exitQ).get("amount").asInt()));
					
				}
				
				logger.log("Successfully parsed subject " + subjectId++);
				

			}
			//System.out.println(blocks.toString());
			
			for(String key : header) {
				int index = -1;
				String putKey = "";
				String putVal = "";
				ArrayList<String> colIndexArr = checkColIndex(key);
				if(colIndexArr.get(0) == "1") {
					index = Integer.parseInt(colIndexArr.get(2));
					bottomLevel.set("amount",Math.max(bottomLevel.get("amount").asInt(), index));					
					putKey = colIndexArr.get(1);					
					putVal = colIndexArr.get(1);
					
					if( predefinedColumns.get(putKey) != null) {
						putVal = predefinedColumns.get(putKey).asString();
					}
					if( ! ignoreColumns.contains(putKey) && ! ignoreColumns.contains(putVal) && putKey != "" && putVal != "") {
						bottomLevel.get("columns").asObject().set(putKey, putVal);
					}
				} else {
					putVal = key;
					putKey = key;
					
					if(  predefinedColumns.get(putKey) != null) {
						putVal = predefinedColumns.get(putKey).asString();
					}
					if( ! ignoreColumns.contains(putKey) && ! ignoreColumns.contains(putVal) && putKey != "" && putVal != "") {

						topLevel.get("columns").asObject().set(putKey, putVal);
					}
				}
				
				
			}
			
			logger.log(blocks.toString(),3);
			JsonArray childs = topLevel.get("childs").asArray();
			for(JsonObject jo : blocks.values()) {
				childs.add(jo);
			}
			topLevel.set("childs", childs);
			
			logger.log("Successfully created markup from CSV file");
			
			
		} catch (Exception e) {
			logger.log("Error in creating markup at subject id "+ subjectId + " with error: "+ e.toString());
			e.printStackTrace();
		}
		
		//System.out.println(topLevel.toString());
		
		logger.log(topLevel.toString(),3);

		return new MarkupNode(topLevel);
	}

	//Initialize the fields required for properly getting the markup from the CSV

	private static void init() {


		predefinedColumns = new JsonObject();
		predefinedColumns.add("V1","ResponseID");
		predefinedColumns.add("V2","ResponseSet");
		predefinedColumns.add("V3","Name");
		predefinedColumns.add("V4","ExternalDataReference");
		predefinedColumns.add("V5","EmailAddress");
		predefinedColumns.add("V6","IPAddress");
		predefinedColumns.add("V7","Status");
		predefinedColumns.add("V8","StartDate");
		predefinedColumns.add("V9","EndDate");
		predefinedColumns.add("V10","Finished");

		ignoreColumns = new ArrayList<String>();
		ignoreColumns.add(QRTE_columns);
		ignoreColumns.add(QRTE_idData);
		ignoreColumns.add(QRTE_blockData);
		ignoreColumns.add(QRTE_exitQuestions);

		topLevel = new JsonObject();
		topLevel.add("childs", new JsonArray());
		topLevel.add("columns", new JsonObject());
		topLevel.add("amount", 1);
		topLevel.add("addIndex", 0);
		topLevel.add("name", "topLevelId");

		bottomLevel = new JsonObject();
		bottomLevel.add("columns", new JsonObject());
		bottomLevel.add("amount", 1);
		bottomLevel.add("addIndex", 1);
		bottomLevel.add("name", "bottomLevelUndefined");		
	}
	
	//Returns 3 values, first is a value telling whether the thing succeeded, if 1 also returns the strippedKey and the index of the field
	private static ArrayList<String> checkColIndex(String key) {
		ArrayList<String> out = new ArrayList<String>(3);
		
		//if

		String[] tokens = key.split(Pattern.quote("("));
		if(tokens.length < 2) { 
			out.add("0");
			return out;
		}
		
		String last = tokens[tokens.length - 1];
		if(last.charAt(last.length() - 1) != ')') {
			out.add("0");
			return out;			
		}
		String findLast = last.substring(0,last.length() - 1);
		Integer index = null;
		try {
			index = Integer.parseInt(findLast);			
		} catch (Exception e) {
		}
		if(index == null) {
			out.add("0");
			return out;				
		} else {

			out.add("1");
			
			ArrayList<String> temp = new ArrayList<String>();
			for(String s : tokens) temp.add(s);
			out.add(CSVParser.array2String(temp, "", 0, tokens.length - 1));
			out.add(index.toString());
			return out;	
		}
	}
}
