package datastructures;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
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
//			str += "\n" + node.getIntent() + ", " + node.getExtent().size(); //node.getNiceExtentString();
		System.out.println(nodes.size() + " different nodes computed.");
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
		nodesByLevel = sortNodesIntoLevels();
		int[] levelArray = extractLevelsAsArray();
		int levelDistance = 1;
		addNodesForLevelDistance(levelArray, levelDistance);
		setTransitiveLinks();
	}
	
	private void setTransitiveLinks() {
		// TODO Auto-generated method stub
		
	}
	
	private int[] extractLevelsAsArray() {
		Set<Integer> levels = nodesByLevel.keySet();
		int[] levelArray = new int[levels.size()];
		int pos = 0;
		for(int level : levels) levelArray[pos++] = level;
		Arrays.sort(levelArray);
		return levelArray;
	}

	private void addNodesForLevelDistance(int[] levelArray, int levelDistance) {
		for(int i = 0; i < levelArray.length-1; i++){
			ArrayList<LatticeNode> nodesAtUpperLevel = nodesByLevel.get(i);
			ArrayList<LatticeNode> nodesAtLowerLevel = nodesByLevel.get(i+levelDistance);
			for(LatticeNode upperNode : nodesAtUpperLevel) {
				for(LatticeNode lowerNode : nodesAtLowerLevel) {
					//TODO: Must not be transitively reachable
					if(LatticeBuilder.isSubsetOf(upperNode.getIntent(), lowerNode.getIntent()))
						this.addEdge(upperNode, lowerNode);
				}
			}
		}
	}
	
	private HashMap<Integer, ArrayList<LatticeNode>> sortNodesIntoLevels() {
		HashMap<Integer, ArrayList<LatticeNode>> levels = new HashMap<Integer, ArrayList<LatticeNode>>();
		for(LatticeNode node : nodes) {
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
