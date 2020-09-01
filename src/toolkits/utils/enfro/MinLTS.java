package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;

/**
 * @author Moqi
 * LTS��С��Utils
 */
public class MinLTS {
	
	private ClosureUtils generateClosure;
	
	public MinLTS() {
		generateClosure = new ClosureUtils();
	}
	
	//����ʹ����LTS��Э��Ǩ��ȫ���Ƴ�
	public LTS rovCoodTrans(LTS enfProLTS) {
		
		Map<String, String> map = new HashMap<String, String>();//Э��״̬ӳ��Ϊһ��״̬
		List<LTSTran> notCoodTrans = new ArrayList<>();//��Э��Ǩ�Ƽ�
		
		List<LTSTran> trans = enfProLTS.getLTSTrans();
		for (LTSTran tran : trans) {
			String from  = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			//1.Э��Ǩ��
			if (label.length() > 6 && label.substring(0, 4).equals("sync")) {
				if (label.charAt(5) == '1') {//sync_1_,��toΪ����Э��״̬
					map.put(to, from);
				}else if (label.charAt(5) == '2') {//sync_2_,��fromΪ����Э��״̬
					map.put(from, to);
				}
			}else {//2.��Э��Ǩ��
				notCoodTrans.add(tran);
			}
		}
		
		//Լ�������Ǩ��
		List<LTSTran> genTrans = new ArrayList<>();
        for (LTSTran tran : notCoodTrans) {
        	
        	String from  = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			
			LTSTran tempTran = new LTSTran();
			tempTran.setTran(label);
			String value1 = map.get(from);
			String value2 = map.get(to);
			//1.from��toҪôͬʱ��Э��״̬Ҫôȫ������
			if (value1 != null && value2 != null) {
				tempTran.setFrom(value1);
				tempTran.setTo(value2);
				genTrans.add(tempTran);
			}else {
				tempTran.setFrom(from);
				tempTran.setTo(to);
				genTrans.add(tempTran);
			}
		}
        
        //�Ƴ�Э��Ǩ�ƺ��������ʹ����
        LTS genEnfProLTS = new LTS();
        genEnfProLTS.setStart(enfProLTS.getStart());
        genEnfProLTS.setEnds(enfProLTS.getEnds());
        genEnfProLTS.setLTSTrans(genTrans);
		List<String> genStates = new ArrayList<String>();
		for (LTSTran genTran : genTrans) {
			String genFrom = genTran.getFrom();
			if (!genStates.contains(genFrom)) {
				genStates.add(genFrom);
			}
			String genTo = genTran.getTo();
			if (!genStates.contains(genTo)) {
				genStates.add(genTo);
			}
		}
		genEnfProLTS.setStates(genStates);
		
		return genEnfProLTS;
		
	}
	
	//�������켣�ȼ���С��LTS 
	public LTS min(LTS lts, int flag) {
		
		List<String> minStates = new ArrayList<>();
		List<LTSTran> minTrans = new ArrayList<>();
		
		String initState = lts.getStart();
		List<String> endStates = lts.getEnds();
		
		List<String> names = getNames(lts);
		
		int index = 0;
		
		MinState initMinState = new MinState();
		//���initState��tau�հ�
		List<String> initClosure = generateClosure.genTauClosure(initState, lts);
		initMinState.addStates(initClosure);
		String initMinStateIdf = "BP" + flag + index;
		initMinState.setMinStateIdf(initMinStateIdf);
		minStates.add(initMinStateIdf);
		
		Queue<MinState> visitingQueue = new LinkedList<>(); 
  		List<MinState> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(initMinState);
  		visitedQueue.add(initMinState);
		
  	    //��������
  		while(visitingQueue.size() > 0){
  			
  		    //����һ����ʶ,�Դ˽���Ǩ��
  			MinState minStateFrom = visitingQueue.poll();
  			String minStateFromIdf = minStateFrom.getMinStateIdf();
  			List<String> minStatesFrom = minStateFrom.getStates();
  			
  			for (String name : names) {
  				
  				List<String> moveStates = getMoveByNames(minStatesFrom, name, lts);
  				
  				if (moveStates.size() == 0) {//�����ܴ�nameǨ��,������
					continue;
				}
  				
  				//����ܹ�Ǩ��
  				List<String> closureTo = new ArrayList<String>();
                for (String moveState : moveStates) {
					List<String> closure = generateClosure.genTauClosure(moveState, lts);
					for (String closureElem : closure) {
						if (!closureTo.contains(closureElem)) {
							closureTo.add(closureElem);
						}
					}
				}
                
                String idf = isGenerated(visitedQueue, closureTo);
                if (idf == null) {//1.closureToδ���ɹ�
                	
                	MinState minStateTo = new MinState();
                	index ++;
                	String minStateToIdf = "BP" + flag + index;
                	minStateTo.setMinStateIdf(minStateToIdf);
                	minStateTo.setStates(closureTo);
                	visitingQueue.offer(minStateTo);
              		visitedQueue.add(minStateTo);
              		
              		minStates.add(minStateToIdf); 
              		
              		LTSTran tempTran = new LTSTran();
              		tempTran.setFrom(minStateFromIdf);
              		tempTran.setTran(name);
              		tempTran.setTo(minStateToIdf);
              		minTrans.add(tempTran);
              		
				}else {//2.closureTo���ɹ�,��ֻ���Ǩ�Ƽ���
					
					LTSTran tempTran = new LTSTran();
              		tempTran.setFrom(minStateFromIdf);
              		tempTran.setTran(name);
              		tempTran.setTo(idf);
              		minTrans.add(tempTran);
					
				}
                
  			}
  			
  		}
        
  		LTS minLts = new LTS();
  		minLts.setStart(initMinStateIdf);
  		minLts.setEnds(getMinLtsEndStateIdf(visitedQueue, endStates));
  		minLts.setStates(minStates);
  		minLts.setLTSTrans(minTrans);
  		
  		return minLts;
		
	}
	
	//���MinLTS��ֹ״̬��ʶ
	public List<String> getMinLtsEndStateIdf(List<MinState> visitedQueue, List<String> endStates) {
		List<String> minEnds = new ArrayList<String>();
		for (MinState minState : visitedQueue) {
			//minState����ֹ��ʶ,���ҽ�����״̬�к�����ֹ״̬
			if (CollectionUtils.intersection(minState.getStates(), endStates).size() > 0) {
				minEnds.add(minState.getMinStateIdf());
			}
		}
		return minEnds;
	}
	
	//�ж�����״̬�Ƿ�������,���ɹ��򷵻�idf
	public String isGenerated(List<MinState> visitedQueue, List<String> closureTo) {
        for (MinState minState : visitedQueue) {
			if (CollectionUtils.isEqualCollection(minState.getStates(), closureTo)) {
				return minState.getMinStateIdf();
			}
		}
        return null;
	}
	
	//��õ�minStatesFrom��Ǩ��name���״̬��
	public List<String> getMoveByNames(List<String> minStatesFrom, String name, LTS lts) {
		List<String> moveStates = new ArrayList<String>();
        for (String minState : minStatesFrom) {
			List<String> statesTo = getMoveByName(minState, name, lts);
			for (String stateTo : statesTo) {
				if (!moveStates.contains(stateTo)) {
					moveStates.add(stateTo);
				}
			}
		}
        return moveStates;
	}
	
	//���minState��Ǩ��name���״̬��
	public List<String> getMoveByName(String minState, String name, LTS lts) {
		List<String> moveStates = new ArrayList<String>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String from = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			if (minState.equals(from) && label.equals(name)) {
				//System.out.println("Test: " + to);
				if (!moveStates.contains(to)) {
					moveStates.add(to);
				}
			}
		}
		return moveStates;
	}
	
	//���LTS���������ּ�(�ų�tau)
	public List<String> getNames(LTS lts) {
		List<String> names = new ArrayList<String>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String label = tran.getTran();
			if (label.equals("tau")) {//tau����
				continue;
			}
			if (!names.contains(label)) {
				names.add(label);
			}
		}
		return names;
	}
	
}
