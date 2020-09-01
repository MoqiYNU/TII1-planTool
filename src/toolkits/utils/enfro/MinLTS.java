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
 * LTS最小化Utils
 */
public class MinLTS {
	
	private ClosureUtils generateClosure;
	
	public MinLTS() {
		generateClosure = new ClosureUtils();
	}
	
	//将迫使过程LTS中协调迁移全部移除
	public LTS rovCoodTrans(LTS enfProLTS) {
		
		Map<String, String> map = new HashMap<String, String>();//协调状态映射为一般状态
		List<LTSTran> notCoodTrans = new ArrayList<>();//非协调迁移集
		
		List<LTSTran> trans = enfProLTS.getLTSTrans();
		for (LTSTran tran : trans) {
			String from  = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			//1.协调迁移
			if (label.length() > 6 && label.substring(0, 4).equals("sync")) {
				if (label.charAt(5) == '1') {//sync_1_,则to为引入协调状态
					map.put(to, from);
				}else if (label.charAt(5) == '2') {//sync_2_,则from为引入协调状态
					map.put(from, to);
				}
			}else {//2.非协调迁移
				notCoodTrans.add(tran);
			}
		}
		
		//约减后产生迁移
		List<LTSTran> genTrans = new ArrayList<>();
        for (LTSTran tran : notCoodTrans) {
        	
        	String from  = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			
			LTSTran tempTran = new LTSTran();
			tempTran.setTran(label);
			String value1 = map.get(from);
			String value2 = map.get(to);
			//1.from和to要么同时是协调状态要么全部不是
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
        
        //移除协调迁移后产生的迫使过程
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
	
	//利用弱轨迹等价最小化LTS 
	public LTS min(LTS lts, int flag) {
		
		List<String> minStates = new ArrayList<>();
		List<LTSTran> minTrans = new ArrayList<>();
		
		String initState = lts.getStart();
		List<String> endStates = lts.getEnds();
		
		List<String> names = getNames(lts);
		
		int index = 0;
		
		MinState initMinState = new MinState();
		//求的initState的tau闭包
		List<String> initClosure = generateClosure.genTauClosure(initState, lts);
		initMinState.addStates(initClosure);
		String initMinStateIdf = "BP" + flag + index;
		initMinState.setMinStateIdf(initMinStateIdf);
		minStates.add(initMinStateIdf);
		
		Queue<MinState> visitingQueue = new LinkedList<>(); 
  		List<MinState> visitedQueue = new ArrayList<>(); 
  		visitingQueue.offer(initMinState);
  		visitedQueue.add(initMinState);
		
  	    //迭代计算
  		while(visitingQueue.size() > 0){
  			
  		    //出对一个标识,以此进行迁移
  			MinState minStateFrom = visitingQueue.poll();
  			String minStateFromIdf = minStateFrom.getMinStateIdf();
  			List<String> minStatesFrom = minStateFrom.getStates();
  			
  			for (String name : names) {
  				
  				List<String> moveStates = getMoveByNames(minStatesFrom, name, lts);
  				
  				if (moveStates.size() == 0) {//若不能从name迁移,则跳过
					continue;
				}
  				
  				//如果能够迁移
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
                if (idf == null) {//1.closureTo未生成过
                	
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
              		
				}else {//2.closureTo生成过,则只添加迁移即可
					
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
	
	//获得MinLTS终止状态标识
	public List<String> getMinLtsEndStateIdf(List<MinState> visitedQueue, List<String> endStates) {
		List<String> minEnds = new ArrayList<String>();
		for (MinState minState : visitedQueue) {
			//minState是终止标识,当且仅当其状态中含有终止状态
			if (CollectionUtils.intersection(minState.getStates(), endStates).size() > 0) {
				minEnds.add(minState.getMinStateIdf());
			}
		}
		return minEnds;
	}
	
	//判断最新状态是否生产过,生成过则返回idf
	public String isGenerated(List<MinState> visitedQueue, List<String> closureTo) {
        for (MinState minState : visitedQueue) {
			if (CollectionUtils.isEqualCollection(minState.getStates(), closureTo)) {
				return minState.getMinStateIdf();
			}
		}
        return null;
	}
	
	//获得得minStatesFrom的迁移name后的状态集
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
	
	//获得minState的迁移name后的状态集
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
	
	//获得LTS的所有名字集(排除tau)
	public List<String> getNames(LTS lts) {
		List<String> names = new ArrayList<String>();
		List<LTSTran> trans = lts.getLTSTrans();
		for (LTSTran tran : trans) {
			String label = tran.getTran();
			if (label.equals("tau")) {//tau跳过
				continue;
			}
			if (!names.contains(label)) {
				names.add(label);
			}
		}
		return names;
	}
	
}
