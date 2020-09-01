package toolkits.utils.lts;

import java.util.List;

/**
 * @author Moqi
 * 定义按任务名字划分的使能活动类,包括:
 * 1)划分名字name;
 * 2)名字name对应使能活动集;
 */
public class EnableActivityParByName {
	
	private String name;
	private List<EnableActivity> enableActivities;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<EnableActivity> getEnableActivities() {
		return enableActivities;
	}
	public void setEnableActivities(List<EnableActivity> enableActivities) {
		this.enableActivities = enableActivities;
	}
	
	

}
