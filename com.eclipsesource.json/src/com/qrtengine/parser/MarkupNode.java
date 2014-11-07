package com.qrtengine.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.*;

public class MarkupNode {


	private boolean addIndex = false;
	private int amount = 0;
	private String name = null;

	private HashMap<String, String> data = null;
	private HashMap<String, String> columns = null;

	private ArrayList<String> jsonCol = null;
	private ArrayList<String> colNames = null;

	private ArrayList<MarkupNode> childs = null;

	public MarkupNode(JsonObject json) {

		//Initialze MarkupNode..
		
		this.columns = new HashMap<String, String>();
		this.data = new HashMap<String, String>();
		this.childs = new ArrayList<MarkupNode>();
		this.jsonCol = new ArrayList<String>();

		if(json.get("columns") != null) {
			for(JsonObject.Member m : json.get("columns").asObject()) {

				//Try if getName is a number
				try {
					int key = Integer.parseInt(m.getName());
					this.columns.put(m.getValue().asString(), m.getValue().asString());
				} catch(Exception e) {
					this.columns.put(m.getName(), m.getValue().asString());
				}
			}		
		}		

		if(json.get("childs") != null) {
			for(JsonValue child : json.get("childs").asArray().values()) {
				this.childs.add(new MarkupNode(child.asObject()));
			}
		}

		if(json.get("amount") != null) {
			this.amount = json.get("amount").asInt();		
		} else {
			this.amount = 1;
			this.addIndex = false;
		}

		if(json.get("addIndex") != null) {
			this.addIndex = ( json.get("addIndex").asInt() == 1)? true: false;			
		} else {
			if(this.amount == 1) {
				this.addIndex = true;
			} else {
				this.addIndex = false;
			}
		}


		if(json.get("jsonCol") != null) {
			for(JsonValue cols : json.get("jsonCol").asArray().values()) {
				jsonCol.add(cols.asString());
			}
		}

		if(json.get("data") != null) {
			for(JsonObject.Member m : json.get("data").asObject()) {

				this.data.put(m.getValue().asString(), m.getValue().asString());

			}		
		}

		if(json.get("name") != null) {
			this.name = json.get("name").asString();			
		} else {
			this.name = "";
		}
	}

	protected ArrayList<String> getColNames() {
		if(colNames == null) {
			ArrayList<String> columns = new ArrayList<String>();

			columns.addAll(this.columns.values());
			columns.add(this.name);		
			columns.addAll(this.data.keySet());

			Collections.sort(columns, String.CASE_INSENSITIVE_ORDER);

			for(MarkupNode c : this.childs) {
				columns.addAll(c.getColNames());
			}

			for(int i = 0; i < columns.size(); i++) {
				int indexOf = columns.indexOf(columns.get(i));
				int lastIndexOf = columns.lastIndexOf(columns.get(i));
				while(indexOf != lastIndexOf) {
					columns.remove(lastIndexOf);
					lastIndexOf = columns.lastIndexOf(columns.get(i));
				}
			}
			this.colNames = columns;
		}
		//System.out.println(colNames.toString());
		return this.colNames;
	}

	protected HashMap<String, String> addLevelData(HashMap<String, String> subjectData, HashMap<String, String> levelData) {
		return addLevelData(subjectData, levelData, -1);
	}

	protected HashMap<String, String> addLevelData(HashMap<String, String> subjectData, HashMap<String, String> levelData, int index) {
		//if(addIndex == false) System.out.println(this.columns);
		for(Map.Entry<String,String> e : this.columns.entrySet()) {
			
			String indexName = e.getKey();
			if(this.addIndex){
				indexName += "(" + (index + 1) + ")";
			}
			if(levelData.containsKey(e.getValue()) && subjectData.containsKey(indexName)) {
				levelData.put(e.getValue(), subjectData.get(indexName));
			}
		}

		for(Map.Entry<String, String> e : this.data.entrySet()) {
			levelData.put(e.getKey(), e.getValue());
		}

		for(String key : this.jsonCol) {
			if(this.addIndex){
				key += "(" + (index + 1) + ")";
				
				//Check if subjectdata exists, if not, create empty object
				
				String sData = subjectData.get(key);
				JsonObject cols;
				if(sData == null || sData == "") {
					cols = new JsonObject();
				} else {
					cols = JsonObject.readFrom(sData);
				}
				for (JsonObject.Member m : cols) {
					levelData.put(m.getName(), m.getValue().toString());
				}
			}
		}
		if(this.addIndex){
			levelData.put(this.name, "" + index);
		}
		//System.out.println(levelData);
		return levelData;
	}

	public boolean writeData(CSVParser parser, String target) throws Exception {


		//Get all columnNames for the target file, just the values and only unique ones
		ArrayList<String> columns = this.getColNames();		

		//write the first line (the column names) in the outputfile.
		CSVParser.writeHeader(columns,target);

		HashMap<String, String> globalData = new HashMap<String, String>(columns.size());
		for(String col : columns) {
			globalData.put(col, "");
		}

		HashMap<String, String> next = new HashMap<String, String>();
		parser.resetIterator();
		//Skip line 2
		parser.nextLine();
		while((next = parser.nextLine()) != null) {
			this.writeData(next, globalData, columns, target);
		}

		return false;
	}

	private void writeData(HashMap<String, String> subjectData,
			HashMap<String, String> levelData, ArrayList<String> header, String target) throws IOException {
		// TODO Auto-generated method stub
		levelData = (HashMap<String, String>) levelData.clone();
		for(int i = 0; i < this.amount; i++) {
			levelData = this.addLevelData(subjectData, levelData, i);
			//System.out.println(subjectData.get("V1"));
			if(this.childs.size() != 0) {
				for(MarkupNode c : this.childs) {
					c.writeData(subjectData, levelData, header, target);
				}
			} else {
				CSVParser.writeLine(target, header, levelData, false);
			}	
		}	
	}
}
