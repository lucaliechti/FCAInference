package driver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import datastructures.LatticeNode;

public class ContextCleanser {
	
	private Lattice lattice;
	private FormalContext context;
	private int attributeDifference;
	
	public ContextCleanser(FormalContext _context, Lattice _lattice){
		this.lattice = _lattice;
		this.context = _context;
	}
	
	//checks which attributes appear the least amount of times in the data
	//and completely removes these attributes from the context
	public void removeRareAttributes(int treshold) {
		HashMap<String, Integer> attributeSupport = context.getAttributeSupport();
		HashSet<Integer> supportSet = new HashSet<Integer>();
		supportSet.addAll(attributeSupport.values());
		Integer[] supportArray = supportSet.toArray(new Integer[supportSet.size()]);
		Arrays.sort(supportArray);
		final int TRESHOLD = supportArray[treshold];
//		System.out.println("Deleting all attributes that occur at most " + TRESHOLD + " times.");
//		int deleted = 0;
//		System.out.print("Nr of attributes before: " + context.numberOfAttributes() + "\n");
		for(String attr : context.getDictionary().getContents()){
			if(attributeSupport.get(attr) <= TRESHOLD){
//				deleted++;
				context.removeAttribute(attr);
			}
		}
//		System.out.println("Nr of attributes after:  " + (context.numberOfAttributes()-deleted) + "\t");
	}
	
	public double tinker() {
		HashMap<Integer, ArrayList<LatticeNode>> latticeLevelNodes = lattice.nodesByLevel();
		int[] levelArray = lattice.levelArray();
		double highScore = 0.0;
		LatticeNode firstNode = null;
		LatticeNode secondNode = null;
		for(int i = 0; i < levelArray.length; i++) {
			ArrayList<LatticeNode> thisLevelNodes = latticeLevelNodes.get(levelArray[i]);
			for(LatticeNode node : thisLevelNodes) {
				//specify which nodes can be merged into
				ArrayList<LatticeNode> mergeCandidates = new ArrayList<LatticeNode>();
				mergeCandidates.addAll(node.upperNeighbours());
				mergeCandidates.addAll(node.lowerNeighbours());
				//nodes from same level with at least one shared parent
//				for(LatticeNode parent : node.upperNeighbours()){
//					for(LatticeNode child : parent.lowerNeighbours()){
//						if(child != node)	mergeCandidates.add(child);
//					}
//				}
				for(LatticeNode candidate : mergeCandidates) {
					if(mergeScore(node, candidate) >= highScore) {
						highScore = mergeScore(node, candidate);
						firstNode = node;
						secondNode = candidate;
					}
				}
			}
		}
		if(highScore > 0.0) {
			mergeInto(firstNode, secondNode);
		}
		return highScore;
	}

	private double mergeScore(LatticeNode node, LatticeNode candidate) {
		if(!node.hasOwnObjects() || !candidate.hasOwnObjects() || candidate.numberOfOwnObjects() <= node.numberOfOwnObjects())
			return 0.0;
		double ownObjectRatio = candidate.numberOfOwnObjects()/(double)node.numberOfOwnObjects();
		double percentOfObjects = node.numberOfOwnObjects()/(double)context.getObjects().size()*100.0;
		return ownObjectRatio/percentOfObjects;
	}
	
	private void mergeInto(LatticeNode firstNode, LatticeNode secondNode) {
		BitSet mergedIntent = (BitSet)secondNode.getIntent().clone();
		lattice.updateBookkeeping(firstNode, secondNode);
		for(FormalObject obj : firstNode.ownObjects())
			obj.setIntent(mergedIntent);
		lattice.setLastMergedInto((BitSet)secondNode.getIntent().clone());
	}

	public void removeSingletonObjects() {
		HashMap<Integer, ArrayList<FormalObject>> nodeArray = new HashMap<Integer, ArrayList<FormalObject>>();
		int i = 0;
		int k = context.getObjects().size();
		//fill node array
		for(FormalObject obj : context.getObjects()) {
			if(nodeArray.containsKey(obj.getIntent().hashCode()))
				nodeArray.get(obj.getIntent().hashCode()).add(obj);
			else {
				ArrayList<FormalObject> newArray = new ArrayList<FormalObject>();
				newArray.add(obj);
				nodeArray.put(obj.getIntent().hashCode(), newArray);
			}
		}
		//delete singleton objects from context
		for(int hash : nodeArray.keySet()) {
			if(nodeArray.get(hash).size() == 1){
				context.getObjects().remove(nodeArray.get(hash).get(0));
				i++;
			}
		}
		System.out.println("Removed " + i + "/" + k + " objects from context.");
	}
}
