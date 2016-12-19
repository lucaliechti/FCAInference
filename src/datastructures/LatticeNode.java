package datastructures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;

public class LatticeNode {
	private HashSet<FormalObject> extent;
	private HashSet<FormalObject> ownObjects;
	private BitSet intent;
	private HashSet<LatticeNode> transitivelyReachable;
	private int nodeNumber;
	private Dictionary dic;
	private HashSet<LatticeNode> upperNeighbours;
	private HashSet<LatticeNode> lowerNeighbours;
	private ArrayList<String> ownAttributes;
	
	public LatticeNode(HashSet<FormalObject> hashSet, BitSet intent, Dictionary _dic) {
		this.intent = intent;
		this.extent = new HashSet<FormalObject>();
			for(FormalObject obj : hashSet) extent.add(obj);
		this.ownObjects = new HashSet<FormalObject>();
		this.transitivelyReachable = new HashSet<LatticeNode>();
		this.dic = _dic;
		this.upperNeighbours = new HashSet<LatticeNode>();
		this.lowerNeighbours = new HashSet<LatticeNode>();
		this.ownAttributes = new ArrayList<String>();
	}
	
	public BitSet getIntent() {
		return intent;
	}
	
	public void setNodeNumber(int num) {
		this.nodeNumber = num;
	}
	
	public HashSet<FormalObject> getExtent() {
		return this.extent;
	}
	
	public void addToTransitivelyReachableNodes(LatticeNode node) {
		this.transitivelyReachable.add(node);
	}
	
	//attention, this is not very recursive yet
	public void addAllToTransitivelyReachableNodes(HashSet<LatticeNode> nodes) {
		this.transitivelyReachable.addAll(nodes);
	}
	
	public void addObject(FormalObject obj) {
		this.extent.add(obj);
	}
	
	public void addToOwnObjects(FormalObject obj) {
		this.ownObjects.add(obj);
	}
	
	//this is always called from the UPPER node, because only the upper node contains a list of all lower nodes
	//that can be transitively reached from it.
	public Boolean canTransitivelyReach(LatticeNode node) {
		return transitivelyReachable.contains(node);
	}
	
	public int getNodeNumber() {
		return nodeNumber;
	}
	
	public int numberOfObjects() {
		return extent.size();
	}
	
	public int numberOfOwnObjects() {
		return ownObjects.size();
	}
	
	public String getNiceString() {
		String nice = "{";
		for(int i = 0; i < intent.length(); i++) {
			if(intent.get(i)){
				nice += dic.getAttribute(i);
				if(i < intent.length()-1) nice += ", ";
			}
		}
		nice += "}";
		return nice;
	}
	
	public String getNiceExtentString() {
		String niceExtent = "(";
		Iterator<FormalObject> it = extent.iterator();
		while(it.hasNext()){
			if(!(niceExtent.length() == 1)) niceExtent += ", ";
			FormalObject obj = it.next();
			niceExtent += obj.getName();
		}
		niceExtent += ")";
		return niceExtent;
	}

	public void addToUpperNeighbours(LatticeNode from) {
		this.upperNeighbours.add(from);
	}
	
	public void addToLowerNeighbours(LatticeNode to) {
		this.lowerNeighbours.add(to);
	}
	
	public HashSet<LatticeNode> upperNeighbours() {
		return this.upperNeighbours;
	}
	
	public HashSet<LatticeNode> lowerNeighbours() {
		return this.lowerNeighbours;
	}
	
	public HashSet<LatticeNode> getTransitivelyReachableNodes() {
		return this.transitivelyReachable;
	}
	
	public void addToOwnAttributes(String attr) {
		this.ownAttributes.add(attr);
	}
	
	public String getNiceAttributes() {
		String attr = "";
		for(int i = 0; i < ownAttributes.size(); i++){
			attr += ownAttributes.get(i);
			if(i < ownAttributes.size()-1)
				attr += ", ";
		}
		if(attr.length() > 0) attr += "\n";
		return attr;
	}

	public HashSet<FormalObject> ownObjects() {
		return ownObjects;
	}
}