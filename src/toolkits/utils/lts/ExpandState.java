package toolkits.utils.lts;

import java.util.List;

/**
 * @author Moqi
 * 定义展开状态,由每个控制器状态组合形成
 */
public class ExpandState {
	
	private List<String> states;
	private String identify;
	//用于计算LTS
	private String state;
	
	public List<String> getStates() {
		return states;
	}
	public void setStates(List<String> states) {
		this.states = states;
	}
	public String getIdentify() {
		return identify;
	}
	public void setIdentify(String identify) {
		this.identify = identify;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

}
