package toolkits.utils.lts;

import toolkits.def.petri.Marking;

/**
 * @author Moqi
 * 定义源过程(ProNet)和控制器组合状态
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
