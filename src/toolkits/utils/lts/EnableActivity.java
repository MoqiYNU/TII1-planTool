package toolkits.utils.lts;

/**
 * @author Moqi
 * 定义使能活动,包括:
 * 1)使能活动所属的LTS的位置;
 * 2)使能活动activity;
 * 5)使能活动执行后到达的状态stateTo.
 */
public class EnableActivity {
	
	private int index;
	private String activity;
	private String stateTo;
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getStateTo() {
		return stateTo;
	}
	public void setStateTo(String stateTo) {
		this.stateTo = stateTo;
	}

}
