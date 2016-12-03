package datastructures;

import java.util.BitSet;
import java.util.HashSet;

public class FormalConcept {
	private HashSet<FormalObject> extent;
	private BitSet intent;
	
	public FormalConcept(HashSet<FormalObject> objects, BitSet attributes){
		this.extent = objects;
		this.intent = attributes;
	}
	
	public HashSet<FormalObject> getExtent() {
		return extent;
	}
	
	public BitSet getIntent() {
		return intent;
	}
}