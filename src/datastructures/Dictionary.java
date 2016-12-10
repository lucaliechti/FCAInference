package datastructures;

import java.util.Set;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

public class Dictionary {

	private TreeBidiMap<Integer, String> attributes;
	private int nextAttributeNumber;
	
	public Dictionary() {
		this.attributes = new TreeBidiMap<Integer, String>();
		this.nextAttributeNumber = 0;
	}
	
	public Boolean containsAttribute(String attr){
		return attributes.containsValue(attr);
	}
	
	public void addAttribute(String attr){
		attributes.put(nextAttributeNumber++, attr);
	}
	
	public int getAttributePosition(String attr){
		return attributes.getKey(attr);
	}
	
	public String getAttribute(int i) {
		return attributes.get(i);
	}
	
	public Set<String> getContents(){
		return attributes.values();
	}
	
	public int getSize(){
		return attributes.size();
	}
}
