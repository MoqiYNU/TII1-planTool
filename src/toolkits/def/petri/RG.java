package toolkits.def.petri;

import java.util.List;
import java.util.Map;
import toolkits.def.petri.Marking;

/**
 * @author Moqi
 * ����������Ŀɴ�ͼ
 */
public class RG {
	
	private Marking start;
	private List<Marking> ends;
	private List<Marking> vertexs;
	private List<Edge> edges;
	private Map<String, String> tranLabelMap;//��ź���
	
	public Marking getStart() {
		return start;
	}
	public void setStart(Marking start) {
		this.start = start;
	}
	public List<Marking> getEnds() {
		return ends;
	}
	public void setEnds(List<Marking> ends) {
		this.ends = ends;
	}
	public List<Marking> getVertexs() {
		return vertexs;
	}
	public void setVertexs(List<Marking> vertexs) {
		this.vertexs = vertexs;
	}
	public List<Edge> getEdges() {
		return edges;
	}
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	public Map<String, String> getTranLabelMap() {
		return tranLabelMap;
	}
	public void setTranLabelMap(Map<String, String> tranLabelMap) {
		this.tranLabelMap = tranLabelMap;
	}

}
