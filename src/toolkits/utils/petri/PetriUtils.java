package toolkits.utils.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.petri.Flow;
import toolkits.def.petri.Marking;
import toolkits.def.petri.Edge;
import toolkits.def.petri.ProNet;
import toolkits.def.petri.RG;

/**
 * @author Moqi
 * �����������Utils
 */
public class PetriUtils {
	
	
	//������ȹ̼�
	private Map<Marking, List<String>> noStubSetMap;
	
	public PetriUtils() {
		noStubSetMap = new LinkedHashMap<Marking, List<String>>();
	}
	
	//��¼RRG��ÿ����ʶδ�������ȹ̼�
	public Map<Marking, List<String>> getNoStubSetMap() {
		return noStubSetMap;
	}

	
	/******************************�����ȹ̼�(Note:δ���Ǻ���)********************************/
	
	public List<String> getStubSet(ProNet proNet, Marking marking) {
		
		List<String> S = new ArrayList<String>();//�����ȹ̼�
		Queue<String> U = new LinkedList<>();//δ����Ǩ�Ƽ�
		
		//����ʹ�ܻ����
		List<String> enableTrans = getEnableTrans(proNet, marking.getPlaces());
		if (enableTrans.size() == 0) {//û��ʹ��Ǩ��,ֱ�ӷ��ؿ��ȹ̼�S
			return S;
		}else {//��ʹ��Ǩ��,���ѡ��һ��ʹ��Ǩ��
			
			String firstAct = enableTrans.get(0);
			S.add(firstAct);
			U.add(firstAct);
			
		    while(U.size() > 0){//��������
		    	List<String> N;
		    	//����һ����Ǩ,�Դ˽��м���
			    String actFrom = U.poll();
			    if (enableTrans.contains(actFrom)) {//��marking��ʹ��,���ȡ��ͻǨ�Ƽ�
			    	N = getDisablingTrans(proNet, actFrom);
			    	//System.out.println("Disabling: " + N);
				}else {//��marking����ʹ��,���ȡ������ʹ��Ǩ�Ƽ�
					N = getEnablingTrans(proNet, actFrom, marking);
					//System.out.println("Enabling: " + N);
				}
			    
			    List<String> subSet = (List<String>) CollectionUtils.subtract(N, S);//�����ظ����
			    for (String subElem : subSet) {
					if (!U.contains(subElem)) {
						U.add(subElem);
					}
				}
			    //System.out.println("U: " + U);
                for (String elem : N) {//����ȹ̼���S
					if (!S.contains(elem)) {
						S.add(elem);
					}
				}
		    }
		    return S;
		}
	}
	
	//��ȡ���±�Ǩʹ�ܵı�Ǩ��(��Id��ʶ)
	public List<String> getEnablingTrans(ProNet proNet, String tran, Marking marking) {
		
		List<String> trans = new ArrayList<String>();
		
		List<String> preSet = getPreSet(tran, proNet.getFlows());
		for (String place : preSet) {
			if (marking.getPlaces().contains(place)) {//�����Ѿ������пϵĿ���
				continue;
			}
			List<String> tempTrans = getPreSet(place, proNet.getFlows());
			//System.out.println("tempTranIds: " + tempTranIds);
			for (String tempTran : tempTrans) {
				if (!trans.contains(tempTran)) {
					trans.add(tempTran);
				}
			}
		}
		return trans;
	}
	
	//��ȡ��Ǩ�ĳ�ͻ��Ǩ��(��tranId��ʶ)
	public List<String> getDisablingTrans(ProNet proNet, String tran) {
		
		List<String> trans = new ArrayList<String>();
		
		List<String> preSet = getPreSet(tran, proNet.getFlows());
		for (String tempTran : proNet.getTrans()) {
			if (tran.equals(tempTran)) {//�������Լ�
				continue;
			}
			List<String> tempPreSet = getPreSet(tempTran, proNet.getFlows());
			if (CollectionUtils.intersection(preSet, tempPreSet).size() > 0) {//ǰ���ཻ
				trans.add(tempTran);
			}
		}
		return trans;
	}
	

	/******************************����Petri���ɴ�ͼ********************************/
	
	//�����ȹ̼��ӿ������в�����Լ���ɴ�ͼ,��ÿ��Ǩ���ȹ̼�(Note:δ���Ǻ�������)
	public RG genRGWithStubSet(ProNet proNet) {
		
		noStubSetMap.clear();
		
		//RRG�б�Ǩ�����ӳ��
		Map<String, String> tranLabelMap = new HashMap<String, String>();
		
		//��ʼ��ʶ
		Marking initMarking = proNet.getSource();
		
		//��ֹ��ʶ
		List<Marking> finalMarkings = proNet.getSinks();
		
		List<Edge> edges = new ArrayList<Edge>();
		
		//�������ʵĶ���visitingQueue���Ѿ����ʹ�����visitedQueue
		Queue<Marking> visitingQueue = new LinkedList<>(); 
		List<Marking> visitedQueue = new ArrayList<>();
		//����ʼ��ʶ��Ӳ���Ϊ�Ѿ�����
		visitingQueue.offer(initMarking);
		visitedQueue.add(initMarking);
		
		//��������
	    while(visitingQueue.size() > 0){

		    //����һ����ʶ,�Դ˽���Ǩ��
		    Marking markingFrom = visitingQueue.poll();
		    List<String> placesFrom = markingFrom.getPlaces();
		    
		    //System.out.println("markingFrom: " + markingFrom.getPlaces());
		    
		    //markingFrom������ʹ�ܻ��
			List<String> allEnableTrans = getEnableTrans(proNet, placesFrom);
		    //����ȹ̼�(δ����������������)
			List<String> S = getStubSet(proNet, markingFrom);
			
			//Note:ֻǨ���ȹ̼���ʹ�ܻ
			List<String> enableTrans = (List<String>) CollectionUtils.intersection(allEnableTrans, S);
			//System.out.println("Marking" + markingFrom.getPlaces() + " Stub set: " + S);
			//System.out.println("Stub set: " + S + ", all enable acts: " + allEnableTrans);
			
			List<String> noStubSet = (List<String>) CollectionUtils.subtract(allEnableTrans, S); 
			noStubSetMap.put(markingFrom, noStubSet);
			System.out.println("Marking" + markingFrom.getPlaces() + " no Stub set: " + noStubSet);

			//�����ȹ̼���ʹ�ܱ�Ǩ�������
            for (String tran : enableTrans) {
				
				List<String> placesTo = getPlacesTo(proNet, placesFrom, tran);
				Marking markingTo = new Marking();
				markingTo.setPlaces(placesTo);
				
				Edge edge = new Edge();
				edge.setFrom(markingFrom);
				
				edge.setTran(tran);
				edge.setTo(markingTo);
				edges.add(edge);
				
				tranLabelMap.put(tran, getLabel(proNet.getTranLabelMap(), tran));
				
				if (!MarkingUtils.markingIsExist(visitedQueue, markingTo)) {
					visitingQueue.offer(markingTo);
					visitedQueue.add(markingTo);
				}
				
			}
	    }
	    
	    RG rrg = new RG();
	    rrg.setStart(initMarking);
	    rrg.setEnds(finalMarkings);
	    rrg.setVertexs(visitedQueue);
	    rrg.setEdges(edges);
	    rrg.setTranLabelMap(tranLabelMap);
	    return rrg;
		
	}
	
	
	// 2.���ô�ͳ�����ӹ������в�����ɴ�ͼ
	public static RG genRG(ProNet openNet) {
		
		Map<String, String> tranLabelMap = new HashMap<String, String>();//�����ź���
		
		//��ʼ��ʶ
		Marking initMarking = openNet.getSource();
		//��ֹ��ʶ
		List<Marking> finalMarkings = openNet.getSinks();
		List<Edge> edges = new ArrayList<Edge>();
		
		//�������ʵĶ���visitingQueue���Ѿ����ʹ�����visitedQueue
		Queue<Marking> visitingQueue = new LinkedList<>(); 
		List<Marking> visitedQueue = new ArrayList<>();
		//����ʼ��ʶ��Ӳ���Ϊ�Ѿ�����
		visitingQueue.offer(initMarking);
		visitedQueue.add(initMarking);
		
		//��������
	    while(visitingQueue.size() > 0){

		    //����һ����ʶ,�Դ˽���Ǩ��
		    Marking markingFrom = visitingQueue.poll();
		    List<String> placesFrom = markingFrom.getPlaces();
		    
		    //����ʹ�ܻ����
		    List<String> enableTrans = getEnableTrans(openNet, placesFrom);
		    
			for (String tran : enableTrans) {
				List<String> placesTo = getPlacesTo(openNet, placesFrom, tran);
				Marking markingTo = new Marking();
				markingTo.setPlaces(placesTo);
				Edge edge = new Edge();
				edge.setFrom(markingFrom);
				edge.setTran(tran);
				edge.setTo(markingTo);
				tranLabelMap.put(tran, getLabel(openNet.getTranLabelMap(), tran));
				edges.add(edge);
				if (!MarkingUtils.markingIsExist(visitedQueue, markingTo)) {
					visitingQueue.offer(markingTo);
					visitedQueue.add(markingTo);
				}
			}
	    }
	    
	    RG rg = new RG();
	    rg.setStart(initMarking);
	    rg.setEnds(finalMarkings);
	    rg.setVertexs(visitedQueue);
	    rg.setEdges(edges);
	    rg.setTranLabelMap(tranLabelMap);
	    return rg;
		
	}
	
	
	//���tran��Ӧ��label
	public static String getLabel(Map<String, String> tranLabelMap, String tran) {
		String label = tranLabelMap.get(tran);
		//System.out.println("tranId: " + tran + ", " + label);
		if (label == null) {
			return "sync";
		}else {
			return label;
		}
	}
	
	//ȷ��tran�ڵڼ����������г���
	public static int getProNetIndex(List<ProNet> proNets, String tran) {
		int index = 0;
		for (ProNet proNet : proNets) {
			if (proNet.getTrans().contains(tran)) {
				return index;
			}
			index ++;
		}
		return -1;
		
	}
	
	//��ȡtran��λ�ñ��
	public static int getTranIndex(String tran, List<String> trans) {
		int index = 0;
		for (String tempTran : trans) {
			if (tempTran.equals(tran)) {
				return index;
			}
			index ++;
		}
		return -1;
		
	}
	
	/**********************����P/T���лʹ��,�����������/��Ǩǰ��*********************/
	
	//���tran�ĺ�̿�������(��Petri����P/T��Ǩ����ȷ��)
	public static List<String> getPlacesTo(ProNet proNet, List<String> places, String tran) {
		
		//���tran��ǰ���ͺ�
		List<String> preSet = getPreSet(tran, proNet.getFlows());
		List<String> postSet = getPostSet(tran, proNet.getFlows());
		
		//��õ�ǰ��ʶ�е�������
	    List<String> placesFrom = places;
	    List<String> placesTo = new ArrayList<String>();
	    
	    //1.1 preSet-postSet
	    List<String> preSetNotInPostSet = new ArrayList<String>();
	    //1.2 postSet-preSet
	    List<String> postSetNotInPreSet = new ArrayList<String>();
	    //1.3 else
	    List<String> elseSet = new ArrayList<String>();
	    
        for (String placeFrom : placesFrom) {
			if (preSet.contains(placeFrom) && !postSet.contains(placeFrom)) {
				preSetNotInPostSet.add(placeFrom);
			}else if (postSet.contains(placeFrom) && !preSet.contains(placeFrom)) {
				postSetNotInPreSet.add(placeFrom);
			}else {
				elseSet.add(placeFrom);
			}
		}
	    
        // 1.ǰ����1,ʹ��CollectionUtils�еĲ�����
        placesTo.addAll((List<String>) CollectionUtils.subtract(preSetNotInPostSet, preSet));
        // 2.������ǰ�����ϼ�1
        placesTo.addAll(postSetNotInPreSet);
        for (String post : postSet) {
			if (!preSet.contains(post)) {
				placesTo.add(post);
			}
		}
        // 3.ʣ�²���
        placesTo.addAll(elseSet);
        
		return placesTo;
	}
	
	//���places������ʹ�ܱ�Ǩ(P/T��)
	public static List<String> getEnableTrans(ProNet openNet, List<String> places) {
		List<String> enableTrans = new ArrayList<String>();
		//����ÿ����Ƿ�ʹ��
		List<String> trans = openNet.getTrans();
		for (String tran : trans) {
			if (isEnable(openNet, places, tran)) {
				enableTrans.add(tran);
			}
		}
		return enableTrans;
	}
		
	//�ж�tran�Ƿ��ܹ����(P/T��)
	public static boolean isEnable(ProNet openNet, List<String> places, String tran) {
		//���tran��ǰ��
		List<String> preSet = getPreSet(tran, openNet.getFlows());
		//���tran��ǰ������places,����Ե��
		if (CollectionUtils.isSubCollection(preSet, places)) {
			return true;
		}
		return false;
	}
	
	//��ȡԪ��elem(�������Ǩ)��ǰ��
	public static List<String> getPreSet(String elem, List<Flow> flows) {
		List<String> preSet = new ArrayList<String>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (elem.equals(to)) {
				if (!preSet.contains(from)) {
					preSet.add(from);
				}
			}
		}
		return preSet;
	}
	
	//��ȡԪ��elem(�������Ǩ)�ĺ�
	public static List<String> getPostSet(String elem, List<Flow> flows) {
		List<String> postSet = new ArrayList<String>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (elem.equals(from)) {
				if (!postSet.contains(to)) {
					postSet.add(to);
				}
			}
		}
		return postSet;
	}

}
