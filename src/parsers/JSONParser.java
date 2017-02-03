package parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import datastructures.FormalObject;

public class JSONParser implements NoSQLParser {
	
	private String wantedObjects = "items";
	private String nameAttribute = "file";

	@Override
	public ArrayList<FormalObject> parseFile(String file, int MAX_OBJECTS) {
		JSONArray jarray = extractJSONarray(file);
		return(createFormalObjects(jarray, MAX_OBJECTS));
	}

	private JSONArray extractJSONarray(String file) {
		JSONArray array = new JSONArray();
		try {
			String jtext = new String(Files.readAllBytes(Paths.get(file)));
			JSONObject jobj = new JSONObject(jtext);
			array = jobj.getJSONArray(wantedObjects);
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
		catch(JSONException jsone) { jsone.printStackTrace(); }
		return array;
	}
	
	private ArrayList<FormalObject> createFormalObjects(JSONArray jarray, int MAX_OBJECTS) {
		ArrayList<FormalObject> parsedObjects = new ArrayList<FormalObject>();
		System.out.print("Parsing objects to context... ");
		if(MAX_OBJECTS == 0) MAX_OBJECTS = jarray.length();	//declare how many objects we want. If all, just parse the whole array
		try{
			for(int i = 0; i < MAX_OBJECTS; i++) {
				FormalObject formalObj = new FormalObject();
				ArrayList<String> formalAttr = new ArrayList<String>();
				JSONObject obj = jarray.getJSONObject(i);
				String[] attributes = JSONObject.getNames(obj);
				for(int j = 0; j < attributes.length; j++){
					if(!formalAttr.contains(attributes[j])) formalAttr.add(attributes[j]);
				}
				formalObj.setAttributes(formalAttr);
				formalObj.setName(obj.getString(nameAttribute));
				parsedObjects.add(formalObj);
			}
		}
		catch(JSONException jsone) { jsone.printStackTrace(); }
		System.out.print("Done.");
		assert (parsedObjects.size() <= MAX_OBJECTS);
		return parsedObjects;
	}

	@Override
	public String getTargetContextFilename(String doc) {
		return "js_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-3) + ".cxt"; //hard-coding the file format at the moment
	}

	@Override
	public String getTargetLatticeFilename(String doc) {
		return "js_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-3) + ".dot";
	}
}
