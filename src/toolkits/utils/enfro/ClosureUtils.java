package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;

/**
 * @author Moqi
 * ����LTS��Ǩ��/tau�հ�
 */
public class ClosureUtils {
	
	
	
	/**************************��ȡt-closure*************************/
	
	public List<String> getTranClosure(String state, LTS lts) {
		
		Queue<String> visitingQueue = new LinkedList<>(); 
  		List<String> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(state);
  		visitedQueue.add(state);//����state�Լ�
  		
  	    //��������
  		while(visitingQueue.size() > 0){
  			//����һ����ʶ,�Դ˽���Ǩ��
  			String stateFrom = visitingQueue.poll();
  			List<String> tranStates = getTranStates(stateFrom, lts);
  			
  			for (String tranState : tranStates) {
				if (!visitedQueue.contains(tranState)) {
					visitingQueue.offer(tranState);
			  		visitedQueue.add(tranState);
				}
			}
  		}
		return visitedQueue;
	}
	
    public List<String> getRestTranClosure(String state, LTS lts) {
		
		Queue<String> visitingQueue = new LinkedList<>(); 
  		List<String> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(state);
  		//visitedQueue.add(state);//����state�Լ�
  		
  	    //��������
  		while(visitingQueue.size() > 0){
  			//����һ����ʶ,�Դ˽���Ǩ��
  			String stateFrom = visitingQueue.poll();
  			List<String> tranStates = getTranStates(stateFrom, lts);
  			
  			for (String tranState : tranStates) {
				if (!visitedQueue.contains(tranState)) {
					visitingQueue.offer(tranState);
			  		visitedQueue.add(tranState);
				}
			}
  		}
		return visitedQueue;
	}
	
	//���stateFrom��Ǩ��״̬
	public List<String> getTranStates(String stateFrom, LTS lts) {
		List<String> tranStates = new ArrayList<String>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String from = tran.getFrom();
			String to = tran.getTo();
			if (from.equals(stateFrom)) {
				if (!tranStates.contains(to)) {
					tranStates.add(to);
				}
			}
		}
		return tranStates;
	}
	
	
	/**************************��ȡtau-closure*************************/
	
	public List<String> genTauClosure(String state, LTS lts) {
		
		Queue<String> visitingQueue = new LinkedList<>(); 
  		List<String> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(state);
  		visitedQueue.add(state);//����state�Լ�
  		
  	    //��������
  		while(visitingQueue.size() > 0){
  			//����һ����ʶ,�Դ˽���Ǩ��
  			String stateFrom = visitingQueue.poll();
  			List<String> tauStates = getTauStates(stateFrom, lts);
  			
  			for (String tauState : tauStates) {
				if (!visitedQueue.contains(tauState)) {
					visitingQueue.offer(tauState);
			  		visitedQueue.add(tauState);
				}
			}
  		}
		return visitedQueue;
	}
	
	//���stateFrom��tauǨ��״̬
	public List<String> getTauStates(String stateFrom, LTS lts) {
		List<String> tauStates = new ArrayList<String>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String from = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			if (from.equals(stateFrom) && label.equals("tau")) {
				if (!tauStates.contains(to)) {
					tauStates.add(to);
				}
			}
		}
		return tauStates;
	}

}
