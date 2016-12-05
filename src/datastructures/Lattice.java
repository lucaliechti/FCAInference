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

	public String latticeStats() {
		String str = "\n";
//		str += "Lattice nodes:";
//		for(LatticeNode node : nodes)
//			str += "\n" + node.getNiceString() ;//+ ", " + node.getExtent().size(); //node.getNiceExtentString();
		System.out.println("Nodes: " + nodes.size() + ", edges: " + edges.size());
//		str += "\n";
		return str;
	}
	
	public void addNode(LatticeNode node) {
		node.setNodeNumber(++currentNodeNumber);
		nodes.add(node);
	}
	
	public void addEdge(LatticeNode from, LatticeNode to) {
		LatticeEdge edge = new LatticeEdge(from, to);
		edges.add(edge);
		to.addToUpperNeighbours(from);
	}
	
	public ArrayList<LatticeNode> getNodes() {
		return this.nodes;
	}

	public void exportLatticeToFile(String outputFile){
		System.out.print("Writing lattice to file... ");
		String latticeString = "";
		latticeString += "digraph d{\n";
		for(LatticeNode node : nodes)
			latticeString += node.getNodeNumber() + " [label=\"" + node.getIntent() + "\"]\n";
		for(LatticeEdge edge: edges)
			latticeString += edge.getLowerNodeNumber() + "->" + edge.getUpperNodeNumber() + ";\n";
		latticeString += "}";
		writeToFile(latticeString, outputFile);
		System.out.println("done.");
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
			System.out.println("distance = " + levelDistance);
			HashSet<LatticeNode> newIncomingEdge = addEdgesForLevelDistance(levelArray, levelDistance);
			HashSet<LatticeNode> alreadyVisited = new HashSet<LatticeNode>();
			setTransitiveLinks(newIncomingEdge, alreadyVisited);
		}
	}
	
	private HashSet<LatticeNode> addEdgesForLevelDistance(int[] levelArray, int levelDistance) {
		HashSet<LatticeNode> updatedNodes = new HashSet<LatticeNode>();
		for(int i = 0; i < levelArray.length-levelDistance; i++){
			ArrayList<LatticeNode> nodesAtUpperLevel = nodesByLevel.get(levelArray[i]);
			ArrayList<LatticeNode> nodesAtLowerLevel = nodesByLevel.get(levelArray[i+levelDistance]);
			System.out.println("  upperLevel = " + levelArray[i] + ", lowerLevel = " + levelArray[i+levelDistance]);
			for(LatticeNode upperNode : nodesAtUpperLevel) {
				for(LatticeNode lowerNode : nodesAtLowerLevel) {
					if(LatticeBuilder.isSubsetOf(upperNode.getIntent(), lowerNode.getIntent()) && !upperNode.canTransitivelyReach(lowerNode)){
						this.addEdge(upperNode, lowerNode);
						System.out.println("    adding edge " + upperNode.getIntent() + " -> " + lowerNode.getIntent());
						updatedNodes.add(lowerNode);
					}
				}
			}
		}
		System.out.println("  Added edges of distance " + levelDistance + ". Updated nodes : ");
		for(LatticeNode node : updatedNodes) System.out.println(node.getIntent() + ", ");
		return updatedNodes;
	}
	
	private void setTransitiveLinks(HashSet<LatticeNode> updatedNodes, HashSet<LatticeNode> alreadyVisited) {
		System.out.println("Call to transitive links.");
		System.out.println("updated nodes = "); for(LatticeNode n : updatedNodes) System.out.println(n.getIntent());
		System.out.println("already visited nodes = "); for(LatticeNode n : alreadyVisited) System.out.println(n.getIntent());
		HashMap<Integer, ArrayList<LatticeNode>> updatedNodesByLevel = sortNodesIntoLevels(updatedNodes);
		System.out.println("keyset = " + updatedNodesByLevel.keySet());
		int[] levelArray = extractLevelsAsArray(updatedNodesByLevel);
		for(int i = levelArray.length-1; 0 <= i; i--) {
			for(LatticeNode lowerNode : updatedNodesByLevel.get(levelArray[i])) {
				if(!alreadyVisited.contains(lowerNode) && updatedNodes.contains(lowerNode)){
					System.out.println("      setting transitive nodes for " + lowerNode.getIntent());
					alreadyVisited = setTransitive(lowerNode, updatedNodes, alreadyVisited);
				}
			}
		}
	}
	
	//sets the list of transitively reachable nodes in all upper nodes that can reach target node, recursively
	private HashSet<LatticeNode> setTransitive(LatticeNode lowerNode, HashSet<LatticeNode> updatedNodes, HashSet<LatticeNode> alreadyVisited) {
		for(LatticeNode parent : lowerNode.getUpperNeighbours()) {
		    System.out.println("        " + lowerNode.getIntent() + " is reachable from " + parent.getIntent());
			parent.addToTransitivelyReachableNodes(lowerNode);
			parent.addAllToTransitivelyReachableNodes(lowerNode.getTransitivelyReachableNodes());
			alreadyVisited = setTransitive(parent, updatedNodes, alreadyVisited);
		}
		alreadyVisited.add(lowerNode);
//		System.out.println("visited " + lowerNode.getIntent());
		return alreadyVisited;
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
}
