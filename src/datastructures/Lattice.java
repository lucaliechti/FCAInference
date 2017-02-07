package datastructures;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import driver.ContextCleanser;
import driver.LatticeBuilder;

public class Lattice {
	private ArrayList<LatticeNode> nodes;
	private ArrayList<LatticeEdge> edges;
	private HashMap<Integer, ArrayList<LatticeNode>> nodesByLevel;
	private FormalContext context;
	private int currentNodeNumber;
	private Dictionary dic;
	private BitSet lastMergedInto; //used to keep track of which node has last been merged into in the tinker algorithm
	private HashMap<String, ArrayList<FormalObject>> bookkeeping; //used to calculate the number of NULLs and legacy values
	private ArrayList<FormalObject> removedSingletons;
	private ContextCleanser cc;
	private long time;
	
	public Lattice(Dictionary _dic, FormalContext _context) {
		this.nodes = new ArrayList<LatticeNode>();
		this.edges = new ArrayList<LatticeEdge>();
		this.context = _context;
		this.currentNodeNumber = 0;
		this.dic = _dic;
		this.nodesByLevel = new HashMap<Integer, ArrayList<LatticeNode>>();
		this.lastMergedInto = null;
		this.bookkeeping = null;
		this.removedSingletons = new ArrayList<FormalObject>();
		this.cc = new ContextCleanser(_dic);
		this.time = 0;
	}
	
	public void clear() {
		nodes.clear();
		edges.clear();
		nodesByLevel.clear();
		currentNodeNumber = 0;
	}

	public String latticeStats() { 
		return context.getObjects().size() + "\t" + types() + "\t" + numberOfAttributes() + "\t" + nodes.size() + "\t" + nodesWithOwnObjects() 
		+ "\t" + edges.size() + "\t" + String.format("%.3f", clusterIndex()) + "\t" + String.format("%.1f", inMajority())
		+ "\t" + String.format("%.1f", inCleanNodes()) + "\t" + String.format("%.1f", nullPercentage()) + "\t" + String.format("%.1f", legacyPercentage()) + "\t" + time;
	}
	

	private int numberOfAttributes() {
		BitSet ORset = new BitSet(dic.getSize());
		for(LatticeNode node : nodes){
			for(FormalObject obj : node.ownObjects()) {
				ORset.or(obj.getIntent());
			}
		}
		return ORset.cardinality();
	}

	public void addNode(LatticeNode node) {
		node.setNodeNumber(++currentNodeNumber);
		nodes.add(node);
	}
	
	public void addEdge(LatticeNode from, LatticeNode to) {
		LatticeEdge edge = new LatticeEdge(from, to);
		edges.add(edge);
		to.addToUpperNeighbours(from);
		from.addToLowerNeighbours(to);
	}
	
	public ArrayList<LatticeNode> getNodes() {
		return this.nodes;
	}

	public void exportLatticeToFile(String outputFile){
//		System.out.print("Writing lattice to file... ");
		String latticeString = "";
		latticeString += "digraph d{\n";
		for(LatticeNode node : nodes)
			latticeString += node.getNodeNumber() 
			+ " [label=\"" + node.getNiceAttributes() //+ node.getIntent() ---------------------excluding intent for the moment
			+ "ext.: " + node.numberOfObjects() + " (" + node.typesOfExtent() + ") "
			+ "\nown: " + node.numberOfOwnObjects()  + " (" + node.typesOfOwnObjects() + ") "
//			+ "\n merges into : " + node.mergesInto()
			+ "\"" + peripheries(node) + color(node) + "]\n";
		for(LatticeEdge edge: edges)
			latticeString += edge.getLowerNodeNumber() + "->" + edge.getUpperNodeNumber() + ";\n";
		latticeString += "}";
		writeToFile(latticeString, outputFile);
//		System.out.println("done.");
	}
	
	//paints the node that was last merged into red
	private String color(LatticeNode node) {
		if(this.lastMergedInto != null && this.lastMergedInto.equals(node.getIntent()))
			return ", style = filled, color = red";
		return "";
	}

	private String peripheries(LatticeNode node) {
		if(node.numberOfOwnObjects() > 0)
			return ", peripheries = 2";
		return "";
	}

	private void writeToFile(String exportString, String outputFile) {
		try {
			PrintWriter out = new PrintWriter(outputFile, "UTF-8");
			out.print(exportString);
			out.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); } 
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
	}

	//together with the next four functions, calculates and inserts edges in a lattice with only nodes.
	public void computeEdges() {
		nodesByLevel = sortNodesIntoLevels(nodes);
		int[] levelArray = extractLevelsAsArray(nodesByLevel);
		for(int levelDistance = 1; levelDistance < levelArray.length; levelDistance++) {
			HashSet<LatticeNode> updatedNodes = addEdgesForLevelDistance(levelArray, levelDistance);
			setTransitiveLinks(updatedNodes);
		}
	}
	
	private HashSet<LatticeNode> addEdgesForLevelDistance(int[] levelArray, int levelDistance) {
		HashSet<LatticeNode> updatedNodes = new HashSet<LatticeNode>();
		for(int i = 0; i < levelArray.length-levelDistance; i++){
			ArrayList<LatticeNode> nodesAtUpperLevel = nodesByLevel.get(levelArray[i]);
			ArrayList<LatticeNode> nodesAtLowerLevel = nodesByLevel.get(levelArray[i+levelDistance]);
			for(LatticeNode upperNode : nodesAtUpperLevel) {
				for(LatticeNode lowerNode : nodesAtLowerLevel) {
					if(!upperNode.canTransitivelyReach(lowerNode) && LatticeBuilder.isSubsetOf(upperNode.getIntent(), lowerNode.getIntent())){
						this.addEdge(upperNode, lowerNode);
						updatedNodes.add(lowerNode);
					}
				}
			}
		}
		return updatedNodes;
	}
	
	private void setTransitiveLinks(HashSet<LatticeNode> updatedNodes) {
		HashMap<Integer, ArrayList<LatticeNode>> updatedNodesByLevel = sortNodesIntoLevels(updatedNodes);
		int[] levelArray = extractLevelsAsArray(updatedNodesByLevel);
		while(levelArray.length > 0) {
			for(LatticeNode lowerNode : updatedNodesByLevel.get(levelArray[levelArray.length-1])) {
				setTransitive(lowerNode);
				addUpdatedNodes(lowerNode, updatedNodesByLevel);
			}
			updatedNodesByLevel.remove(levelArray[levelArray.length-1]);
			levelArray = extractLevelsAsArray(updatedNodesByLevel);
		}
	}
	
	private void addUpdatedNodes(LatticeNode lowerNode, HashMap<Integer, ArrayList<LatticeNode>> updatedNodesByLevel) {
		for(LatticeNode parent : lowerNode.upperNeighbours()){
			if(updatedNodesByLevel.get(parent.getIntent().cardinality()) == null)
				updatedNodesByLevel.put(parent.getIntent().cardinality(), new ArrayList<LatticeNode>());
			if(!updatedNodesByLevel.get(parent.getIntent().cardinality()).contains(parent))
				updatedNodesByLevel.get(parent.getIntent().cardinality()).add(parent);
		}	
	}

	//sets the list of transitively reachable nodes in all upper nodes that can reach target node
	private void setTransitive(LatticeNode lowerNode) {
		for(LatticeNode parent : lowerNode.upperNeighbours()) {
			parent.addToTransitivelyReachableNodes(lowerNode);
			parent.addAllToTransitivelyReachableNodes(lowerNode.getTransitivelyReachableNodes());
		}
	}

	private int[] extractLevelsAsArray(HashMap<Integer, ArrayList<LatticeNode>> nodeMap) {
		Set<Integer> levels = nodeMap.keySet();
		int[] levelArray = new int[levels.size()];
		int pos = 0;
		for(int level : levels) levelArray[pos++] = level;
		Arrays.sort(levelArray);
		return levelArray;
	}
	
	//takes a Collection of LatticeNodes and returns a HashMap where the key is the number of attributes
	private HashMap<Integer, ArrayList<LatticeNode>> sortNodesIntoLevels(Collection<LatticeNode> nodesToSort) {
		HashMap<Integer, ArrayList<LatticeNode>> levels = new HashMap<Integer, ArrayList<LatticeNode>>();
		for(LatticeNode node : nodesToSort) {
			int numAttributes = node.getIntent().cardinality();
			if(levels.get(numAttributes) == null) {
				ArrayList<LatticeNode> newList = new ArrayList<LatticeNode>();
				newList.add(node);
				levels.put(numAttributes, newList);
			}
			else {
				levels.get(numAttributes).add(node);
			}
		}
		return levels;
	}

	public Boolean isEmpty() {
		return (nodes.size() == 0 && edges.size() == 0);
	}
	
	public Boolean containsNodeWithIntent(BitSet intent) {
		for(LatticeNode node : nodes) if(node.getIntent().equals(intent)) return true;
		return false;
	}
	
	public LatticeNode getNodeWithIntent(BitSet intent) {
		for(LatticeNode node : nodes) if(node.getIntent().equals(intent)) return node;
		return null;
	}
	
	public Dictionary getDic() {
		return dic;
	}

	//computes where in the lattice which attribute exists for the first time
	//every attribute has exactly one such node, of which all subnodes contain it
	public void computeAttributes() {
		int[] levelArray = extractLevelsAsArray(nodesByLevel);
		LatticeNode topNode = nodesByLevel.get(levelArray[0]).get(0);
		for(int i = 0; i < topNode.getIntent().size(); i++){
			if(topNode.getIntent().get(i))
				topNode.addToOwnAttributes(dic.getAttribute(i));
		}
		//nodes with upper neighbours
		for(int j = 1; j < levelArray.length; j++) {
			ArrayList<LatticeNode> thisLevelsNodes = nodesByLevel.get(levelArray[j]);
			for(LatticeNode node : thisLevelsNodes) {
				BitSet empty = new BitSet(dic.getSize());
				BitSet thisIntent = (BitSet)node.getIntent().clone();
				for(LatticeNode un : node.upperNeighbours()){
					empty.or(un.getIntent());
				}
				thisIntent.xor(empty);
				for(int k = 0; k < thisIntent.size(); k++){
					if(thisIntent.get(k))
						node.addToOwnAttributes(dic.getAttribute(k));
				}
			}
		}
	}
	
	public int nodesWithOwnObjects() {
		int count = 0;
		for(LatticeNode node : nodes) {
			if(node.numberOfOwnObjects() > 0) count++;
		}
		return count;
	}
	
	public HashMap<Integer, ArrayList<LatticeNode>> nodesByLevel() {
		return nodesByLevel;
	}
	
	public int[] levelArray() {
		return extractLevelsAsArray(nodesByLevel);
	}
	
	private double clusterIndex() {
		double index = 0.0d;
		int[] numbersOfOwnObjects = numbersOfOwnObjects();
		Arrays.sort(numbersOfOwnObjects);
		double[] normalized = new double[numbersOfOwnObjects.length];
		double sum = 0;
		int min = numbersOfOwnObjects[0];
		int max = numbersOfOwnObjects[numbersOfOwnObjects.length-1];
		assert (min <= max);
		//calculate sum of objects
		for(int i = 0; i < numbersOfOwnObjects.length; i++)
			sum += numbersOfOwnObjects[i];
//		System.out.println("Sum: " + sum);
		//normalize
		for(int j = 0; j < numbersOfOwnObjects.length; j++)
			normalized[j] = ((double)numbersOfOwnObjects[j]) / (double)(sum);
		double normalizedSum = 0.0d;
		for(int k = 0; k < normalized.length; k++)//{
			normalizedSum += normalized[k];// System.out.println(normalized[k]); }
		double avg = normalizedSum/(double)nodesWithOwnObjects();
//		System.out.println("Avg.: " + avg);
		double median = 0.5d;
		double standardvalue = avg; //choose here between avg and median
		if(normalized.length %2 == 0)
			median = (normalized[normalized.length/2] + normalized[(normalized.length/2)-1])/2;
		else
			median = normalized[(int)Math.floor(normalized.length/2)];
		//add squared values to index
		for(int k = 0; k < normalized.length; k++)
			index += (normalized[k]-standardvalue)*(normalized[k]-standardvalue);
		index /= Math.sqrt(normalized.length);
		assert (0 <= index && index <= 1);
		return index;
	}

	//returns an int array with all numbers of own objects of nodes, 
	//duplicates included (because we need the average)
	private int[] numbersOfOwnObjects() {
		int[] numbers = new int[nodesWithOwnObjects()];
		int pos = 0;
		for(LatticeNode node : nodes) {
			if(node.numberOfOwnObjects() > 0) numbers[pos++] = node.numberOfOwnObjects();
		}
		assert(pos == nodesWithOwnObjects()-1);
		return numbers;
	}

	public void setLastMergedInto(BitSet intent) {
		this.lastMergedInto = intent;
	}
	
	public double inMajority() {
		int majority = 0;
		int total = 0;
		for(LatticeNode node : nodes) {
			if(node.hasOwnObjects()){
				majority += node.majority(); 
				total += node.numberOfOwnObjects();
			}
		}
		return ((double)majority/(double)total)*100;
	}
	
	public double inCleanNodes() {
		int inClean = 0;
		int total = 0;
		for(LatticeNode node : nodes) {
			if(node.hasOwnObjects()){
				if(node.typesOfFormalObjects(node.ownObjects()).substring(0,4).equals("100%"))
					inClean += node.numberOfOwnObjects();
				total += node.numberOfOwnObjects();
			}
		}
		return ((double)inClean/(double)total)*100;
	}
	
	public Boolean bookkeepingIsNull() {
		return bookkeeping == null;
	}
	
	public void initialiseBookkeeping() {
		ContextCleanser cc = new ContextCleanser(dic);
		bookkeeping = new HashMap<String, ArrayList<FormalObject>>();
		for(LatticeNode node : nodes){
			if(node.hasOwnObjects()){
				for(FormalObject ownObject : node.ownObjects()){
					if(bookkeeping.containsKey(cc.bitsetHash(ownObject.getIntent())))
						bookkeeping.get(cc.bitsetHash(ownObject.getIntent())).add(ownObject);
					else {
						ArrayList<FormalObject> newList = new ArrayList<FormalObject>();
						newList.add(ownObject);
						bookkeeping.put(cc.bitsetHash(ownObject.getIntent()), newList);
					}
				}
			}
		}
	}

	//when all objects of one node (the mergee) are merged into another node (the merger),
	//we keep track of this in the bookkeeping datastructure used to calculate NULLs and legacies
	public void updateBookkeeping(LatticeNode mergee, LatticeNode merger) {
		String mergeeHash = cc.bitsetHash(mergee.getIntent());
		String mergerHash = cc.bitsetHash(merger.getIntent());
		ArrayList<FormalObject> mergedObjects = bookkeeping.get(mergeeHash);
		for(FormalObject obj : mergedObjects){
			FormalObject copy = new FormalObject();
			copy.setIntent((BitSet)obj.getIntent().clone());
			bookkeeping.get(mergerHash).add(copy);
		}
		bookkeeping.remove(mergeeHash);
	}
	
	private int totalCardinality() {
		int card = 0;
		for(LatticeNode node : nodes)
			card += node.getIntent().cardinality()*node.numberOfOwnObjects();
		return card;
	}
	
	private int nulls() {
		int nulls = 0;
		for(String hash : bookkeeping.keySet()){
			ArrayList<FormalObject> nodeObjects = bookkeeping.get(hash);
			BitSet archetype = bitsetFromHash(hash/*, nodeObjects*/);
//			System.out.println(archetype);
			for(FormalObject comp : nodeObjects){
				BitSet nullSet = (BitSet)archetype.clone();
				nullSet.xor(comp.getIntent());
				nullSet.and(archetype);
				nulls += nullSet.cardinality();
			}
		}
		return nulls;
	}
	
	private int legacies() {
		int legacies = 0;
		for(String hash : bookkeeping.keySet()){
			ArrayList<FormalObject> nodeObjects = bookkeeping.get(hash);
			BitSet archetype = bitsetFromHash(hash/*, nodeObjects*/);
			for(FormalObject comp : nodeObjects){
				BitSet legSet = (BitSet)archetype.clone();
				legSet.xor(comp.getIntent());
				legSet.and(comp.getIntent());
				legacies += legSet.cardinality();
			}
		}
		return legacies;
	}
	
	private BitSet bitsetFromHash(String hash/*, ArrayList<FormalObject> objectArray*/) {
//		for(FormalObject obj : objectArray){
//			if(cc.bitsetHash(obj.getIntent()).equals(hash))
//				return (BitSet)obj.getIntent().clone();
//		}
//		return null;
		BitSet set = new BitSet(hash.length());
		for(int i = 0; i < hash.length(); i++){
			if(hash.charAt(i) == '1')
				set.set(i);
		}
		return set;
	}
	
	private double nullPercentage() {
		return (double)nulls()/(double)totalCardinality()*100;
	}
	
	private double legacyPercentage() {
		return (double)legacies()/(double)totalCardinality()*100;
	}

	public void addToRemovedSingletons(FormalObject singleton) {
		this.removedSingletons.add(singleton);
	}

	public void retrofitSingletons() {
		for(FormalObject single : removedSingletons) {
			LatticeNode bestFit = findBestNodeFit(single);//find suitable latticeNode WITH own objects for each formalObject in singleton array.
			//add the objects to those nodes. TODO: Does this really have to require two function calls?
			single.setIntent(bestFit.getIntent());/////////////////////////////////////////////////////
			bestFit.addObject(single);
			bestFit.addToOwnObjects(single);
			//update the bookkeeping datastructure, ie. add the formalObject to the hash of the closest node. Do not re-compute anything.
			bookkeeping.get(cc.bitsetHash(bestFit.getIntent())).add(single);
			context.addObject(single);
		}
	}

	private LatticeNode findBestNodeFit(FormalObject single) {
		BitSet singleIntent = single.getIntent();
		LatticeNode bestFit = null;
		int bestFitScore = dic.getSize(); //worst possible score: all attributes exactly XOR
		int bestFitOwnObjects = 0;
		for(LatticeNode node : nodes){
			BitSet nodeIntent = (BitSet)node.getIntent().clone();
			nodeIntent.xor(singleIntent);
			int currentScore = nodeIntent.cardinality();
			int currentOwnObjects = node.numberOfOwnObjects();
			if(currentScore <= bestFitScore && currentOwnObjects > bestFitOwnObjects){
				bestFit = node;
				bestFitScore = currentScore;
				bestFitOwnObjects = currentOwnObjects;
			}
		}
		assert (bestFit != null);
//		System.out.println("Retrofitting " + single.getIntent() + " into " + bestFit.getIntent() + " (score = " + bestFitScore + ", own = " + bestFitOwnObjects + ")");
		return bestFit;
	}
	
	public int types() {
		HashSet<String> types = new HashSet<String>();
		for(FormalObject obj : context.getObjects())
			types.add(obj.getName());
		return types.size();
	}

	public void setTime(long timeElapsed) {
		this.time = timeElapsed;
	}
}
