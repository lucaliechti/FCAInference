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

import driver.LatticeBuilder;

public class Lattice {
	private ArrayList<LatticeNode> nodes;
	private ArrayList<LatticeEdge> edges;
	private HashMap<Integer, ArrayList<LatticeNode>> nodesByLevel;
	private int currentNodeNumber;
	private Dictionary dic;
	
	public Lattice(Dictionary _dic) {
		this.nodes = new ArrayList<LatticeNode>();
		this.edges = new ArrayList<LatticeEdge>();
		this.currentNodeNumber = 0;
		this.dic = _dic;
		this.nodesByLevel = new HashMap<Integer, ArrayList<LatticeNode>>();
	}
	
	public void clear() {
		nodes.clear();
		edges.clear();
		nodesByLevel.clear();
		currentNodeNumber = 0;
	}

	public String latticeStats() { 
		return "Nodes: " + nodes.size() + "\twith own objects: " + nodesWithOwnObjects() + "\tedges: " + edges.size() + "\tclusterIndex: " + String.format("%.3f", clusterIndex());
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
		System.out.print("Writing lattice to file... ");
		String latticeString = "";
		latticeString += "digraph d{\n";
		for(LatticeNode node : nodes)
			latticeString += node.getNodeNumber() 
			+ " [label=\"" + node.getNiceAttributes() + node.getIntent() 
			+ "\next.: " + node.numberOfObjects() + " (" + node.typesOfExtent() + ") "
			+ "\nown: " + node.numberOfOwnObjects()  + " (" + node.typesOfOwnObjects() + ") "
			+ "\"" + peripheries(node) + "]\n";
		for(LatticeEdge edge: edges)
			latticeString += edge.getLowerNodeNumber() + "->" + edge.getUpperNodeNumber() + ";\n";
		latticeString += "}";
		writeToFile(latticeString, outputFile);
		System.out.println("done.");
	}

	private String peripheries(LatticeNode node) {
		if(node.numberOfOwnObjects() > 0){
			return ", peripheries = 2"; }
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
		int span = max - min;
		//normalize
		for(int i = 0; i < numbersOfOwnObjects.length; i++)// { System.out.print("Normalizing " + numbersOfOwnObjects[i] + ": ");
			normalized[i] = ((double)numbersOfOwnObjects[i] - min) / (double)(span);// System.out.println("Normalized = " + normalized[i]); }
		//calculate sum
		for(int j = 0; j < normalized.length; j++)
			sum += normalized[j];
		double avg = sum/normalized.length;
		double median = 0.5d;
		double standardvalue = median; //choose here between avg and median
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
}
