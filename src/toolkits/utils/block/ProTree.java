package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Moqi
 * 定义过程树,包括节点及其孩子节点集
 */
public class ProTree {
	
	private List<Node> nodes;
	
	public ProTree() {
		nodes = new ArrayList<Node>();
	}
	
	public void addNode(Node node) {
		nodes.add(node);
	}
	public void addNodes(List<Node> tempNodes) {
		nodes.addAll(tempNodes);
	}
	
	public void removeNode(Node node) {//移除树中节点node
		for (int i = 0; i < nodes.size(); i++) {
			Node tempNode = nodes.get(i);
			if (tempNode.getIdf().equals(node.getIdf())) {
				nodes.remove(i);
				break;
			}
		}
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

}
