package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import toolkits.def.petri.Flow;

/**
 * @author Moqi 
 * 定义过程网中内网,即将消息库所移除后形成的网
 */
public class InnerNet {
	
	private String source;
	private String sink;
	private List<String> places;
	private List<String> trans;
	private List<Flow> flows;
	private Map<String, String> tranLabelMap;
	
	public InnerNet() {
		places = new ArrayList<String>();
		trans = new ArrayList<String>();
		flows = new ArrayList<Flow>();
	}
	
	public void addPlace(String place) {
		if (!places.contains(place)) {
			places.add(place);
		}
	}
	
	public void addTran(String tran) {
		if (!trans.contains(tran)) {
			trans.add(tran);
		}
	}
	
	public void addFlow(Flow flow) {
		flows.add(flow);
	}
	public void addFlows(List<Flow> tempFlows) {
		flows.addAll(tempFlows);
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSink() {
		return sink;
	}
	public void setSink(String sink) {
		this.sink = sink;
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
	public Map<String, String> getTranLabelMap() {
		return tranLabelMap;
	}
	public void setTranLabelMap(Map<String, String> tranLabelMap) {
		this.tranLabelMap = tranLabelMap;
	}
	
	
}
