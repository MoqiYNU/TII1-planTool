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
 * 定义检测Utils
 */
public class CheckUtils {
	
	private ClosureUtils closureUtils;
	
	public CheckUtils() {
		closureUtils = new ClosureUtils();
		
	}
	
	//判断Bi方法是否有效
	public boolean isValid_BiApproach(ProNet orgPro, LTS lts, LTS rlts) {
		
		// 1.Bi方法不能迫使涉及同步的源过程
		List<String> trans = orgPro.getTrans();
		for (String tran : trans) {
			//将tran按'_'切分(若为同步合并变迁,则形如:T1_T2_T3)
			String[] syncTrans = tran.split("\\_");
			//1)tran不是同步合并迁移
			if (syncTrans.length == 1) {
				continue;
			}else {//2)tran是同步合并迁移,则Bi方法退出
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
		//计算源过程RG中每个状态的受限迁移闭包
		for (String state : states) {
			List<String> tranClosure = closureUtils.getRestTranClosure(state, lts);
			//若tranClosure中未含终止状态且state不为终止状态,则为无效状态
			if (CollectionUtils.intersection(tranClosure, ends).size() == 0
					&& !ends.contains(state)) {
				invalidStates.add(state);
				//若tranClosure为空,则为死锁
				if (tranClosure.size() == 0) {
					deadlocks.add(state);
				}else {
					interStates.add(state);
				}
			}else {
				validStates.add(state);
			}
		}
		
		// 2.Bi方法不能迫使含活锁的源过程
        for (String interState : interStates) {
        	List<String> tranClosure = closureUtils.getRestTranClosure(interState, lts);
			//若中间状态(即异常状态)不能到达死锁状态,则为活锁
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
			//若tranClosure中未含终止状态且state不为终止状态,则为无效状态
			if (CollectionUtils.intersection(tranClosure, rends).size() == 0
					&& !rends.contains(rState)) {
				rinvalidStates.add(rState);
			}else {
				rvalidStates.add(rState);
			}
		}
		
		//计算RG中受控标识集
		List<Marking> ctStates = new ArrayList<>();
        for (String validState : validStates) {
			List<String> tranStates = closureUtils.getTranStates(validState, lts);
			if (CollectionUtils.intersection(tranStates, invalidStates).size() != 0) {
				System.out.println("ctState in RG: " + validState);
				ctStates.add(lts.getStateMarkingMap().get(validState));
			}
		}
        
        //计算RRG中受控标识集
        List<Marking> rctStates = new ArrayList<>();
        for (String rvalidState : rvalidStates) {
			List<String> tranStates = closureUtils.getTranStates(rvalidState, rlts);
			if (CollectionUtils.intersection(tranStates, rinvalidStates).size() != 0) {
				System.out.println("ctState in RRG: " + rvalidState);
				rctStates.add(rlts.getStateMarkingMap().get(rvalidState));
			}
		}
		
		// 3.若RG中所有受控标识在RRG重现,则返回true
		if (MarkingUtils.markingsIsEqual(ctStates, rctStates)) {
			return true;
		}else {
			System.out.println("some controlled markings are missing...");
			return false;
		}
		
	}
	
	//根据源过程LTS检测其正确性
	public String checkCorrect(LTS orgProLTS) {
		
		List<String> validStates = new ArrayList<String>();
		List<String> states = orgProLTS.getStates();
		List<String> ends = orgProLTS.getEnds();
		//计算每个状态的迁移闭包
		for (String state : states) {
			List<String> tranClosure = closureUtils.getTranClosure(state, orgProLTS);
			//state的迁移闭包中包含终止状态
			if (CollectionUtils.intersection(ends, tranClosure).size() != 0) {
				validStates.add(state);
			}
		}
		//根据有限状态判断正确性
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
	
	//检测核与迫使过程是否一致
	public boolean checkConsist(LTS core, LTS enfProLTS) {
		
		MinLTS minLTS = new MinLTS();
		
		//1.将迫使过程中引入的协调迁移全部移除后生成的迫使过程
		LTS genEnfPro = minLTS.rovCoodTrans(enfProLTS);
		
		//2.进行一致性检测
		String initStateInCore = core.getStart();
		String initStateInEnf = genEnfPro.getStart();
		ConsistState initConState = new ConsistState();
		initConState.setState1(initStateInCore);
		initConState.setState2(initStateInEnf);
		//即将访问的队列visitingQueue和已经访问过队列visitedQueue
		Queue<ConsistState> visitingQueue = new LinkedList<>(); 
		List<ConsistState> visitedQueue = new ArrayList<>();
		//将初始标识入队并置为已经访问
		visitingQueue.offer(initConState);
		visitedQueue.add(initConState);
		
		//迭代计算
	    while(visitingQueue.size() > 0){
	    	
	    	//出对一个标识,以此进行迁移
	    	ConsistState conStateFrom = visitingQueue.poll();
	    	String stateFrom1 = conStateFrom.getState1();
	    	String stateFrom2 = conStateFrom.getState2();
	    	
	    	//System.err.println("Relation: " + "(" + stateFrom1 + ", " + stateFrom2 + ")");
	    	
	    	//1.判断state1和state2是否都为终止状态或均不为终止状态
	    	if (core.getEnds().contains(stateFrom1) && !genEnfPro.getEnds().contains(stateFrom2) || 
	    			!core.getEnds().contains(stateFrom1) && genEnfPro.getEnds().contains(stateFrom2)) {
	    		return false;
			}else {//2.state1和state2都为终止状态或均不为终止状态
				List<LTSTran> trans1 = getTrans(stateFrom1, core);
		    	List<LTSTran> trans2 = getTrans(stateFrom2, genEnfPro);
		    	
		    	List<String> labels1 = getLabels(trans1);
		    	List<String> labels2 = getLabels(trans2);
		    	
		    	//判断由state1和state2出发的迁移是否存在一一映射关系
		    	//Note:对于state1而言,其出发的迁移标号(Petri网中变迁)必定是不重复的
		    	if (!CollectionUtils.isEqualCollection(labels1, labels2)) {
					return false;
				}else {
					//存在一一映射关系
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
	
	//判断一致状态conStateTo是否已经被访问
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

	//获取lts中从state出发的变迁
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
	
	//不区分标号是否重复
	public List<String> getLabels(List<LTSTran> trans) {
		List<String> labels = new ArrayList<String>();
        for (LTSTran tran : trans) {
			labels.add(tran.getTran());
		}
        return labels;
	}
	
	//获取具有同样标号的迁移
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
