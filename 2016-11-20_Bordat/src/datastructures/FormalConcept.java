package datastructures;

import java.util.ArrayList;
import java.util.BitSet;

public class FormalConcept {
	private ArrayList<FormalObject> extent;
	private BitSet intent;
	private ArrayList<FormalConcept> lowerNeighbours;
	
	public FormalConcept(ArrayList<FormalObject> objects, BitSet attributes){
		this.extent = objects;
		this.intent = attributes;
		this.lowerNeighbours = new ArrayList<FormalConcept>();
	}
	
	public ArrayList<FormalObject> getExtent() {
		return extent;
	}
	
	public BitSet getIntent() {
		return intent;
	}
	
	public void addToLowerNeighbours(FormalConcept fc) {
		lowerNeighbours.add(fc);
	}
	
	public void addAllToLowerNeighbours(ArrayList<FormalConcept> concepts) {
		lowerNeighbours.addAll(concepts);
	}
	
	public ArrayList<FormalConcept> getLowerNeighbours() {
		return lowerNeighbours;
	}
}