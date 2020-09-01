package toolkits.utils.plan;

import java.util.ArrayList;
import java.util.List;

import toolkits.utils.block.ProTree;

/**
 * @author Moqi
 * 定义组合片段,用于实现基于执行计划迫使
 */
public class CompFrag {
	
	private List<ProTree> frags;
	private List<String> inputMsgs;
	private List<String> outputMsgs;
	
	public CompFrag() {
		frags = new ArrayList<ProTree>();
	}
	
	public void addFrag(ProTree frag) {
		frags.add(frag);
	}
	public List<ProTree> getFrags() {
		return frags;
	}
	public void setFrags(List<ProTree> frags) {
		this.frags = frags;
	}
	public List<String> getInputMsgs() {
		return inputMsgs;
	}
	public void setInputMsgs(List<String> inputMsgs) {
		this.inputMsgs = inputMsgs;
	}
	public List<String> getOutputMsgs() {
		return outputMsgs;
	}
	public void setOutputMsgs(List<String> outputMsgs) {
		this.outputMsgs = outputMsgs;
	}
	

}
