package toolkits.utils.lts;

/**
 * @author Moqi
 * 定义多个控制器并发组合展开迁移
 */
public class ExpandTransition {
	
	private ExpandState expandStateFrom;
	private String label;
	private ExpandState expandStateTo;
	
	public ExpandState getExpandStateFrom() {
		return expandStateFrom;
	}
	public void setExpandStateFrom(ExpandState expandStateFrom) {
		this.expandStateFrom = expandStateFrom;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public ExpandState getExpandStateTo() {
		return expandStateTo;
	}
	public void setExpandStateTo(ExpandState expandStateTo) {
		this.expandStateTo = expandStateTo;
	}

}
