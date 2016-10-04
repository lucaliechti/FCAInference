package datastructures;

import java.util.ArrayList;
import java.util.BitSet;

public class FormalObject {
	private String name;
	private BitSet extent;
	private ArrayList<String> attributes;
	
	public FormalObject(){
		this.name = "";
		this.extent = new BitSet();
		this.attributes = new ArrayList<String>();
	}
	
	public ArrayList<String> getAttributes() {
		return attributes;
	}
	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BitSet getExtent() {
		return extent;
	}
	public void setExtent(BitSet extent) {
		this.extent = extent;
	}

}
