package datastructures;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class FormalContext {
	private ArrayList<FormalObject> objects;
	private HashMap<String, Integer> objectNumbers;
	private Dictionary dic;
	private HashMap<String, Integer> attributeSupport;
	
	public FormalContext() {
		this.objects = new ArrayList<FormalObject>();
		this.objectNumbers = new HashMap<String, Integer>();
		this.dic = new Dictionary();
		this.attributeSupport = new HashMap<String, Integer>();
	}
	
	//the BitSet is created at the time of the object being added to the context
	public void createAndAddObject(FormalObject object){
		BitSet intent = new BitSet();
		for(String attribute : object.getAttributes()){
			if(!dic.containsAttribute(attribute))
				dic.addAttribute(attribute);
			countAttribute(attribute); //counts all attributes to find more and less important ones
			intent.set(dic.getAttributePosition(attribute));
		}
		object.setIntent(intent);
		objects.add(object);
	}
	
	public void addObject(FormalObject obj) {
		objects.add(obj);
	}
	
	private void countAttribute(String attribute) {
		if(!attributeSupport.containsKey(attribute))
			attributeSupport.put(attribute, 1);
		else
			attributeSupport.put(attribute, attributeSupport.get(attribute)+1);
	}

	public void exportContextToFile(String outputFile){
		String exportString = createCXTString();
		System.out.print("Writing context to file... ");
		writeToFile(exportString, outputFile);
		System.out.println("done.");
	}

	private void writeToFile(String exportString, String outputFile) {
		try {
			PrintWriter out = new PrintWriter(outputFile, "UTF-8");
			out.print(exportString);
			out.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); } 
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
	}
	
	private String createCXTString(){
		//sortObjects(); //--------------------------- commented OUT for test purposes only!
		String export = "";
		export += "B\n\n";
		export += objects.size() + "\n";
		export += dic.getSize() + "\n\n";
		for(FormalObject obj : objects){
			export += obj.getName() + getObjectNumber(obj.getName()) + "\n";
		}
		//prints out attributes in order
		for(int i = 0; i < dic.getSize(); i++)
			export += dic.getAttribute(i) + "\n";
		for(FormalObject obj : objects){
			BitSet intent = obj.getIntent();
			String currentObject = "";
			for(int i = 0; i < dic.getSize(); i++){
				if(intent.get(i)) currentObject += "X";
				else currentObject += ".";
			}
			export += currentObject + "\n";
		}
		return export;
	}
	
//	private void sortObjects(){
//		Collections.sort(objects, new Comparator<FormalObject>() {
//			public int compare(FormalObject obj1, FormalObject obj2) {
//				return obj1.getName().compareTo(obj2.getName());
//			}
//		});
//	}
	
	//returns an int enumerating all objects of the same type
	//like this, we have book0, book1, book2 etc.
	private int getObjectNumber(String name){
		if(objectNumbers.containsKey(name))
			objectNumbers.put(name, objectNumbers.get(name)+1);
		else 
			objectNumbers.put(name, 0);
		return objectNumbers.get(name);
	}
	
	public ArrayList<FormalObject> getObjects() {
		return objects;
	}
	
	public int numberOfAttributes() {
		return dic.getSize();
	}
	
	public Dictionary getDictionary() {
		return dic;
	}

	public HashSet<FormalObject> getDerivationOfAttributes(BitSet seed) {
		HashSet<FormalObject> derivation = new HashSet<FormalObject>();
		for(FormalObject obj : objects) {
			if(obj.isSupersetOf(seed)) derivation.add(obj);
		}
		return derivation;
	}
	
	public BitSet getDerivationOfObjects(ArrayList<FormalObject> extent) {
		BitSet derivation = (BitSet)extent.get(0).getIntent().clone();
		for(FormalObject obj : extent)
			derivation.and(obj.getIntent());
		return derivation;
	}
	
	public BitSet getDerivationOfSingleObject(FormalObject obj) {
		return obj.getIntent();
	}
	
	public String attributeSupport() {
		String support = "";
		for(String attr : attributeSupport.keySet())
			support += attr + ": " + attributeSupport.get(attr) + "\n";
		return support;
	}
	
	public HashMap<String, Integer> getAttributeSupport() {
		return this.attributeSupport;
	}

	public void removeAttribute(String attr) {
		int position = dic.getAttributePosition(attr);
		for(FormalObject obj : objects)
			obj.getIntent().set(position, false);
	}
}