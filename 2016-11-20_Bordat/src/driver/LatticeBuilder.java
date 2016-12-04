package driver;

import java.util.ArrayList;
import java.util.BitSet;

import datastructures.FormalConcept;
import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import datastructures.LatticeNode;

public class LatticeBuilder {
	
	private FormalContext context;
	private Lattice lattice;

	public LatticeBuilder(FormalContext _context) {
		this.context = _context;
		this.lattice = new Lattice(_context.getDictionary());
	}

	public Lattice buildLattice(){
		System.out.print("Building lattice... \n");
		System.out.println("-------------------------");
		System.out.println("Maximal concept: " + computeMaximalConcept().getIntent());
		process(computeMaximalConcept(), computeMaximalConcept().getIntent());
		System.out.println("-------------------------");
		System.out.println("done.");
		return lattice;
	}
	
	private void process(FormalConcept concept, BitSet attributes) {
		System.out.println("call to process");
		LatticeNode node = new LatticeNode(concept.getIntent());
		lattice.addNode(node);
		ArrayList<FormalConcept> lowerNeighbours = lowerNeighbours(concept);
		//concept.addAllToLowerNeighbours(lowerNeighbours);
		for(FormalConcept neighbour : lowerNeighbours){
			System.out.println("neighbour: " + neighbour.getIntent());
			BitSet c = (BitSet)attributes.clone();
			c.and(neighbour.getIntent());
			LatticeNode neighbourNode;
			if(c.equals(concept.getIntent())){
				attributes.or(neighbour.getIntent());
				process(neighbour, attributes);
				concept.addToLowerNeighbours(neighbour);
				neighbourNode = new LatticeNode(neighbour.getIntent());
			}
			else {
				FormalConcept nextConcept = findConcept(concept, neighbour);
				System.out.println("process method, nextConcept intent: " + nextConcept.getIntent());
				neighbour.addToLowerNeighbours(nextConcept);
				neighbourNode = new LatticeNode(nextConcept.getIntent());
			}
			//node.addToLowerNeighbours(neighbourNode);
			lattice.addEdge(node, neighbourNode);
		}
	}
	
	private ArrayList<FormalConcept> lowerNeighbours(FormalConcept fc) {
		System.out.println("call to lowerNeighbours");
		ArrayList<FormalObject> a = fc.getExtent();
		System.out.println("  size of a = " + a.size());
		ArrayList<FormalConcept> lowerNeighbours = new ArrayList<FormalConcept>();
		BitSet c = (BitSet)fc.getIntent().clone();
		System.out.println("  c = " + c);
		
		//set the first g
		FormalObject g = a.get(a.size()-1); //if we don't find a suitable element
		int foundG = a.size()-1;
		for(int i = 0; i < a.size(); i++) { //these are candidates for g
			System.out.println("  testing for g at pos " + i);
			FormalObject gCand = a.get(i);
			BitSet gCandDer = gCand.getIntent();
			BitSet gCandDerCopy = (BitSet)gCandDer.clone();
			gCandDerCopy.and(c);
			System.out.println("  g AND c = " + gCandDerCopy);
			if(!gCandDerCopy.equals(gCandDer)){
				System.out.println("  found g!");
				g = gCand;
				System.out.println("  g = " + g.getName() + ", " + g.getIntent());
				foundG = i;
				break;
			}
		}
		System.out.println("  found g at " + foundG);
		
		//g loop
		while (!(g == a.get(a.size()-1))){
			System.out.println("\n    entering g loop");
			System.out.println("    setting e, f, h...");
			ArrayList<FormalObject> e = new ArrayList<FormalObject>();
			e.add(g);
			BitSet f = (BitSet)g.getIntent().clone(); //should calculate derivation, but works here for the moment
			FormalObject h = g;
			int foundH = foundG;
			
			//h loop
			while(!(h == a.get(a.size()-1))) {
				System.out.println("\n      entering h loop");
				//System.out.println("\t\t\th = " + h + ", letztes von a = " + a.get(a.size()-1));
				h = a.get(++foundH);
				System.out.println("      h = " + foundH + ": " + h.getName() + ", " + h.getIntent());
				BitSet fCopy = (BitSet)f.clone();
				System.out.println("      f = " + fCopy);
				BitSet hDer = h.getIntent();
				System.out.println("      hDer = " + hDer);
				fCopy.and(hDer);
				BitSet fCopy2 = (BitSet)fCopy.clone();
				fCopy.and(c);
				System.out.println("      c = " + c);
				System.out.println("      f AND hDer = " + fCopy2 + ", f AND hDer AND c = " + fCopy);
				if(!(fCopy.equals(fCopy2))){
					System.out.println("      adding h = " + h.getName() + ", " + h.getIntent() + " to e");
					e.add(h);
					f.and(hDer);
					System.out.println("      f is now : " + f);
				}
				else {
					System.out.println("      not adding anything to e here!");
				}
			}
			BitSet fCopy3 = (BitSet)f.clone();
			fCopy3.and(c);
			System.out.println("    Step 4.5: f AND c = " + fCopy3);
			if(fCopy3.equals(fc.getIntent())){
				System.out.println("    because " + fCopy3 + " equals " + fc.getIntent() + ":");
				System.out.println("    added " + e.size() + " objects with common intent = " + f + " to LN");
				lowerNeighbours.add(new FormalConcept(e,f));
			}
			c.or(f);
			System.out.println("    c after g loop: " + c);
			
			//find next g
			g = a.get(a.size()-1);
			for(int j = 0; j < a.size(); j++) { //these are candidates for g
				FormalObject gCand = a.get(j);
				BitSet gCandDer = gCand.getIntent();
				BitSet gCandDerCopy = (BitSet)gCandDer.clone();
				gCandDerCopy.and(c);
				if(!gCandDerCopy.equals(gCandDer)){
					System.out.println("  found next g!");
					g = gCand;
					foundG = j;
					break;
				}
			}
			System.out.println("  new g: " + g.getName() + g.getIntent());
		}
		System.out.println("\nreturning " + lowerNeighbours.size());
		return lowerNeighbours;
	}
	
	private FormalConcept findConcept(FormalConcept start, FormalConcept wanted) {
		System.out.println("call to find");
		System.out.println("concept has " + start.getLowerNeighbours().size() + " lower neighbours");
		FormalConcept found = null;
		BitSet d = (BitSet)wanted.getIntent().clone();
		ArrayList<FormalConcept> startNeighbours = start.getLowerNeighbours();
		for(FormalConcept child : startNeighbours){
			BitSet f = (BitSet)child.getIntent().clone();
			f.and(d);
			if(f.equals(child.getIntent())){
				if(!child.getIntent().equals(d))
					found = findConcept(child, wanted);
				else
					found = child;
			}
		}
		System.out.println("end of call to find, returned concept = " + found.getIntent());
		return found;
	}
	
	private FormalConcept computeMaximalConcept() {
		ArrayList<FormalObject> objects = context.getObjects();
		ArrayList<BitSet> intents = new ArrayList<BitSet>();
		for(FormalObject obj : objects)
			intents.add(obj.getIntent());
		BitSet seed = new BitSet(context.numberOfAttributes());
		seed.set(0,seed.size(), true);
		for(BitSet bs : intents)
			seed.and(bs);
		ArrayList<FormalObject> seedDerivation = context.getDerivationOfAttributes(seed);
		return new FormalConcept(seedDerivation, seed);
	}
}