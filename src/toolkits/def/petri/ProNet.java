package toolkits.def.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.utils.block.InnerNet;

/**
 * @author Moqi
 * 定义过程网,用来建模业务过程及其组合
 */
public class ProNet {
	
	private Marking source;//一个开始标识及多个终止标识
	private List<Marking> sinks;
	private List<String> places;//库所集(包含消息库所)
	private List<String> msgPlaces;
	private List<String> inputMsgs;
	private List<String> outputMsgs;
	private List<String> trans;//变迁集
	private List<Flow> flows;//流关系
	private Map<String, String> tranLabelMap;//标号函数
	
	public ProNet() {
		sinks = new ArrayList<Marking>();
		places = new ArrayList<String>();
		msgPlaces = new ArrayList<String>();
		inputMsgs = new ArrayList<String>();
		outputMsgs = new ArrayList<String>();
		trans = new ArrayList<String>();
		flows = new ArrayList<Flow>();
		tranLabelMap = new HashMap<String, String>();
	}
	
	
	/****************************Add方法******************************/
	
	//添加一个sink库所
	public void addSink(Marking sink) {
		sinks.add(sink);
	}
	//添加一个库所(避免重复添加)
	public void addPlace(String place) {
		if (!places.contains(place)) {
			places.add(place);
		}
	}
	//添加多个库所(避免重复添加)
	public void addPlaces(List<String> tempPlaces) {
		for (String tempPlace : tempPlaces) {
			if (!places.contains(tempPlace)) {
				places.add(tempPlace);
			}
		}
	}
	//添加tran(避免重复添加)
	public void addTran(String tran) {
		if (!trans.contains(tran)) {
			trans.add(tran);
		}
	}
	//添加一条flow
	public void addFlow(Flow flow) {
		flows.add(flow);
	}
	//添加多条flow
	public void addFlows(List<Flow> tempFlows) {
		flows.addAll(tempFlows);
	}
	//添加消息库所(避免重复添加)
	public void addMsgPlace(String msgPlace) {
		if (!msgPlaces.contains(msgPlace)) {
			msgPlaces.add(msgPlace);
		}
	}
	//添加输入消息库所(避免重复添加)
	public void addInputMsgPlace(String msgPlace) {
		if (!inputMsgs.contains(msgPlace)) {
			inputMsgs.add(msgPlace);
		}
	}
	//添加输出消息库所(避免重复添加)
	public void addOutputMsgPlace(String msgPlace) {
		if (!outputMsgs.contains(msgPlace)) {
			outputMsgs.add(msgPlace);
		}
	}
	
	/*************************Get和Set方法****************************/
	
	public Marking getSource() {
		return source;
	}
	public void setSource(Marking source) {
		this.source = source;
	}	
	public List<Marking> getSinks() {
		return sinks;
	}
	public void setSinks(List<Marking> sinks) {
		this.sinks = sinks;
	}
	public List<String> getPlaces() {
		return places;
	}
	public void setPlaces(List<String> places) {
		this.places = places;
	}
	public List<String> getTrans() {
		return trans;
	}
	public void setTrans(List<String> trans) {
		this.trans = trans;
	}
	public List<Flow> getFlows() {
		return flows;
	}
	public void setFlows(List<Flow> flows) {
		this.flows = flows;
	}
	public List<String> getMsgPlaces() {
		return msgPlaces;
	}
	public void setMsgPlaces(List<String> msgPlaces) {
		this.msgPlaces = msgPlaces;
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
	public Map<String, String> getTranLabelMap() {
		return tranLabelMap;
	}
	public void setTranLabelMap(Map<String, String> tranLabelMap) {
		this.tranLabelMap = tranLabelMap;
	}
	
	/***********************返回过程网的内网(工作流网描述)**************************/
	
	public InnerNet getInnerNet() {
		
		InnerNet innerNet = new InnerNet();
        innerNet.setSource(getSource().getPlaces().get(0));
        innerNet.setSink(getSinks().get(0).getPlaces().get(0));
        innerNet.setPlaces((List<String>) CollectionUtils.subtract(getPlaces(), getMsgPlaces()));
        innerNet.setTrans(getTrans());
        List<Flow> interFlows = new ArrayList<Flow>();
        for (Flow flow : getFlows()) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (getMsgPlaces().contains(from) || getMsgPlaces().contains(to)) {
				continue;
			}
			interFlows.add(flow);
        }
        innerNet.setFlows(interFlows);
        innerNet.setTranLabelMap(getTranLabelMap());
        return innerNet;
        
	}
	
	//获取ProNet中交互活动集
	public List<String> getInterActs() {
		
		List<String> interActs = new ArrayList<String>();
		for (Flow flow : getFlows()) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (getMsgPlaces().contains(from)) {
				interActs.add(to);
			}else if (getMsgPlaces().contains(to)) {
				interActs.add(from);
			}
        }
		return interActs;
		
	}

}
