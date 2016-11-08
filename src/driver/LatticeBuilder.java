package driver;

import java.util.ArrayList;
import java.util.BitSet;

import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;

public class LatticeBuilder {

	public LatticeBuilder() {}
	
	public Lattice buildLattice(FormalContext context){
		Lattice lattice = new Lattice();
		
		//add nodes to lattice
		ArrayList<Integer> seenConcepts = new ArrayList<Integer>();
		for(FormalObject obj : context.getObjects()){
			BitSet bs = obj.getExtent();
			int hash = bs.hashCode();
			if(!seenConcepts.contains(hash)){//for the first object with this extent
				seenConcepts.add(hash);
				lattice.addToNodes(bs);
			}
			else { lattice.increaseNodeCount(bs); }
		}
		
		//add edges to lattice
		//all relevant datastructures are in the lattice, so we do this from there
		lattice.computeEdges();
		
		return lattice;
	}
}