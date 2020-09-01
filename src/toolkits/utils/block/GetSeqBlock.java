package toolkits.utils.block;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import toolkits.def.petri.Flow;

/**
 * @author Moqi 
 * �������������˳���
 */
public class GetSeqBlock {
	
	private List<Block> maxSEQBlocks;
	
	public GetSeqBlock() {
		maxSEQBlocks = new ArrayList<Block>();
	}

	public List<Block> getMaxSEQBlocks() {
		return maxSEQBlocks;
	}
	
	public void compute(InnerNet net) {
		
		maxSEQBlocks.clear();//����֮ǰ����ջ���
		
		//��������˳���
		List<SEQPair> seqPairs = new ArrayList<>();
		List<String> places = net.getPlaces();
		for (String place : places) {
			List<String> preSet = getPreSet(place, net.getFlows());
			List<String> postSet = getPostSet(place, net.getFlows());
			if (preSet.size() == 1 && postSet.size() == 1 //placeǰ�漰�����ֻ��1����Ǩa��b,��(a,place,b)
					&& (getPreSet(preSet.get(0), net.getFlows())).size() == 1//��Ǩa��ǰ��Ϊ1
					&& (getPostSet(preSet.get(0), net.getFlows())).size() == 1//��Ǩa�ĺ�Ϊ1
					&& (getPreSet(postSet.get(0), net.getFlows())).size() == 1//��Ǩb��ǰ��Ϊ1
					&& (getPostSet(postSet.get(0), net.getFlows())).size() == 1) {//��Ǩb�ĺ�Ϊ1
				//System.out.println("entry: " + place);
				SEQPair seqPair = new SEQPair();
				seqPair.setPreAct(preSet.get(0));
				seqPair.setSuccAct(postSet.get(0));
				seqPairs.add(seqPair);
			}
		}
		
		//�������˳���,����(a,b),(c,d),(f,g),�����˳���Ϊ(a,b),(f,g)
		List<SEQPair> maxSeqPairs = getMaxSeqPairs(seqPairs);
		for (SEQPair maxSeqPair : maxSeqPairs) {
			String preAct = maxSeqPair.getPreAct();
			String succAct = maxSeqPair.getSuccAct();
			List<String> succActs = getSeqActs(succAct, seqPairs);
			List<String> seqActs = new ArrayList<String>();
			seqActs.add(preAct);
			seqActs.add(succAct);
			seqActs.addAll(succActs);
			Block part = new Block();
			part.setEntry(seqActs.get(0));
			part.setExit(seqActs.get(seqActs.size()-1));
			part.setSeqActs(seqActs);
			part.setType("SEQ");
			maxSEQBlocks.add(part);
		}
	}
	
	//��ȡelem�ĺ�
	public List<String> getPostSet(String elem, List<Flow> flows) {
		List<String> postSet = new ArrayList<String>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (elem.equals(from)) {
				postSet.add(to);
			}
		}
		return postSet;
	}
	
	//��ȡelem��ǰ��
	public List<String> getPreSet(String elem, List<Flow> flows) {
		List<String> preSet = new ArrayList<String>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (elem.equals(to)) {
				preSet.add(from);
			}
		}
		return preSet;
	}
	
	//��act����������Ǩϵ��,����act=a,��(a,b),(b,c),(c,d),��<b,c,d>
	public List<String> getSeqActs(String act, List<SEQPair> seqPairs) {
		
		List<String> succActs = new ArrayList<String>();
		//�������ʵĶ���visitingQueue
		Queue<String> visitingQueue = new LinkedList<>();
		visitingQueue.offer(act);
		//succActs.add(act);
		//��������
	    while(visitingQueue.size() > 0){
	    	//����һ����ʶ,�Դ˽���Ǩ��
		    String actFrom = visitingQueue.poll();
	    	for (SEQPair seqPair : seqPairs) {
	    		String preAct = seqPair.getPreAct();
				String succAct = seqPair.getSuccAct();
				if (actFrom.equals(preAct)) {
					succActs.add(succAct);
					visitingQueue.offer(succAct);
					break;
				}
			}
	    }
		return succActs;
	}
	
	//�����˳��Լ�
	public List<SEQPair> getMaxSeqPairs(List<SEQPair> seqPairs) {
		List<SEQPair> maxSeqPairs = new ArrayList<SEQPair>();
		for (SEQPair seqPair : seqPairs) {
			if (isMaxSeqPair(seqPair, seqPairs)) {
				maxSeqPairs.add(seqPair);
			}
		}
		return maxSeqPairs;
	}

	//�ж��Ƿ�Ϊ���˳���,����˳���ǰ�治���������
	private boolean isMaxSeqPair(SEQPair seqPair, List<SEQPair> seqPairs) {
		String preAct = seqPair.getPreAct();
		for (SEQPair tempSeqPair : seqPairs) {
			String tempPostAct = tempSeqPair.getSuccAct();
			if (preAct.equals(tempPostAct)) {
				return false;
			}else {
				continue;
			}
		}
		return true;
	}

}
