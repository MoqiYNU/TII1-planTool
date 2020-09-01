package toolkits.utils.lts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;

public class SemanticUtils {
	
	//获取第i个业务过程在状态curState下使能活动集合
    public List<EnableActivity> getEnableActivities(int index, String curState, LTS bizPro){
        
    	//定义使能活动集合
    	List<EnableActivity> enableActivities = new ArrayList<EnableActivity>();
    	
    	List<LTSTran> transitions = bizPro.getLTSTrans();
    	for (LTSTran transition : transitions) {
			String stateFrom = transition.getFrom();
			String label = transition.getTran();
			String stateTo = transition.getTo();
			if (curState.equals(stateFrom)) {
				//生成使能活动集合
				EnableActivity enableActivity = new EnableActivity();
				//使能活动以位置标识
				enableActivity.setIndex(index);
				enableActivity.setActivity(label);
				enableActivity.setStateTo(stateTo);
				//添加至使能活动集合
				enableActivities.add(enableActivity);
			}
		}
    	return enableActivities;
    }
    
    //判断一个业务过程是否需要在名字name同步
  	public boolean isSynBizPro(LTS bizPro, String name) {
  		List<LTSTran> transitions = bizPro.getLTSTrans();
  		for (LTSTran transition : transitions) {
  			String label = transition.getTran();
  			if (label.equals(name)) {
  				return true;
  			}
  		}
  		return false;
  	}
    
    //获得所有需要在名字name上同步的业务过程集合
  	public List<Integer> getSynchronSet(List<LTS> bizPros, String name) {
  		List<Integer> synchronBizPro = new ArrayList<Integer>();
  		for (int i = 0; i < bizPros.size(); i++) {
  			LTS lts = bizPros.get(i);
  			if (isSynBizPro(lts, name)) {
  				synchronBizPro.add(i);
  			}
  		}
  		return synchronBizPro;
  	}
  	
    //确定是否能够同步
  	public boolean isSynchron(List<Integer> synBizPros, SynEnableActivity synEnableActivity) {
  		List<Integer> bizPros = new ArrayList<Integer>();
  		List<EnableActivity> enableActivities = synEnableActivity.getEnableActivities();
  		for (EnableActivity enableActivity : enableActivities) {
  			int bizPro = enableActivity.getIndex();
  			bizPros.add(bizPro);
  		}
  		if (CollectionUtils.isEqualCollection(synBizPros, bizPros)) {
  			return true;
  		}else {
  			return false;
  		}
  	}
  	
    // 获得并发迁移划分集.
 	// eg CTS=({P1.c1,P1.c2},{P2.c1,P2.c2});
 	// 则  PTS=({P1.c1,P2.c1},{P1.c1,P2.c2},{P1.c2,P2.c1},{P1.c2,P2.c2}).
 	public List<SynEnableActivity> getSynEnableActivityInOpt(List<EnableActivity> enableActivities){
 		
 		//首先获得不确定性选择集
 		List<OptEnableActivity> optEnableActivities = getOptEnableActivity(enableActivities);
 		
 		//返回的同步划分集
 		List<SynEnableActivity> synEnableActivities = new ArrayList<SynEnableActivity>();
 		
 		//开始时将其全部赋给synEnableActivity
 		SynEnableActivity synEnableActivity = new SynEnableActivity();
 		synEnableActivity.setEnableActivities(enableActivities);
 		
 		//队列以及临时队列
 		Queue<SynEnableActivity> queue = new LinkedList<>();
 		Queue<SynEnableActivity> tempQueue = new LinkedList<>();
 		tempQueue.add(synEnableActivity);
 		
 		//对每一个选择集进行处理
         for (OptEnableActivity optEnableActivity : optEnableActivities) {
         	
         	List<EnableActivity> OptBlockElems =  optEnableActivity.getEnableActivities();
         	
         	//1. 如果选择集长度为1,则表示不需要划分,跳过
         	if (OptBlockElems.size() == 1) {
 				continue;
 			}
         	
         	//2. 否则首先获得选择块所在的业务过程名(以index标识)
         	int optBizProName = OptBlockElems.get(0).getIndex();
         	
         	//首先将临时队列中的所有元素赋给queue,然后将其清空以保证每次都是一次划分
         	queue.addAll(tempQueue);
         	tempQueue.clear();
         	
         	//表示是非确定性选择
 			while(queue.size() > 0){
 				
 				SynEnableActivity synBlock = queue.poll(); 
 				List<EnableActivity> synBlockElems = synBlock.getEnableActivities();
 				
 				List<EnableActivity> enableActivitiesIsNotInOpt = new ArrayList<EnableActivity>();
 				
 				//获取所有不在选择块中的剩余元素,即与选择块不在一个组织即可
                 for (EnableActivity synElem : synBlockElems) {
 					int bizProName1 = synElem.getIndex();
 					if (bizProName1 == optBizProName) {
 						continue;
 					}
 					enableActivitiesIsNotInOpt.add(synElem);
 				}
                 
                 //剩余元素+选择块的一个元素为新产生的同步块
                 for (EnableActivity optElem : OptBlockElems) {
                 	List<EnableActivity> SynBlockElemsPar = new ArrayList<EnableActivity>();
                 	SynBlockElemsPar.add(optElem);
                 	SynBlockElemsPar.addAll(enableActivitiesIsNotInOpt);
                 	SynEnableActivity SynBlockPar = new SynEnableActivity();
                 	SynBlockPar.setEnableActivities(SynBlockElemsPar);
                 	tempQueue.offer(SynBlockPar);
                 }
 			}
 		}
         while(tempQueue.size() > 0){
         	SynEnableActivity SynBlockPar = tempQueue.poll();
         	synEnableActivities.add(SynBlockPar);
 		}
 		return synEnableActivities;
 	}
 	
 	//获得所有不确定选择集合的划分
 	//对于两个不同的使能变迁,如果它们属于同一个组织(即他们在协同中的位置相同),则为选择关系.
 	//eg CTS=({<org1,c>,<org1,c>},{<org2,c>},{<org3,c>}).
     public List<OptEnableActivity> getOptEnableActivity(List<EnableActivity> enableActivities){
         
     	List<OptEnableActivity> optEnableActivities = new ArrayList<OptEnableActivity>();
     	
     	//设置访问队列,如果已经访问过,则跳过
     	List<Integer> visitedBizPros = new ArrayList<Integer>();
     	
     	for (EnableActivity enableActivity1 : enableActivities) {
 			int bizProIndex1 = enableActivity1.getIndex();
 			//如果业务过程已经处理过,则跳过
 			if (visitedBizPros.contains(bizProIndex1)) {
 				continue;
 			}
 			OptEnableActivity optEnableActivity = new OptEnableActivity();
     		for (EnableActivity enableActivity2 : enableActivities) {
     			int bizProIndex2 = enableActivity2.getIndex();
 				//表示enableActivity1和enableActivity2属于一个组织,即他们在协同中的位置相同
 				if (bizProIndex1 == bizProIndex2) {
 					optEnableActivity.setEnableActivities(enableActivity2);
 				}
     		}
     		visitedBizPros.add(bizProIndex1);
 			optEnableActivities.add(optEnableActivity);
 		}
 		return optEnableActivities;
 	}

}
