package driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import datastructures.Dictionary;
import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import datastructures.LatticeNode;

public class ContextCleanser {
	
	private Lattice lattice;
	private FormalContext context;
	private Dictionary dic;
	
	public ContextCleanser(FormalContext _context, Lattice _lattice){
		this.lattice = _lattice;
		this.context = _context;
		this.dic = _context.getDictionary();
	}
	
	//only for the bitSet hash. TODO: Make static class, refactor
	public ContextCleanser(Dictionary _dic) {
		this.lattice = null;
		this.context = null;
		this.dic = _dic;
	}
	
	//only for testing
	public ContextCleanser(){
		this.lattice = null;
		this.context = null;
	}
	
	//checks which attributes appear the least amount of times in the data
	//and completely removes these attributes from the context
	//CURRENTLY NOT USED
	public void removeRareAttributes(int treshold) {
		HashMap<String, Integer> attributeSupport = context.getAttributeSupport();
		HashSet<Integer> supportSet = new HashSet<Integer>();
		supportSet.addAll(attributeSupport.values());
		Integer[] supportArray = supportSet.toArray(new Integer[supportSet.size()]);
		Arrays.sort(supportArray);
		final int TRESHOLD = supportArray[treshold];
		System.out.print("Deleted attributes (Treshold = " + TRESHOLD + "): ");
//		System.out.println("Deleting all attributes that occur at most " + TRESHOLD + " times.");
		for(String attr : context.getDictionary().getContents()){
			if(attributeSupport.get(attr) <= TRESHOLD){
				System.out.print(attr + " // ");
				context.removeAttribute(attr);
			}
		}
		System.out.println("");
	}
	
	//noBigAttrDifference = firstOption, noOwnAttr = thirdOption
	public double latticeMerge(Boolean noBigAttrDifference, Boolean noOwnAttr) {
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
					double currentMergeScore = mergeScore(node, candidate, noOwnAttr, noBigAttrDifference);
					if(currentMergeScore >= highScore) {
						highScore = currentMergeScore;
						firstNode = node;
						secondNode = candidate;
					}
				}
			}
		}
//		System.out.println("highscore = " + highScore);
		if(highScore > 0.0) {
			mergeInto(firstNode, secondNode);
			return highScore;
		}
		return -1;
	}

	private double mergeScore(LatticeNode node, LatticeNode candidate, Boolean noOwnAttr, Boolean noBigAttrDifference) {
		if(!node.hasOwnObjects() || !candidate.hasOwnObjects() || candidate.numberOfOwnObjects() < node.numberOfOwnObjects())
			return 0d;
		//first option
		if(noBigAttrDifference && attributeDifference(node, candidate) > 2){
			return 0d;
		}
		//third option
		if(noOwnAttr && ((candidate.hasOwnAttributes() && candidate.upperNeighbours().contains(node) /*&& !node.isTopNode()*/) 
					  || (node.hasOwnAttributes() && node.upperNeighbours().contains(candidate) /*&& !candidate.isTopNode()*/))){//excluding the top node is a parameter we experimented with
//			System.out.println("not merging into node with attribute(s) " + candidate.getNiceAttributes());
			return 0d;
		}
		double ownObjectRatio = candidate.numberOfOwnObjects()/(double)node.numberOfOwnObjects();
		return ownObjectRatio/(double)node.numberOfOwnObjects();
	}
	
	private void mergeInto(LatticeNode firstNode, LatticeNode secondNode) {
//		System.out.println("\tmerging " + firstNode.getIntent() + " (" + bitsetHash(firstNode.getIntent()) + ") -> " + secondNode.getIntent() + " (" + bitsetHash(secondNode.getIntent()) + ")"); 
		BitSet mergedIntent = (BitSet)secondNode.getIntent().clone();
		lattice.updateBookkeeping(firstNode, secondNode);
		for(FormalObject obj : firstNode.ownObjects())
			obj.setIntent(mergedIntent);
		lattice.setLastMergedInto((BitSet)secondNode.getIntent().clone());
	}
	
	//creates a String from a BitSet where true = 1 and false = 0.
	//limits or pads the string to the length of the dictionary of the current context.
	public String bitsetHash (BitSet set) {
		String hash = "";
		for(int i = 0; i < set.size(); i++){
			if(set.get(i))
				hash += "1";
			else
				hash += "0";
		}
		if(hash.length() < dic.getSize()){
			String padding = "";
			for(int j = 0; j < (dic.getSize()-hash.length()); j++)
				padding += "0";
			hash += padding;
			return hash;
		}
		else if(hash.length() > dic.getSize())
			return hash.substring(0, dic.getSize());
		else
			return hash;
	}
	
	//returns the number of attributes that are different in two lattice nodes
	public int attributeDifference(LatticeNode node1, LatticeNode node2){
		BitSet set1 = (BitSet)node1.getIntent().clone();
		set1.xor(node2.getIntent());
		return set1.cardinality();
	}

	public void removeUniqueObjects() {
		for(LatticeNode node : lattice.getNodes()){
			if(node.numberOfOwnObjects() == 1 && !node.isConnected()){ //we try this out
				Iterator<FormalObject> it = node.ownObjects().iterator();
				FormalObject obj = it.next();
				context.getObjects().remove(obj);
				lattice.addToRemovedSingletons(obj);
			}
		}
	}
}
