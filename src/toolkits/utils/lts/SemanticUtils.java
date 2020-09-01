package toolkits.utils.lts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;

public class SemanticUtils {
	
	//��ȡ��i��ҵ�������״̬curState��ʹ�ܻ����
    public List<EnableActivity> getEnableActivities(int index, String curState, LTS bizPro){
        
    	//����ʹ�ܻ����
    	List<EnableActivity> enableActivities = new ArrayList<EnableActivity>();
    	
    	List<LTSTran> transitions = bizPro.getLTSTrans();
    	for (LTSTran transition : transitions) {
			String stateFrom = transition.getFrom();
			String label = transition.getTran();
			String stateTo = transition.getTo();
			if (curState.equals(stateFrom)) {
				//����ʹ�ܻ����
				EnableActivity enableActivity = new EnableActivity();
				//ʹ�ܻ��λ�ñ�ʶ
				enableActivity.setIndex(index);
				enableActivity.setActivity(label);
				enableActivity.setStateTo(stateTo);
				//�����ʹ�ܻ����
				enableActivities.add(enableActivity);
			}
		}
    	return enableActivities;
    }
    
    //�ж�һ��ҵ������Ƿ���Ҫ������nameͬ��
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
    
    //���������Ҫ������name��ͬ����ҵ����̼���
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
  	
    //ȷ���Ƿ��ܹ�ͬ��
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
  	
    // ��ò���Ǩ�ƻ��ּ�.
 	// eg CTS=({P1.c1,P1.c2},{P2.c1,P2.c2});
 	// ��  PTS=({P1.c1,P2.c1},{P1.c1,P2.c2},{P1.c2,P2.c1},{P1.c2,P2.c2}).
 	public List<SynEnableActivity> getSynEnableActivityInOpt(List<EnableActivity> enableActivities){
 		
 		//���Ȼ�ò�ȷ����ѡ��
 		List<OptEnableActivity> optEnableActivities = getOptEnableActivity(enableActivities);
 		
 		//���ص�ͬ�����ּ�
 		List<SynEnableActivity> synEnableActivities = new ArrayList<SynEnableActivity>();
 		
 		//��ʼʱ����ȫ������synEnableActivity
 		SynEnableActivity synEnableActivity = new SynEnableActivity();
 		synEnableActivity.setEnableActivities(enableActivities);
 		
 		//�����Լ���ʱ����
 		Queue<SynEnableActivity> queue = new LinkedList<>();
 		Queue<SynEnableActivity> tempQueue = new LinkedList<>();
 		tempQueue.add(synEnableActivity);
 		
 		//��ÿһ��ѡ�񼯽��д���
         for (OptEnableActivity optEnableActivity : optEnableActivities) {
         	
         	List<EnableActivity> OptBlockElems =  optEnableActivity.getEnableActivities();
         	
         	//1. ���ѡ�񼯳���Ϊ1,���ʾ����Ҫ����,����
         	if (OptBlockElems.size() == 1) {
 				continue;
 			}
         	
         	//2. �������Ȼ��ѡ������ڵ�ҵ�������(��index��ʶ)
         	int optBizProName = OptBlockElems.get(0).getIndex();
         	
         	//���Ƚ���ʱ�����е�����Ԫ�ظ���queue,Ȼ��������Ա�֤ÿ�ζ���һ�λ���
         	queue.addAll(tempQueue);
         	tempQueue.clear();
         	
         	//��ʾ�Ƿ�ȷ����ѡ��
 			while(queue.size() > 0){
 				
 				SynEnableActivity synBlock = queue.poll(); 
 				List<EnableActivity> synBlockElems = synBlock.getEnableActivities();
 				
 				List<EnableActivity> enableActivitiesIsNotInOpt = new ArrayList<EnableActivity>();
 				
 				//��ȡ���в���ѡ����е�ʣ��Ԫ��,����ѡ��鲻��һ����֯����
                 for (EnableActivity synElem : synBlockElems) {
 					int bizProName1 = synElem.getIndex();
 					if (bizProName1 == optBizProName) {
 						continue;
 					}
 					enableActivitiesIsNotInOpt.add(synElem);
 				}
                 
                 //ʣ��Ԫ��+ѡ����һ��Ԫ��Ϊ�²�����ͬ����
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
 	
 	//������в�ȷ��ѡ�񼯺ϵĻ���
 	//����������ͬ��ʹ�ܱ�Ǩ,�����������ͬһ����֯(��������Эͬ�е�λ����ͬ),��Ϊѡ���ϵ.
 	//eg CTS=({<org1,c>,<org1,c>},{<org2,c>},{<org3,c>}).
     public List<OptEnableActivity> getOptEnableActivity(List<EnableActivity> enableActivities){
         
     	List<OptEnableActivity> optEnableActivities = new ArrayList<OptEnableActivity>();
     	
     	//���÷��ʶ���,����Ѿ����ʹ�,������
     	List<Integer> visitedBizPros = new ArrayList<Integer>();
     	
     	for (EnableActivity enableActivity1 : enableActivities) {
 			int bizProIndex1 = enableActivity1.getIndex();
 			//���ҵ������Ѿ������,������
 			if (visitedBizPros.contains(bizProIndex1)) {
 				continue;
 			}
 			OptEnableActivity optEnableActivity = new OptEnableActivity();
     		for (EnableActivity enableActivity2 : enableActivities) {
     			int bizProIndex2 = enableActivity2.getIndex();
 				//��ʾenableActivity1��enableActivity2����һ����֯,��������Эͬ�е�λ����ͬ
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
