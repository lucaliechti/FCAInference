package datastructures;

import java.util.ArrayList;
import java.util.BitSet;

public class LatticeNode {
	private ArrayList<FormalObject> objects;
	private ArrayList<LatticeNode> lowerNeighbours;
	private BitSet extent;
	private int nodeNumber;
	
	public LatticeNode(BitSet bs) {
		this.objects = new ArrayList<FormalObject>();
		this.lowerNeighbours = new ArrayList<LatticeNode>();
		this.extent = bs;
		this.nodeNumber = 0;
	}
	
	public void setObjects(ArrayList<FormalObject> obj) {
		this.objects = obj;
	}
	
	public void addToLowerNeighbours(LatticeNode ln) {
		lowerNeighbours.add(ln);
	}
	
	public BitSet getExtent() {
		return extent;
	}
	
	public void setNodeNumber(int num) {
		this.nodeNumber = num;
	}
	
	public int getNodeNumber() {
		return nodeNumber;
	}
	
	public int getNumberOfObjects() {
		return objects.size();
	}
}