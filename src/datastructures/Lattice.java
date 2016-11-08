package datastructures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

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
		//System.out.println("adding " + card + " " + hash + " " + levels.get(card).get(hash));
	}

	public void increaseNodeCount(BitSet bs) {
		int card = bs.cardinality();
		int hash = bs.hashCode();
		//System.out.println("increasing " + card + " " + hash + " " + levels.get(card).get(hash));
		LatticeNode node = levels.get(card).get(hash);
		node.increaseNumberOfObjects();
		HashMap<Integer, LatticeNode> level = levels.get(card);
		level.put(hash, node);
		levels.put(card, level);
	}

	public void computeEdges() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "\nLattice stats: \n";
		str += "Different levels: " + levels.keySet() + "\n";
		str += "Different nodes: " + nodes.size() + "\n";
		for(LatticeNode ln : nodes)
			str += ln + "\n";
		return str;
	}
}
