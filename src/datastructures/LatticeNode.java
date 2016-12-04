package datastructures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LatticeNode {
	private HashSet<FormalObject> extent;
	private BitSet intent;
	private HashSet<LatticeNode> transitivelyReachable;
	private int nodeNumber;
	private Dictionary dic;
	
	public LatticeNode(HashSet<FormalObject> hashSet, BitSet intent, Dictionary _dic) {
		this.intent = intent;
		this.extent = new HashSet<FormalObject>();
			for(FormalObject obj : hashSet) extent.add(obj);
		this.transitivelyReachable = new HashSet<LatticeNode>();
		this.dic = _dic;
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
	public void addAllToTransitivelyReachableNodes(ArrayList<LatticeNode> nodes) {
		this.transitivelyReachable.addAll(nodes);
	}
	
	public void addObject(FormalObject obj) {
		this.extent.add(obj);
	}
	
	public Boolean isTransitivelyReachable(LatticeNode node) {
		return transitivelyReachable.contains(node);
	}
	
	public int getNodeNumber() {
		return nodeNumber;
	}
	
	public int getNumberOfObjects() {
		return intent.size();
	}
	
	public String getNiceString() {
		Set<String> allAttributes = dic.getContents();
		String nice = "{";
		for(int i = 0; i < intent.length(); i++) {
			if(intent.get(i)){
				for(String attr : allAttributes){
					if(dic.getAttributePosition(attr) == i) nice += attr;
				}
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
}