package datastructures;

import java.util.HashMap;
import java.util.Set;

public class Dictionary {
	
	private HashMap<String, Integer> attributes;
	private int nextAttributeNumber;
	
	public Dictionary() {
		this.attributes = new HashMap<String, Integer>();
		this.nextAttributeNumber = 0;
	}
	
	public Boolean containsAttribute(String _attr){
		return attributes.containsKey(_attr);
	}
	
	public void addAttribute(String _attr){
		attributes.put(_attr, nextAttributeNumber++);
	}
	
	public int getAttributePosition(String _attr){
		return attributes.get(_attr);
	}
	
	public Set<String> getContents(){
		return attributes.keySet();
	}
	
	public int getSize(){
		return attributes.size();
	}
}
