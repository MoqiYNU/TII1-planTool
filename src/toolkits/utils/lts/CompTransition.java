package toolkits.utils.lts;

/**
 * @author Moqi
 * 定义源过程(ProNet)和控制器组合迁移
 */
public class CompTransition {
	
	private CompState compStateFrom;
	private String tran;
	private CompState compStateTo;
	
	public CompState getCompStateFrom() {
		return compStateFrom;
	}
	public void setCompStateFrom(CompState compStateFrom) {
		this.compStateFrom = compStateFrom;
	}
	public String getTran() {
		return tran;
	}
	public void setTran(String tran) {
		this.tran = tran;
	}
	public CompState getCompStateTo() {
		return compStateTo;
	}
	public void setCompStateTo(CompState compStateTo) {
		this.compStateTo = compStateTo;
	}

}
