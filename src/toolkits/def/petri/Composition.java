package toolkits.def.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toolkits.utils.petri.PetriUtils;
import toolkits.utils.plan.MsgPlaceBag;

/**
 * @author Moqi
 * 定义若干个过程网的异步组合
 */
public class Composition {
	
	private List<ProNet> proNets;//待合并过程网
	
	public Composition() {
		proNets = new ArrayList<ProNet>();
	}

	public List<ProNet> getProNets() {
		return proNets;
	}
	public void setProNets(List<ProNet> proNets) {
		this.proNets = proNets;
	}
	
	
	/**************************组合多个开放网(只考虑异步)*************************/
	
	public ProNet compose() {
		int size = proNets.size();
		//System.out.println("PN size: " + size);
		if (size == 1) {//1.只有一个过程网
			return proNets.get(0);
		}else {//2.至少有两个过程网
			ProNet proNet = composeTwoProNets(proNets.get(0), proNets.get(1));
			for (int i = 2; i < size; i++) {
				//System.out.println("Compose i th PN: " + i);
				proNet = composeTwoProNets(proNet, proNets.get(i));	
			}
			return proNet;
		}
	}
		
	// 异步组合过程网proNet1和proNet2
	public ProNet composeTwoProNets(ProNet proNet1, ProNet proNet2) {
		
		//定义组合网中相关属性
		List<String> sourPlaces = new ArrayList<String>();
		List<Marking> sinks = new ArrayList<Marking>();
		List<String> places = new ArrayList<String>();
		List<String> msgPlaces = new ArrayList<String>();
		List<String> trans = new ArrayList<String>();
		List<Flow> flows = new ArrayList<Flow>();
		Map<String, String> tranLabelMap = new HashMap<String, String>();
		
		//1.求proNet1和proNet2组合形成初始标识.......................
		sourPlaces.addAll(proNet1.getSource().getPlaces());
		sourPlaces.addAll(proNet2.getSource().getPlaces());
		Marking source = new Marking();
		source.addPlaces(sourPlaces);
		
		//2.合并Place(避免重复消息库所)...........................
		List<String> places1 = proNet1.getPlaces();
		for (String place : places1) {
			if (!places.contains(place)) {
				places.add(place);
			}
		}
		List<String> places2 = proNet2.getPlaces();
		for (String place : places2) {
			if (!places.contains(place)) {
				places.add(place);
			}
		}
		
		//3.变迁集.............................................
		//3.1获得proNet1和proNet2中变迁(以Id标识)
		List<String> trans1 = proNet1.getTrans();
		List<String> trans2 = proNet2.getTrans();
		//3.2获取变迁及其标号
		for (String tran1 : trans1) {
			trans.add(tran1);
			tranLabelMap.put(tran1, getLabel(proNet1.getTranLabelMap(), tran1));
		}
		for (String tran2 : trans2) {
			trans.add(tran2);
			tranLabelMap.put(tran2, getLabel(proNet2.getTranLabelMap(), tran2));
		}
		
        //4.流变迁.............................................
  		List<Flow> flows1 = proNet1.getFlows();
  		List<Flow> flows2 = proNet2.getFlows();
  		flows.addAll(flows1);
  		flows.addAll(flows2);
  		
	    //5.终止标识.............................................
	    List<Marking> sinks1 = proNet1.getSinks();
	    List<Marking> sinks2 = proNet2.getSinks();
	    for (Marking marking1 : sinks1) {
	        List<String> finalPlaces1 = marking1.getPlaces();
			for (Marking marking2 : sinks2) {
				List<String> finalPlaces2 = marking2.getPlaces();
				Marking sink = new Marking();
				List<String> finalPlaces = new ArrayList<String>();
				finalPlaces.addAll(finalPlaces1);
				finalPlaces.addAll(finalPlaces2);
				sink.setPlaces(finalPlaces);
				sinks.add(sink);
			}
		}
        
        //System.out.println("Comp Final makrings: " + sink.getPlaces());
        
        //6.合并消息库所,避免重复.............................
  		List<String> msgPlaces1 = proNet1.getMsgPlaces();
  		for (String place : msgPlaces1) {
  			if (!msgPlaces.contains(place)) {
  				msgPlaces.add(place);
  			}
  		}
  		List<String> msgPlaces2 = proNet2.getMsgPlaces();
  		for (String place : msgPlaces2) {
  			if (!msgPlaces.contains(place)) {
  				msgPlaces.add(place);
  			}
  		}
        
        ProNet petri = new ProNet();
		petri.setSource(source);
		petri.setPlaces(places);
		petri.setTrans(trans);
		petri.setFlows(flows);
		petri.setSinks(sinks);
		petri.setMsgPlaces(msgPlaces);
		petri.setTranLabelMap(tranLabelMap);
		
		return petri;
		
	}
	
	//获得tran对应的label(默认为id)
	public String getLabel(Map<String, String> tranLabelMap, String tran) {
		String label = tranLabelMap.get(tran);
		return label;
	}
	
	//获取组合网中消息库所包
	public List<MsgPlaceBag> getMsgPlaceBags(List<String> msgPlaces, List<Flow> flows) {
		List<MsgPlaceBag> msgPlaceBags = new ArrayList<>();
		for (String msgPlace : msgPlaces) {
			List<String> preSet = PetriUtils.getPreSet(msgPlace, flows);
			List<String> postSet = PetriUtils.getPostSet(msgPlace, flows);
			MsgPlaceBag bag = new MsgPlaceBag();
			bag.setPreSet(preSet);
			bag.setPostSet(postSet);
			msgPlaceBags.add(bag);
			//System.out.println("msgPlace: " + msgPlace + ", PreSet: " + preSet + ", PostSet: " + postSet);
		}
		return msgPlaceBags;
	}

	
}
