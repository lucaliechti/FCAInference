package driver;

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
	public void removeAttributesWithLeastSupport() {
		System.out.println("---BEGIN CLEANSING---");
		HashMap<String, Integer> attributeSupport = context.getAttributeSupport();
		HashSet<Integer> supportSet = new HashSet<Integer>();
		supportSet.addAll(attributeSupport.values());
		Integer[] supportArray = supportSet.toArray(new Integer[supportSet.size()]);
		Arrays.sort(supportArray);
		final int TRESHOLD = supportArray[3];
		System.out.println("Deleting all attributes that occur at most " + TRESHOLD + " times.");
		int deleted = 0;
		System.out.print("Nr of attributes before: " + context.numberOfAttributes() + ", ");
		System.out.println("Lattice stats before: " + lattice.latticeStats());
		for(String attr : context.getDictionary().getContents()){
			if(attributeSupport.get(attr) <= TRESHOLD){
				deleted++;
				context.removeAttribute(attr);
			}
		}
		System.out.print("Nr of attributes after: " + (context.numberOfAttributes()-deleted) + ", ");
	}
	
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
				if(node.numberOfOwnObjects() > 0 && node.numberOfOwnObjects() < (percent*context.getObjects().size()/100)) {
					//find out which lower node has the most own objects
					LatticeNode mergeCandidate = findMergeCandidate(node.lowerNeighbours());
					//if criteria fit, merge upper node into lower
					if(isMergeCandidateFor(mergeCandidate, node)){
						for(FormalObject obj : node.ownObjects()){
							obj.setIntent((BitSet)mergeCandidate.getIntent().clone());
							//TODO: recompute upper and lower neighbours
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
}
