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
import toolkits.utils.lts.CompLTS;
import toolkits.utils.lts.CompState;
import toolkits.utils.lts.CompTransition;
import toolkits.utils.lts.ExpandState;
import toolkits.utils.lts.ExpandTransition;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * ������ʹ����LTS
 */
public class GetEnforProLTS {
	
	PetriUtils petriUtils;
	private CompLTS compLTS;
	
	public GetEnforProLTS() {
		petriUtils = new PetriUtils();
		compLTS = new CompLTS();
	}
	
	//���Դ���̺Ϳ���������CompLTS
	@SuppressWarnings("static-access")
	public CompLTS genCompLTS(ProNet orgPro, List<LTS> controllers) {
		
		//�����������Ǩ�Ƽ�
		List<CompTransition> compTransitions = new ArrayList<>();
		
		//Դ���̿�ʼ��ʶ
		Marking initMarking = orgPro.getSource();
		
		//��������ʼ���״̬
		List<String> initStates = new ArrayList<String>();
        for (LTS controller : controllers) {
			String initState = controller.getStart();
			initStates.add(initState);
		}
        ExpandState initExpandState = new ExpandState();
        initExpandState.setStates(initStates);
        
        CompState initCompState = new CompState();
        initCompState.setMarking(initMarking);
        initCompState.setExpandState(initExpandState);
        
        //�������ʵĶ���visitingQueue���Ѿ����ʹ�����visitedQueue
  		Queue<CompState> visitingQueue = new LinkedList<>(); 
  		List<CompState> visitedQueue = new ArrayList<>();
  		//����ʼ��ʶ��Ӳ���Ϊ�Ѿ�����
  		visitingQueue.offer(initCompState);
  		visitedQueue.add(initCompState);
        
  		//int index = 0;
  	    //��������
	    while(visitingQueue.size() > 0){
	    	
	    	//����һ�����״̬,�Դ˽���Ǩ��
	    	CompState compState = visitingQueue.poll();
	    	Marking markingFrom = compState.getMarking();
	    	List<String> placesFrom = markingFrom.getPlaces();
	    	ExpandState expandStateFrom = compState.getExpandState();
	    	
	    	//����ʹ�ܻ����
			List<String> enableActs = petriUtils.getEnableTrans(orgPro, placesFrom);
			List<ExpandTransition> succExpandTrans = compLTS.getSucceedExpandTrans(controllers, expandStateFrom);
			
			//System.out.println("Index: " + index + "Succ trans: " + succExpandTrans.size());
			
			for (ExpandTransition succExpandTran : succExpandTrans) {
				String label = succExpandTran.getLabel();
				ExpandState expandStateTo = succExpandTran.getExpandStateTo();
				
				List<String> acts = getActs(label, enableActs);
				
				//System.out.println("Acts: " + acts + " enableActs: " + enableActs);
				
				if (acts.size() == 0) {//1)��ͬ����Ǩ,��ֻ��Ǩ�����LTS
					
					int labelSize = label.length();
					if (labelSize > 4) {//�ж��Ƿ�Ϊsync�
						String prefix = label.substring(0, 4);
						if (prefix.equals("sync")) {
							CompState compStateTo = new CompState();
						    compStateTo.setMarking(markingFrom);
						    compStateTo.setExpandState(expandStateTo);
							
							CompTransition compTran = new CompTransition();
						    compTran.setCompStateFrom(compState);
						    compTran.setTran(label);
						    compTran.setCompStateTo(compStateTo);
						    compTransitions.add(compTran);
						    
						    if (!isVisitedCompState(visitedQueue, compStateTo)) {
						    	visitingQueue.offer(compStateTo);
								visitedQueue.add(compStateTo);
							}
						}
					}
				    
				}else {//2)��Petri���б�Ǩ,ͬʱǨ��petri�������LTS
					for (String act : acts) {
						List<String> placesTo = petriUtils.getPlacesTo(orgPro, placesFrom, act);
						Marking markingTo = new Marking();
						markingTo.setPlaces(placesTo);
						
						CompState compStateTo = new CompState();
						compStateTo.setMarking(markingTo);
						compStateTo.setExpandState(expandStateTo);
						
					    CompTransition compTran = new CompTransition();
					    compTran.setCompStateFrom(compState);
					    compTran.setTran(label);
					    compTran.setCompStateTo(compStateTo);
					    compTransitions.add(compTran);
					    
					    if (!isVisitedCompState(visitedQueue, compStateTo)) {
					    	visitingQueue.offer(compStateTo);
							visitedQueue.add(compStateTo);
						}
					}
				}
			}
	    }
	    
	    //��ȡ��ֹ���״̬
	    List<CompState> ends = new ArrayList<CompState>();
        for (CompState compState : visitedQueue) {
			if (isEndCompState(compState, orgPro, controllers)) {
				ends.add(compState);
			}
		}
	    
	    //System.out.println("VisitedQueue: " + visitedQueue.size());
	    CompLTS compLTS = new CompLTS();
	    compLTS.setInitCompState(initCompState);
	    compLTS.setEndCompStates(ends);
	    compLTS.setCompStates(visitedQueue);
	    compLTS.setCompTransitions(compTransitions);
	    return compLTS;
		
	}
	
	//��compLTSת��ΪLTS
	public LTS generate(ProNet orgPro, List<LTS> controllers) {
		
		CompLTS compLTS = genCompLTS(orgPro, controllers);
		
		LTS lts = new LTS();
		lts.setStart("S" + 0);
		
		List<String> statesLTS = new ArrayList<String>();
		List<CompState> states = compLTS.getCompStates();
		int stateSize = states.size();
		for (int i = 0; i < stateSize; i++) {
			String stateLTS = "S" + i;
			statesLTS.add(stateLTS);
		}
		lts.setStates(statesLTS);;
		
		List<LTSTran> transLTS = new ArrayList<>();
		List<CompTransition> compTrans = compLTS.getCompTransitions();
		for (CompTransition compTran : compTrans) {
			CompState compStatefrom = compTran.getCompStateFrom();
			String label = compTran.getTran();
			CompState compStateto = compTran.getCompStateTo();
			//ӳ��ΪSi
			int from = getIndex(states, compStatefrom);
			int to = getIndex(states, compStateto);
			LTSTran tranLTS = new LTSTran();
			tranLTS.setFrom("S" + from);
			tranLTS.setTran(label);
			tranLTS.setTo("S" + to);
			transLTS.add(tranLTS);
		}
		lts.setLTSTrans(transLTS);
		
		List<String> endStrs = new ArrayList<String>();
		List<CompState> ends = compLTS.getEndCompStates();
		for (CompState end : ends) {
			int index = getIndex(states, end);
			endStrs.add("S" + index);
		}
		lts.setEnds(endStrs);
		return lts;
		
	}
	
	//�ж�compStateTo�Ƿ��Ѿ�����
	private boolean isVisitedCompState(List<CompState> visitedQueue,
			CompState compStateTo) {
		for (CompState compState : visitedQueue) {
			if (isEqualCompStates(compState, compStateTo)) {
				return true;
			}
		}
		return false;
	}
	
	//�ж��Ƿ���ֹ���״̬
	public boolean isEndCompState(CompState compState, ProNet petri, List<LTS> bizPros) {
		Marking marking = compState.getMarking();
		ExpandState expandState = compState.getExpandState();
		if (isEndMarking(marking, petri) && isEndExpandStates(expandState, bizPros)) {
			return true;
		}
		return false;
	}
	
	//�ж��������״̬�Ƿ���ͬ
	public boolean isEqualCompStates(CompState compState1, CompState compState2) {
		if (CollectionUtils.isEqualCollection(compState1.getMarking().getPlaces(), compState2.getMarking().getPlaces())
				&& isEqualExpandStates(compState1.getExpandState(), compState2.getExpandState())) {
			return true;
		}
		return false;
	}
	
	//�ж��Ƿ���ֹ��ʶ
	public boolean isEndMarking(Marking marking, ProNet petri) {
		List<Marking> finalMarkings = petri.getSinks();
		for (Marking tempMarking : finalMarkings) {
			if (CollectionUtils.isEqualCollection(tempMarking.getPlaces(), marking.getPlaces())) {
				return true;
			}
		}
		return false;
	}
	
	//�ж��Ƿ���ֹչ��״̬
	public boolean isEndExpandStates(ExpandState expandState, List<LTS> bizPros) {
		int size = expandState.getStates().size();
		for (int i = 0; i < size; i++) {
			String state = expandState.getStates().get(i);
			List<String> ends = bizPros.get(i).getEnds();
			if (ends.contains(state)) {
				continue;
			}else {
				return false;
			}
		}
		return true;
	}

	//�ж�����չ��״̬�Ƿ����
	public boolean isEqualExpandStates(ExpandState expandState1, ExpandState expandState2) {
		List<String> states1 = expandState1.getStates();
		int size1 = states1.size();
		List<String> states2 = expandState2.getStates();
		int size2 = states2.size();
		if (size1 != size2) {
			return false;
		}else {
			for (int i = 0; i < size1; i++) {
				String state1 = states1.get(i);
				String state2 = states2.get(i);
				if (state1.equals(state2)) {
					continue;
				}else {
					return false;
				}
				
			}
		}
		return true;
	}
	
	//��ȡ�������е�ǰʹ�ܱ�Ǩ��(��tran_ID��ʶ)
	public List<String> getActs(String label, List<String> enableActs) {
		List<String> acts = new ArrayList<String>();
		for (String enableAct : enableActs) {
			if (label.equals(enableAct)) {
				acts.add(enableAct);
			}
		}
		return acts;
	}
	
	//��ȡcompState��λ��
	private int getIndex(List<CompState> states, CompState compState) {
		int size = states.size();
		for (int i = 0; i < size; i++) {
			CompState tempCompState = states.get(i);
			if (isEqualCompStates(compState, tempCompState)) {
				return i;
			}
		}
		return -1;
	}

}
