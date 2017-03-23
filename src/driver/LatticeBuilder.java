package driver;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

import datastructures.FormalConcept;
import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import datastructures.LatticeNode;
import tools.Timer;

public class LatticeBuilder {
	
	private FormalContext context;
	private Lattice lattice;
	private ArrayList<FormalObject> alreadyAddedObjects;
	private FormalConcept maximalConcept;

	public LatticeBuilder(FormalContext _context) {
		this.context = _context;
		this.lattice = new Lattice(_context.getDictionary(), _context);
		this.alreadyAddedObjects = new ArrayList<FormalObject>();
		this.maximalConcept = null;
	}

	public Lattice buildLattice(){
		Timer timer = new Timer();
		//Norris algorithm
		for(FormalObject g : context.getObjects()) add(g);
		lattice.setTime(timer.timeElapsed());
//		System.out.println("Added all objects in " + timer.timeElapsed() + " ms."); timer.reset();
		addNodeWithAllAttributes();
//		System.out.println("Added node with all attributes in " + timer.timeElapsed() + " ms."); timer.reset();
		computeExtents();
//		System.out.println("Computed extents in " + timer.timeElapsed() + " ms."); timer.reset();
		lattice.computeEdges();
//		System.out.println("Computed edges in " + timer.timeElapsed() + " ms."); timer.reset();
		lattice.computeAttributes();
//		System.out.println("Computed which attribute enters where in " + timer.timeElapsed() + " ms.");
		if(lattice.bookkeepingIsNull()) lattice.initialiseBookkeeping();
		alreadyAddedObjects.clear();
		return lattice;
	}

	//Norris algorithm add function
	private void add(FormalObject g) {
		//this happens only at the very beginning
		if (lattice.isEmpty())	{
			FormalConcept maxConcept = computeMaximalConcept();
			maximalConcept = maxConcept;
			lattice.addNode(new LatticeNode(maxConcept.getExtent(), maxConcept.getIntent(), lattice.getDic()));
			lattice.getNodes().get(0).setTopNode();
		}
		for(int i = 0; i < lattice.getNodes().size(); i++) {
			LatticeNode ab = lattice.getNodes().get(i);
			if(isSubsetOf(ab.getIntent(), g.getIntent())) {
				ab.addObject(g);
			}
			else {
				BitSet d = (BitSet)g.getIntent().clone();
				d.and(ab.getIntent());
				if(hFunction(g, ab.getExtent())) {
					ab.addObject(g); //weird: if this is deleted, the number of concepts can vary!!
					if(!lattice.containsNodeWithIntent(d)) {
						LatticeNode newNode = new LatticeNode(ab.getExtent(), d, lattice.getDic());
						lattice.addNode(newNode);
					}
				}
			}
		}
		if(hFunction(g, new HashSet<FormalObject>())){
			HashSet<FormalObject> gSet = new HashSet<FormalObject>();
			gSet.add(g);
			LatticeNode newNode = new LatticeNode(gSet, g.getIntent(), lattice.getDic());
			if(!maximalConcept.getIntent().equals(newNode.getIntent())) //only add an object if it hasn't already been added as the maximal concept!
					lattice.addNode(newNode);
		}
		alreadyAddedObjects.add(g);
	}
	
	//helper function for Norris algorithm
	private boolean hFunction(FormalObject g, HashSet<FormalObject> extent) {
		for(FormalObject h : context.getObjects()) {
			if(h != g && alreadyAddedObjects.contains(h) && !extent.contains(h) && isSubsetOf(g.getIntent(), h.getIntent())) {
				return false;
			}
		}
		return true;
	}
	
	private void addNodeWithAllAttributes() {	
		BitSet allAttributes = new BitSet(lattice.getDic().getSize());
		allAttributes.set(0, lattice.getDic().getSize()); //second parameter is EXCLUSIVE, so not dictionarySize-1
		if(!lattice.containsNodeWithIntent(allAttributes)){
			HashSet<FormalObject> extentWithAllAttributes = context.getDerivationOfAttributes(allAttributes);
			LatticeNode nodeWithAllAttributes = new LatticeNode(extentWithAllAttributes, allAttributes, lattice.getDic());
			lattice.addNode(nodeWithAllAttributes);
		}
	}
	
	private void computeExtents() {
		for(LatticeNode node : lattice.getNodes()) {
			node.getExtent().clear();
			for(FormalObject obj : context.getObjects()) {
				if(isSubsetOf(node.getIntent(), obj.getIntent())) node.addObject(obj);
				if(node.getIntent().equals(obj.getIntent())) node.addToOwnObjects(obj);
			}
		}
	}

	public static Boolean isSubsetOf(BitSet first, BitSet second) {
		BitSet firstCopy = (BitSet)first.clone();
		firstCopy.and(second);
		return(first.equals(firstCopy));
	}
	
	private FormalConcept computeMaximalConcept() {
		ArrayList<FormalObject> objects = context.getObjects();
		ArrayList<BitSet> intents = new ArrayList<BitSet>();
		for(FormalObject g : objects)
			intents.add(g.getIntent());
		BitSet seed = new BitSet(context.numberOfAttributes());
		seed.set(0,seed.size(), true);
		for(BitSet bs : intents)
			seed.and(bs);
		HashSet<FormalObject> seedDerivation = context.getDerivationOfAttributes(seed);
		return new FormalConcept(seedDerivation, seed);
	}
}