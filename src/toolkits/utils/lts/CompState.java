package toolkits.utils.lts;

import toolkits.def.petri.Marking;

/**
 * @author Moqi
 * ����Դ����(ProNet)�Ϳ��������״̬
 */
public class CompState {
	
	private ExpandState expandState;
	private Marking marking;
	
	public ExpandState getExpandState() {
		return expandState;
	}
	public void setExpandState(ExpandState expandState) {
		this.expandState = expandState;
	}
	public Marking getMarking() {
		return marking;
	}
	public void setMarking(Marking marking) {
		this.marking = marking;
	}

}
