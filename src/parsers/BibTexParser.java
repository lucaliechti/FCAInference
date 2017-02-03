package parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import datastructures.FormalObject;

public class BibTexParser implements NoSQLParser {
	
	public ArrayList<FormalObject> parseFile(String file, int MAX_OBJECTS){
		ArrayList<String> splitObjects = splitFile(file); //split the input file
		return createFormalObjects(splitObjects, MAX_OBJECTS); //extract attributes from split objects		
	}

	private ArrayList<String> splitFile(String file) {
		ArrayList<String> splitString = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader (new FileReader(file));
			int brackets = 0;
			String currentObject = "";
			Boolean validObject = false; //this is very ugly. We prevent empty lines from being objects, because nr. of brackets = 0
			String line = br.readLine();
			while(line != null){
				for(int i = 0; i < line.length(); i++) {
					if(line.charAt(i) == '{') { brackets++; validObject = true; }
					else if (line.charAt(i) == '}') brackets--;
					else {}
				}
				currentObject += line + "\n";
				//check if we have reached the end of an object
				if(brackets == 0 && validObject) {
					splitString.add(currentObject.trim());
					currentObject = "";
					validObject = false;
				}
				line = br.readLine();
			}
			br.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		return splitString;
	}
	
	private ArrayList<FormalObject> createFormalObjects(ArrayList<String> splitObjects, int MAX_OBJECTS) {
		ArrayList<FormalObject> parsedObjects = new ArrayList<FormalObject>();
		System.out.print("Parsing objects to context... ");
		//extract the attributes from each object
		//for simplicity's sake, we assume that the '=' character only ever appears as separator between attribute and value
		int k = 0;
		for(String obj : splitObjects){
			FormalObject currentObject = new FormalObject();
			ArrayList<String> attributes = new ArrayList<String>();
			String name = "NO NAME FOUND"; //will be overwritten
			String[] lines = obj.split("\n");
			for(int i = 0; i < lines.length; i++){
				if(lines[i].contains(" = ")){
					String attribute = lines[i].split(" = ")[0].trim();
					if(!attributes.contains(attribute)) attributes.add(attribute.toLowerCase());
				}
				else if(lines[i].matches("@.*\\{.*")){
					name = lines[i].substring(lines[i].indexOf("@")+1, lines[i].indexOf("{"));
				}
			}
			currentObject.setAttributes(attributes);
			currentObject.setName(name);
			if(MAX_OBJECTS == 0 || ++k <= MAX_OBJECTS)
				parsedObjects.add(currentObject); 
		}
		System.out.println("done.");
		assert (parsedObjects.size() <= MAX_OBJECTS);
		return parsedObjects;
	}

	@Override
	public String getTargetContextFilename(String doc) {
		return "bib_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-4) + ".cxt"; //hard-coding the file format at the moment
	}

	@Override
	public String getTargetLatticeFilename(String doc) {
		return "bib_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-4) + ".dot";
	}
}
