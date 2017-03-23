package datastructures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
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
	private Boolean mergedInto;
	private Boolean topNode;
	
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
		this.mergedInto = false;
		this.topNode = false;
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
	
	public Boolean hasOwnObjects() {
		return (ownObjects.size() > 0);
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
	
	public String typesOfExtent() {
		return typesOfFormalObjects(extent);
	}
	
	public String typesOfOwnObjects() {
		return typesOfFormalObjects(ownObjects);
	}
	
	public void setMergedInto() {
		this.mergedInto = true;
	}
	
	public boolean mergedInto() {
		return mergedInto;
	}
	
	public void setTopNode() {
		this.topNode = true;
	}
	
	public Boolean isTopNode() {
		return topNode;
	}
	
	public int majority() {
		HashMap<String, Integer> counts = countObjectTypes(ownObjects);
		String majorityType = mostFrequentType(counts);
		return counts.get(majorityType);
	}
	
	protected String typesOfFormalObjects(HashSet<FormalObject> set) {
		if(set.size() > 0) {
			HashMap<String, Integer> counts = countObjectTypes(set);
			if(counts.keySet().size() == 1)
				return "100% " + counts.keySet().toArray()[0];
			else {
				String highestAttr = mostFrequentType(counts);
				int highest = counts.get(highestAttr);
				return (100*highest)/set.size() + "% " + highestAttr;
			}	
		}
		else
			return "empty";
	}
	
	private String mostFrequentType(HashMap<String, Integer> counts) {
		int highest = 0;
		String highestAttr = "";
		for(String attr : counts.keySet()) {
			if(counts.get(attr) > highest) {
				highest = counts.get(attr);
				highestAttr = attr;
			}
		}
		return highestAttr;
	}

	private HashMap<String, Integer> countObjectTypes(HashSet<FormalObject> set) {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		String type = "";
		for(FormalObject obj : set){
			type = obj.getName();
			if(!counts.containsKey(type))
				counts.put(type, 1);
			else
				counts.put(type, counts.get(type)+1);
		}
		return counts;
	}
	
	public Boolean hasOwnAttributes() {
		return ownAttributes.size() > 0;
	}
	
	//returns true if any parent or child node of this node has at least 1 own object.
	public Boolean isConnected() {
		for(LatticeNode upper : upperNeighbours()) {
			if(upper.hasOwnObjects())
				return true;
		}
		for(LatticeNode lower : lowerNeighbours()) {
			if(lower.hasOwnObjects())
				return true;
		}
		return false;
	}
}