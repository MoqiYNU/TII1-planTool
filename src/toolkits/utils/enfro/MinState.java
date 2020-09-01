package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moqi
 * 定义最小化过程中状态
 */
public class MinState {
	
	private String minStateIdf;
	private List<String> states;
	
	public MinState() {
		states = new ArrayList<String>();
	}
	
	public String getMinStateIdf() {
		return minStateIdf;
	}

	public void setMinStateIdf(String minStateIdf) {
		this.minStateIdf = minStateIdf;
	}

	public List<String> getStates() {
		return states;
	}

	public void setStates(List<String> states) {
		this.states = states;
	}
	
	public void addState(String state) {
		states.add(state);
	}
	
	public void addStates(List<String> tempStates) {
		states.addAll(tempStates);
	}
	

}
