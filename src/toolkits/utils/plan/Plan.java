package toolkits.utils.plan;

import java.util.ArrayList;
import java.util.List;

/**
 * @author QiMo
 * ����ƻ�,��Ӧһ�����Ƭ����ÿ��Ƭ����һ���������
 */
public class Plan {
	
	private List<ORDMatrix> matrices;
	
	public Plan() {
		matrices = new ArrayList<ORDMatrix>();
	}

	public void addORDMatrix(ORDMatrix order) {
		matrices.add(order);
	}

	public List<ORDMatrix> getMatrices() {
		return matrices;
	}
	public void setMatrices(List<ORDMatrix> matrices) {
		this.matrices = matrices;
	}
	

}
