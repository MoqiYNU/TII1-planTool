package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;

/**
 * @author Moqi
 * 定义LTS的迁移/tau闭包
 */
public class ClosureUtils {
	
	
	
	/**************************获取t-closure*************************/
	
	public List<String> getTranClosure(String state, LTS lts) {
		
		Queue<String> visitingQueue = new LinkedList<>(); 
  		List<String> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(state);
  		visitedQueue.add(state);//包括state自己
  		
  	    //迭代计算
  		while(visitingQueue.size() > 0){
  			//出对一个标识,以此进行迁移
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
  		//visitedQueue.add(state);//包括state自己
  		
  	    //迭代计算
  		while(visitingQueue.size() > 0){
  			//出对一个标识,以此进行迁移
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
	
	//获得stateFrom的迁移状态
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
	
	
	/**************************获取tau-closure*************************/
	
	public List<String> genTauClosure(String state, LTS lts) {
		
		Queue<String> visitingQueue = new LinkedList<>(); 
  		List<String> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(state);
  		visitedQueue.add(state);//包括state自己
  		
  	    //迭代计算
  		while(visitingQueue.size() > 0){
  			//出对一个标识,以此进行迁移
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
	
	//获得stateFrom的tau迁移状态
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
