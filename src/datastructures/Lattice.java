package datastructures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Lattice {
	private ArrayList<LatticeNode> nodes;
	private ArrayList<LatticeEdge> edges;
	private HashMap<Integer, HashMap<Integer, LatticeNode>> levels;
	private HashMap<Integer, BitSet> hashes;
	
	public Lattice() {
		this.nodes = new ArrayList<LatticeNode>();
		this.edges = new ArrayList<LatticeEdge>();
		this.levels = new HashMap<Integer, HashMap<Integer, LatticeNode>>();
		this.hashes = new HashMap<Integer, BitSet>();
	}
	
	public void addToNodes(BitSet bs) {
		int card = bs.cardinality();
		int hash = bs.hashCode();
		if(!levels.containsKey(card))	levels.put(card, new HashMap<Integer, LatticeNode>()); //new level
		LatticeNode newNode = new LatticeNode(bs);
		nodes.add(newNode);
		levels.get(card).put(hash, newNode);
		hashes.put(hash, bs);
	}

	public void increaseNodeCount(BitSet bs) {
		int card = bs.cardinality();
		int hash = bs.hashCode();
		//TODO: Make prettier
		LatticeNode node = levels.get(card).get(hash);
		node.increaseNumberOfObjects();
		HashMap<Integer, LatticeNode> level = levels.get(card);
		level.put(hash, node);
		levels.put(card, level);
	}

	public void computeEdges() {
		Set<Integer> differentLevels = levels.keySet(); //ordered!
		for(int lowerLevel : differentLevels){
			Iterator<LatticeNode> lowerIt = levels.get(lowerLevel).values().iterator();
			while(lowerIt.hasNext()){
				LatticeNode lowerNode = lowerIt.next();
				BitSet coveredAttributes = (BitSet)lowerNode.getExtent().clone(); //helps avoid transitive duplicates of edges
				for(int upperLevel : differentLevels){
					if(0 < lowerLevel && lowerLevel < upperLevel){
						Iterator<LatticeNode> upperIt = levels.get(upperLevel).values().iterator();
						while(upperIt.hasNext()){
							LatticeNode upperNode = upperIt.next();
							if(lowerNode.isSubsetOf(upperNode)){
								BitSet covered = (BitSet)coveredAttributes.clone();
								covered.or(upperNode.getExtent());
								if(!covered.equals(coveredAttributes)) {//if we haven't "covered" all attributes in the potential new node
									coveredAttributes.or(upperNode.getExtent());
									edges.add(new LatticeEdge(lowerNode, upperNode));
								}
							}
						}
					}	
				}
			}
		}
	}

	@Override
	public String toString() {
		String str = "";
		str += "\nLattice stats: \n";
		str += "Different levels: " + levels.keySet() + "\n";
		str += "Different nodes: " + nodes.size() + "\n";
//		for(LatticeNode ln : nodes)
//			str += ln + "\n";
		return str;
	}
}
