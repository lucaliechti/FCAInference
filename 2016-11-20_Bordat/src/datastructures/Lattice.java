package datastructures;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class Lattice {
	private ArrayList<LatticeNode> nodes;
	private ArrayList<LatticeEdge> edges;
	private HashMap<Integer, HashMap<Integer, LatticeNode>> levels;
	private int currentNodeNumber;
	private Dictionary dic;
	
	public Lattice(Dictionary _dic) {
		this.nodes = new ArrayList<LatticeNode>();
		this.edges = new ArrayList<LatticeEdge>();
		this.currentNodeNumber = 0;
		this.dic = _dic;
	}

	public String latticeStats() {
		String str = "";
		str += "\nLattice stats: \n";
		str += "Different levels: " + levels.keySet() + "\n";
		str += "Different nodes: " + nodes.size() + "\n";
		str += "Number of edges: " + edges.size() + "\n";
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

	public void exportLatticeToFile(String outputFile){
		System.out.print("Writing lattice to file... ");
		String latticeString = "";
		latticeString += "digraph d{\n";
		for(LatticeNode node : nodes)
			latticeString += node.getNodeNumber() + " [label=\"" + node.getExtent() + "\"]\n";
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
}
