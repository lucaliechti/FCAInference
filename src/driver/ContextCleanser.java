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
		System.out.print("Nr of attributes after:  " + (context.numberOfAttributes()-deleted) + ", ");
	}
	
	public void mergeNodes(int factor, int attrDiff) {
		System.out.println("---BEGIN CLEANSING---");
		System.out.println("Merging all nodes with their lower neighbours if they have "
		+ factor + " times more own objects and at most " + attrDiff + " more attributes.");
		System.out.println("Lattice stats before: " + lattice.latticeStats());
		HashMap<Integer, ArrayList<LatticeNode>> latticeLevelNodes = lattice.nodesByLevel();
		int[] levelArray = lattice.levelArray();
		for(int i = 0; i < levelArray.length; i++) {
			ArrayList<LatticeNode> thisLevelNodes = latticeLevelNodes.get(levelArray[i]);
			for(LatticeNode node : thisLevelNodes) {
				if(node.numberOfObjects() > 0) {
					HashSet<LatticeNode> lowerNeighbours = node.lowerNeighbours();
					//find out which lower node has the most own objects
					int mostOwnObjects = 0;
					LatticeNode mergeCandidate = null;
					for(LatticeNode lowerNode : lowerNeighbours){
						if(lowerNode.numberOfOwnObjects() > mostOwnObjects){
							mostOwnObjects = lowerNode.numberOfOwnObjects();
							mergeCandidate = lowerNode;
						}
					}
					//if criteria fit, merge upper node into lower
					if(mergeCandidate != null 
							&& node.numberOfOwnObjects()*10 <= mergeCandidate.numberOfOwnObjects()
					   		&& node.getIntent().cardinality() + attrDiff >= mergeCandidate.getIntent().cardinality()){
						for(FormalObject obj : node.ownObjects())
							obj.setIntent((BitSet)mergeCandidate.getIntent().clone());
					}
				}
			}
		}
	}
}
