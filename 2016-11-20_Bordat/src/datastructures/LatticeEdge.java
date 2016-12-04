package datastructures;

public class LatticeEdge {
	private LatticeNode lowerNode;
	private LatticeNode upperNode;
	
	public LatticeEdge(LatticeNode _lowerNode, LatticeNode _upperNode) {
		this.lowerNode = _lowerNode;
		this.upperNode = _upperNode;
	}
	
	public int getLowerNodeNumber() {
		return lowerNode.getNodeNumber();
	}
	
	public int getUpperNodeNumber() {
		return upperNode.getNodeNumber();
	}
}
