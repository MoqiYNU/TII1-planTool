package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;

/**
 * @author Moqi
 * ����ü�Utils
 */
public class PruneUtils {
	
	private ClosureUtils generateClosure;
	
	public PruneUtils() {
		generateClosure = new ClosureUtils();
	}
	
	//��ȡЭ�����ּ�
	public List<String> getCoodNames(LTS orgProLTS, LTS core) {
		
		List<String> corNames = new ArrayList<String>();
		List<String> coreStates = core.getStates();
		for (String coreState : coreStates) {
			List<String> transInOrgPro = getTrans(coreState, orgProLTS);
			List<String> transInCore = getTrans(coreState, core);
			List<String> tempCorNames = (List<String>) CollectionUtils.subtract(transInOrgPro, transInCore);
			corNames.addAll(tempCorNames);
		}
		return corNames;
	}
	
	//��ȡlts�д�state�����ı�Ǩ
	public List<String> getTrans(String state, LTS lts) {
		List<String> tempTrans = new ArrayList<String>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String from = tran.getFrom();
			String label = tran.getTran();
			if (state.equals(from)) {
				tempTrans.add(label);
			}
		}
		return tempTrans;
	}
	
	//�ü�LTS
	public LTS prune(LTS lts) {
		
		List<String> ends = lts.getEnds();
		List<String> states = lts.getStates();
		List<String> prunedStates = new ArrayList<String>();
		List<String> validStates = new ArrayList<String>();
        for (String state : states) {
			List<String> tranClosure = generateClosure.getTranClosure(state, lts);
			//1)Ǩ�Ʊհ���δ������ֹ״̬,��state����Ч״̬
			if (CollectionUtils.intersection(tranClosure, ends).size() == 0) {
				prunedStates.add(state);
			}else {//2)state����Ч״̬
				validStates.add(state);
			}
		}
        
		List<LTSTran> validTrans = new ArrayList<>();
		List<LTSTran> transitions = lts.getLTSTrans();
		for (LTSTran transition : transitions) {
			String from = transition.getFrom();
			String to = transition.getTo();
			//��ʾ��Ҫ�ü���Ǩ��
			if (prunedStates.contains(from) || prunedStates.contains(to)) {
				continue;
			}
			validTrans.add(transition);
		}
		
		 LTS prunedLTS = new LTS();
	     prunedLTS.setStart(lts.getStart());
	     prunedLTS.setStates(validStates);
	     prunedLTS.setEnds(ends);
	     prunedLTS.setLTSTrans(validTrans);
	     return prunedLTS;
	     
	}
	

}
