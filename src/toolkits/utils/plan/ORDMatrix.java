package toolkits.utils.plan;

import java.util.List;

/**
 * @author QiMo
 * ����������,��Ӧһ���ж�
 */
public class ORDMatrix {
	
	private int[][] graph;  //�������
	private List<String> acts;//��������б�Ǩ��
	
	public int[][] getGraph() {
		return graph;
	}
	public void setGraph(int[][] graph) {
		this.graph = graph;
	}
	public List<String> getActs() {
		return acts;
	}
	public void setActs(List<String> acts) {
		this.acts = acts;
	}

	
}
