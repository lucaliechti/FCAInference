package datastructures;

import java.util.BitSet;

public class LatticeNode {
	private BitSet extent;
	private int numberOfObjects;
	
	public LatticeNode(BitSet bs) {
		this.extent = bs;
		this.numberOfObjects = 1;
	}
	
	public void increaseNumberOfObjects() {
		numberOfObjects++;
	}
	
	public BitSet getExtent() {
		return extent;
	}

	public Boolean isSubsetOf(LatticeNode otherNode){
		BitSet thisExtent = (BitSet)extent.clone();
		thisExtent.and(otherNode.getExtent());
		return(thisExtent.equals(extent));
	}
	
	@Override
	public String toString() {
		return extent.cardinality() + "\t" + numberOfObjects;
	}
}