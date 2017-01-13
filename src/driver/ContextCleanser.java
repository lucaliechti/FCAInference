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
		System.out.println("---BEGIN CLEANSING---");
		HashMap<String, Integer> attributeSupport = context.getAttributeSupport();
		HashSet<Integer> supportSet = new HashSet<Integer>();
		supportSet.addAll(attributeSupport.values());
		Integer[] supportArray = supportSet.toArray(new Integer[supportSet.size()]);
		Arrays.sort(supportArray);
		final int TRESHOLD = supportArray[treshold];
		System.out.println("Deleting all attributes that occur at most " + TRESHOLD + " times.");
		int deleted = 0;
		System.out.print("Nr of attributes before: " + context.numberOfAttributes() + "\t");
		System.out.println("Lattice stats before:\t" + lattice.latticeStats());
		for(String attr : context.getDictionary().getContents()){
			if(attributeSupport.get(attr) <= TRESHOLD){
				deleted++;
				context.removeAttribute(attr);
			}
		}
		System.out.print("Nr of attributes after:  " + (context.numberOfAttributes()-deleted) + "\t");
	}
	
	//changes the intents of objects that are very close to other objects.
	//after running this, the lattice has to be recomputed
	public void mergeNodes(int factor, int attrDiff, int percent) {
		System.out.println("---BEGIN CLEANSING---");
		System.out.println("Merging all nodes with their biggest neighbours if they\n"
				+ "\t- have at least " + factor + " times more own objects\n" 
				+ "\t- have at most " + attrDiff + " more/less attribute(s)\n" 
				+ "\t- make up at most " + percent + "% of all objects.");
		System.out.println("Lattice stats before:\t" + lattice.latticeStats());
		attributeDifference = attrDiff;
		HashMap<Integer, ArrayList<LatticeNode>> latticeLevelNodes = lattice.nodesByLevel();
		int[] levelArray = lattice.levelArray();
		for(int i = 0; i < levelArray.length; i++) {
			ArrayList<LatticeNode> thisLevelNodes = latticeLevelNodes.get(levelArray[i]);
			for(LatticeNode node : thisLevelNodes) {
				if(node.hasOwnObjects() && node.numberOfOwnObjects() < (percent*context.getObjects().size()/100)) {
					//find out which lower node has the most own objects
					LatticeNode mergeCandidate = findMergeCandidate(node.lowerNeighbours());
					//if criteria fit, merge upper node into lower
					if(isMergeCandidateFor(mergeCandidate, node)){
						for(FormalObject obj : node.ownObjects()){
							obj.setIntent((BitSet)mergeCandidate.getIntent().clone());
						}
					}
					//if that didn't happen but there's an UPPER candidate, merge upward
					else{
						LatticeNode upperMergeCandidate = findMergeCandidate(node.upperNeighbours());
						if(isMergeCandidateFor(upperMergeCandidate, node)){
							for(FormalObject obj : node.ownObjects()){
								obj.setIntent((BitSet)upperMergeCandidate.getIntent().clone());
								//TODO: recompute upper and lower neighbours
							}
						}
					}
				}
			}
		}
	}

	private LatticeNode findMergeCandidate(HashSet<LatticeNode> neighbours) {
		int mostOwnObjects = 0;
		LatticeNode mergeCandidate = null;
		for(LatticeNode lowerNode : neighbours){
			if(lowerNode.numberOfOwnObjects() > mostOwnObjects){
				mostOwnObjects = lowerNode.numberOfOwnObjects();
				mergeCandidate = lowerNode;
			}
		}
		return mergeCandidate;
	}
	
	private Boolean isMergeCandidateFor(LatticeNode mergeCandidate, LatticeNode smallNode) {
		return (mergeCandidate != null 
				&& smallNode.numberOfOwnObjects()*10 <= mergeCandidate.numberOfOwnObjects()
		   		&& mergeCandidate.getIntent().cardinality() <= (smallNode.getIntent().cardinality() + attributeDifference));
	}
	
	public double tinker() {
//		System.out.println("tinkering...");
		HashMap<Integer, ArrayList<LatticeNode>> latticeLevelNodes = lattice.nodesByLevel();
		int[] levelArray = lattice.levelArray();
		double highScore = 0.0;
		String highScoreMerge = "";
		LatticeNode firstNode = null;
		LatticeNode secondNode = null;
		for(int i = 0; i < levelArray.length; i++) {
			ArrayList<LatticeNode> thisLevelNodes = latticeLevelNodes.get(levelArray[i]);
			for(LatticeNode node : thisLevelNodes) {
				for(LatticeNode upper : node.upperNeighbours()) {
					if(mergeScore(node, upper) > highScore) {
						highScore = mergeScore(node, upper);
						highScoreMerge = "merged " /*+ node.getIntent() + " -> " + upper.getIntent()*/ + 
								" (up, score = " + new DecimalFormat("#.##").format(mergeScore(node, upper)) + ")";
						firstNode = node;
						secondNode = upper;
					}
				}
				for(LatticeNode lower : node.lowerNeighbours()) {
					if(mergeScore(node, lower) > highScore) {
						highScore = mergeScore(node, lower);
						highScoreMerge = "merged " /*+ node.getIntent() + " -> " + lower.getIntent()*/ + 
								" (down, score = " + new DecimalFormat("#.##").format(mergeScore(node, lower)) + ")";
						firstNode = node;
						secondNode = lower;
					}
				}
			}
		}
		if(highScore > 0.0) {
			mergeInto(firstNode, secondNode);
			System.out.println(highScoreMerge);
		}
		return highScore;
	}

	private double mergeScore(LatticeNode node, LatticeNode candidate) {
		if(!node.hasOwnObjects() || !candidate.hasOwnObjects() || candidate.numberOfOwnObjects() <= node.numberOfOwnObjects())
			return 0.0;
		double ownObjectRatio = candidate.numberOfOwnObjects()/(double)node.numberOfOwnObjects();
		double percentOfObjects = node.numberOfOwnObjects()/(double)context.getObjects().size()*100.0;
		if(node.lowerNeighbours().contains(candidate))
			return 2*(ownObjectRatio/percentOfObjects);
		return ownObjectRatio/percentOfObjects;
	}
	
	private void mergeInto(LatticeNode firstNode, LatticeNode secondNode) {
		BitSet mergedIntent = (BitSet)secondNode.getIntent().clone();
		for(FormalObject obj : firstNode.ownObjects())
			obj.setIntent(mergedIntent);
	}
}
