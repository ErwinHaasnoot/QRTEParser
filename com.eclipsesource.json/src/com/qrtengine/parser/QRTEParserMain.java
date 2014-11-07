/**
 * 
 */
package com.qrtengine.parser;

import java.io.File;

/**
 * @author Erwin
 *
 */
public class QRTEParserMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String input, outputDir;
		
		QRTEParserConsoleLogger logger = new QRTEParserConsoleLogger();
		if (args.length == 0) {
			logger.log("Please supply an input file, as such: \n\tjava -jar QRTEParser_console.jar <input file> [<output directory>]");
			return;
		}
		
		if ( args.length == 1) {
			outputDir = "";
			logger.log("No output directory received, assuming current directory");
		} else {
			outputDir = args[1];
		}
		
		input = args[0];
		logger.log("Parsing file: " + input + "\n\ttarget directory: " + outputDir);
		
			
		QRTEParserMain.convert(logger, input, outputDir );
		// TODO Auto-generated method stub			
		
	}
	
	public static boolean convert(QRTEParserLoggerI logger, String dataSource, String targetDirectory) {
		try {
			CSVParser parser = new CSVParser(dataSource, true);	
			if(targetDirectory == "") {
				targetDirectory = parser.getPath();
			}
			MarkupNode markup = MarkupNodeFactory.createNodeFromCSV(parser, logger);
			return QRTEParserMain.convert(logger, parser, markup, targetDirectory);
		} catch (Exception e) {
			logger.log("Parsing failed");
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean convert(QRTEParserLoggerI logger, String dataSource, String targetDirectory, String JSONSource) {

		try {
			CSVParser parser = new CSVParser(dataSource, true);
			MarkupNode markup = MarkupNodeFactory.createNodeFromFile(JSONSource);
			return QRTEParserMain.convert(logger, parser, markup, targetDirectory);
		} catch (Exception e) {
			logger.log("Couldn't parse supplied JSONFile, attempting to create from CSV");
			return QRTEParserMain.convert(logger, dataSource, targetDirectory);
			//System.out.println(e.toString());
		}
		
	}
	
	public static boolean convert(QRTEParserLoggerI logger, CSVParser parser, MarkupNode markup, String targetDirectory) throws Exception {
		System.out.println(parser.getJSONName() + "     " + parser.getOutName());
		/*boolean isFirst = true;
		while((out = parser.nextLine()) != null) {
			CSVParser.writeLine(target, parser.getColumns(), out, isFirst);
			isFirst = false;
		}*/

		logger.log("Generating MarkupNodes from markup");
		String target = targetDirectory + ((targetDirectory.length() == 0 || ("" + targetDirectory.charAt(targetDirectory.length() - 1)) == File.separator)? "" : File.separator) +  parser.getOutName();
		
		logger.log("Successfully created MarkupNodes from markup, attempting to write");
		markup.writeData(parser, target);
		logger.log("Write successful, check: "+ target);
		
		return true;
		
	}

}
