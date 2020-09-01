package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;
import toolkits.def.petri.Marking;
import toolkits.def.petri.ProNet;
import toolkits.utils.petri.MarkingUtils;

/**
 * @author Moqi
 * ������Utils
 */
public class CheckUtils {
	
	private ClosureUtils closureUtils;
	
	public CheckUtils() {
		closureUtils = new ClosureUtils();
		
	}
	
	//�ж�Bi�����Ƿ���Ч
	public boolean isValid_BiApproach(ProNet orgPro, LTS lts, LTS rlts) {
		
		// 1.Bi����������ʹ�漰ͬ����Դ����
		List<String> trans = orgPro.getTrans();
		for (String tran : trans) {
			//��tran��'_'�з�(��Ϊͬ���ϲ���Ǩ,������:T1_T2_T3)
			String[] syncTrans = tran.split("\\_");
			//1)tran����ͬ���ϲ�Ǩ��
			if (syncTrans.length == 1) {
				continue;
			}else {//2)tran��ͬ���ϲ�Ǩ��,��Bi�����˳�
				System.out.println("sync tran: " + tran);
				System.out.println("Sync occurs...");
				return false;
			}
		}
		
		List<String> interStates = new ArrayList<>();
		List<String> deadlocks = new ArrayList<>();
		List<String> invalidStates = new ArrayList<>();
		List<String> validStates = new ArrayList<>();
		List<String> states = lts.getStates();
		List<String> ends = lts.getEnds();
		//����Դ����RG��ÿ��״̬������Ǩ�Ʊհ�
		for (String state : states) {
			List<String> tranClosure = closureUtils.getRestTranClosure(state, lts);
			//��tranClosure��δ����ֹ״̬��state��Ϊ��ֹ״̬,��Ϊ��Ч״̬
			if (CollectionUtils.intersection(tranClosure, ends).size() == 0
					&& !ends.contains(state)) {
				invalidStates.add(state);
				//��tranClosureΪ��,��Ϊ����
				if (tranClosure.size() == 0) {
					deadlocks.add(state);
				}else {
					interStates.add(state);
				}
			}else {
				validStates.add(state);
			}
		}
		
		// 2.Bi����������ʹ��������Դ����
        for (String interState : interStates) {
        	List<String> tranClosure = closureUtils.getRestTranClosure(interState, lts);
			//���м�״̬(���쳣״̬)���ܵ�������״̬,��Ϊ����
        	if (CollectionUtils.intersection(tranClosure, deadlocks).size() == 0) {
        		System.out.println("livelock: " + interState);
				System.out.println("a livelock occurs...");
				return false;
			}
		}
		
		List<String> rstates = rlts.getStates();
		List<String> rends = rlts.getEnds();
		List<String> rinvalidStates = new ArrayList<>();
		List<String> rvalidStates = new ArrayList<>();
		for (String rState : rstates) {
			List<String> tranClosure = closureUtils.getRestTranClosure(rState, rlts);
			//��tranClosure��δ����ֹ״̬��state��Ϊ��ֹ״̬,��Ϊ��Ч״̬
			if (CollectionUtils.intersection(tranClosure, rends).size() == 0
					&& !rends.contains(rState)) {
				rinvalidStates.add(rState);
			}else {
				rvalidStates.add(rState);
			}
		}
		
		//����RG���ܿر�ʶ��
		List<Marking> ctStates = new ArrayList<>();
        for (String validState : validStates) {
			List<String> tranStates = closureUtils.getTranStates(validState, lts);
			if (CollectionUtils.intersection(tranStates, invalidStates).size() != 0) {
				System.out.println("ctState in RG: " + validState);
				ctStates.add(lts.getStateMarkingMap().get(validState));
			}
		}
        
        //����RRG���ܿر�ʶ��
        List<Marking> rctStates = new ArrayList<>();
        for (String rvalidState : rvalidStates) {
			List<String> tranStates = closureUtils.getTranStates(rvalidState, rlts);
			if (CollectionUtils.intersection(tranStates, rinvalidStates).size() != 0) {
				System.out.println("ctState in RRG: " + rvalidState);
				rctStates.add(rlts.getStateMarkingMap().get(rvalidState));
			}
		}
		
		// 3.��RG�������ܿر�ʶ��RRG����,�򷵻�true
		if (MarkingUtils.markingsIsEqual(ctStates, rctStates)) {
			return true;
		}else {
			System.out.println("some controlled markings are missing...");
			return false;
		}
		
	}
	
	//����Դ����LTS�������ȷ��
	public String checkCorrect(LTS orgProLTS) {
		
		List<String> validStates = new ArrayList<String>();
		List<String> states = orgProLTS.getStates();
		List<String> ends = orgProLTS.getEnds();
		//����ÿ��״̬��Ǩ�Ʊհ�
		for (String state : states) {
			List<String> tranClosure = closureUtils.getTranClosure(state, orgProLTS);
			//state��Ǩ�Ʊհ��а�����ֹ״̬
			if (CollectionUtils.intersection(ends, tranClosure).size() != 0) {
				validStates.add(state);
			}
		}
		//��������״̬�ж���ȷ��
		if (validStates.size() == 0) {
			return "fully incorrect";
		}else {
			if (states.size() == validStates.size()) {
				return "correct";
			}else {
				return "partially correct";
			}
		}
		
	}
	
	//��������ʹ�����Ƿ�һ��
	public boolean checkConsist(LTS core, LTS enfProLTS) {
		
		MinLTS minLTS = new MinLTS();
		
		//1.����ʹ�����������Э��Ǩ��ȫ���Ƴ������ɵ���ʹ����
		LTS genEnfPro = minLTS.rovCoodTrans(enfProLTS);
		
		//2.����һ���Լ��
		String initStateInCore = core.getStart();
		String initStateInEnf = genEnfPro.getStart();
		ConsistState initConState = new ConsistState();
		initConState.setState1(initStateInCore);
		initConState.setState2(initStateInEnf);
		//�������ʵĶ���visitingQueue���Ѿ����ʹ�����visitedQueue
		Queue<ConsistState> visitingQueue = new LinkedList<>(); 
		List<ConsistState> visitedQueue = new ArrayList<>();
		//����ʼ��ʶ��Ӳ���Ϊ�Ѿ�����
		visitingQueue.offer(initConState);
		visitedQueue.add(initConState);
		
		//��������
	    while(visitingQueue.size() > 0){
	    	
	    	//����һ����ʶ,�Դ˽���Ǩ��
	    	ConsistState conStateFrom = visitingQueue.poll();
	    	String stateFrom1 = conStateFrom.getState1();
	    	String stateFrom2 = conStateFrom.getState2();
	    	
	    	//System.err.println("Relation: " + "(" + stateFrom1 + ", " + stateFrom2 + ")");
	    	
	    	//1.�ж�state1��state2�Ƿ�Ϊ��ֹ״̬�����Ϊ��ֹ״̬
	    	if (core.getEnds().contains(stateFrom1) && !genEnfPro.getEnds().contains(stateFrom2) || 
	    			!core.getEnds().contains(stateFrom1) && genEnfPro.getEnds().contains(stateFrom2)) {
	    		return false;
			}else {//2.state1��state2��Ϊ��ֹ״̬�����Ϊ��ֹ״̬
				List<LTSTran> trans1 = getTrans(stateFrom1, core);
		    	List<LTSTran> trans2 = getTrans(stateFrom2, genEnfPro);
		    	
		    	List<String> labels1 = getLabels(trans1);
		    	List<String> labels2 = getLabels(trans2);
		    	
		    	//�ж���state1��state2������Ǩ���Ƿ����һһӳ���ϵ
		    	//Note:����state1����,�������Ǩ�Ʊ��(Petri���б�Ǩ)�ض��ǲ��ظ���
		    	if (!CollectionUtils.isEqualCollection(labels1, labels2)) {
					return false;
				}else {
					//����һһӳ���ϵ
					for (LTSTran tran1 : trans1) {
						
						String label = tran1.getTran();
						String stateTo1 = tran1.getTo();
						LTSTran tempTran = getTranByLabel(label, trans2);
						String stateTo2 = tempTran.getTo();
						ConsistState conStateTo = new ConsistState();
						conStateTo.setState1(stateTo1);
						conStateTo.setState2(stateTo2);
						
						if (!isVisitedConState(visitedQueue, conStateTo)) {
					    	visitingQueue.offer(conStateTo);
							visitedQueue.add(conStateTo);
						}
					}
				}
			}
	    }
		return true;
	}
	
	//�ж�һ��״̬conStateTo�Ƿ��Ѿ�������
	private boolean isVisitedConState(List<ConsistState> visitedQueue,
			ConsistState conStateTo) {
		for (ConsistState conState: visitedQueue) {
			String state1 = conState.getState1();
			String state2 = conState.getState2();
			String stateTo1 = conStateTo.getState1();
			String stateTo2 = conStateTo.getState2();
			if (state1.equals(stateTo1) && state2.equals(stateTo2)) {
				return true;
			}
		}
		return false;
	}

	//��ȡlts�д�state�����ı�Ǩ
	public List<LTSTran> getTrans(String state, LTS lts) {
		List<LTSTran> tempTrans = new ArrayList<>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String from = tran.getFrom();
			if (state.equals(from)) {
				tempTrans.add(tran);
			}
		}
		return tempTrans;
	}
	
	//�����ֱ���Ƿ��ظ�
	public List<String> getLabels(List<LTSTran> trans) {
		List<String> labels = new ArrayList<String>();
        for (LTSTran tran : trans) {
			labels.add(tran.getTran());
		}
        return labels;
	}
	
	//��ȡ����ͬ����ŵ�Ǩ��
	public LTSTran getTranByLabel(String label, List<LTSTran> trans) {
		for (LTSTran tran : trans) {
			String tempLabel = tran.getTran();
			if (label.equals(tempLabel)) {
				return tran;
			}
		}
		return null;
	}

}
