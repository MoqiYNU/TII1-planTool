package toolkits.utils.lts;

import java.util.ArrayList;
import java.util.List;

import toolkits.def.lts.LTS;


public class CompLTS {
	
	private SemanticUtils semanticUtils;
	private CompState initCompState;
	private List<CompState> endCompStates;
	private List<CompState> compStates;
	private List<CompTransition> compTransitions;
	
	public CompLTS() {
		semanticUtils = new SemanticUtils();
	}
	
	public CompState getInitCompState() {
		return initCompState;
	}

	public void setInitCompState(CompState initCompState) {
		this.initCompState = initCompState;
	}
	
	public List<CompState> getEndCompStates() {
		return endCompStates;
	}

	public void setEndCompStates(List<CompState> endCompStates) {
		this.endCompStates = endCompStates;
	}

	public List<CompState> getCompStates() {
		return compStates;
	}

	public void setCompStates(List<CompState> compStates) {
		this.compStates = compStates;
	}

	public List<CompTransition> getCompTransitions() {
		return compTransitions;
	}

	public void setCompTransitions(List<CompTransition> compTransitions) {
		this.compTransitions = compTransitions;
	}
	
	

	//����expandStateFrom�ĺ���չ��Ǩ��
	public List<ExpandTransition> getSucceedExpandTrans(List<LTS> bizPros, ExpandState expandStateFrom) {
		
       List<ExpandTransition> expandTransitions = new ArrayList<ExpandTransition>();
		
		//��õ�ǰ���е�ʹ�ܻ
		List<EnableActivity> enableActivities = new ArrayList<EnableActivity>();
		List<String> statesFrom = expandStateFrom.getStates();
		for (int i = 0; i < statesFrom.size(); i++) {
		    LTS tempBizPro = bizPros.get(i);
			//��λ�ñ�ʶʹ�ܻ,����P = P1||P1����
			String curState = statesFrom.get(i);
		    List<EnableActivity> tempEnableActivities = semanticUtils.getEnableActivities(i, 
		    		  curState, tempBizPro);
		    enableActivities.addAll(tempEnableActivities);
		  }
		
	    ////���ÿ������ʹ�ܻ(�����ػ�ͬ���),�������´���:
	    //��1��:�������ʹ�ܻ�е�����
	    List<String> names = new ArrayList<String>();
        for (EnableActivity enableActivity : enableActivities) {
		      String name = enableActivity.getActivity();
			  //����Ѿ���¼(ͬ������),������
			  if (names.contains(name)) {
				  continue;
			  }
			  names.add(name);
	      }
        
          //��2��:��ð����ֻ���ʹ�ܻ��
		  List<EnableActivityParByName> enableActivityParByNames = new ArrayList<EnableActivityParByName>();
		  for (String name : names) {
		      EnableActivityParByName enableActivityParByName = new EnableActivityParByName();
		      //��������
		      enableActivityParByName.setName(name);
		      List<EnableActivity> tempEnableActivities = new ArrayList<EnableActivity>();
		      for (EnableActivity enableActivity : enableActivities) {
		          String taskName = enableActivity.getActivity();
		      	  if (name.equals(taskName)) {
				      tempEnableActivities.add(enableActivity);
				  }
		      	}
		      	//����ʹ�ܻ��
		      	enableActivityParByName.setEnableActivities(tempEnableActivities);
		      	enableActivityParByNames.add(enableActivityParByName);
			}
		  
		    //Ǩ��ÿ���
	        for (EnableActivityParByName enableActivityParByName : enableActivityParByNames) {
	            
	        	String name = enableActivityParByName.getName();
        	    List<EnableActivity> tempEnableActivities = enableActivityParByName.getEnableActivities();
        	
        	    //�������������,���첽Ǩ��;����ͬ��Ǩ��	
				List<Integer> synBizPros = semanticUtils.getSynchronSet(bizPros, name);
				//�첽Ǩ��
				if (synBizPros.size() == 1) {
					
					EnableActivity enableActivity = enableActivityParByName.getEnableActivities().get(0);
					
					//������ɻ��λ��,��Լ��ﵽ״̬
	            	int updateIndex = enableActivity.getIndex();
	            	String activity = enableActivity.getActivity();
	            	String stateTo = enableActivity.getStateTo();
	            	
					//�����µ����״̬����
	            	List<String> statesTo = new ArrayList<String>();
	            	statesTo.addAll(statesFrom);
	            	statesTo.set(updateIndex, stateTo);
	            	
	            	
	            	//����expandStateTo
        		    ExpandState expandStateTo = new ExpandState();
					expandStateTo.setStates(statesTo);
					
					//���ɴ˴�Ǩ�Ƶ�չ����Ǩ
					ExpandTransition expandTransition = new ExpandTransition();
					expandTransition.setExpandStateFrom(expandStateFrom);
					expandTransition.setLabel(activity);
					expandTransition.setExpandStateTo(expandStateTo);
					expandTransitions.add(expandTransition);
	            	
				} else {//ͬ��Ǩ��
					
					//���ͬ���鼯��
					List<SynEnableActivity> synEnableActivities = semanticUtils.getSynEnableActivityInOpt(tempEnableActivities);
					for (SynEnableActivity synBlock : synEnableActivities) {
						//�ж��Ƿ��ܹ�ͬ��
						boolean isSyn = semanticUtils.isSynchron(synBizPros, synBlock);
						if (isSyn) {//����ܹ�ͬ��
							
							//����stateFrom��stateTo
							List<String> statesTo = new ArrayList<String>();
			            	statesTo.addAll(statesFrom);
			            	
			            	//���θ���ͬ��״̬
							List<EnableActivity> synBlockElems = synBlock.getEnableActivities();
							for (EnableActivity synBlockElem : synBlockElems) {
								int updateIndex = synBlockElem.getIndex();
				            	String stateTo = synBlockElem.getStateTo();
				            	statesTo.set(updateIndex, stateTo);
							}
							
			            	
			            	//����expandStateTo
		            		ExpandState expandStateTo = new ExpandState();
							expandStateTo.setStates(statesTo);
							
							//���ɴ˴�Ǩ�Ƶ�չ����Ǩ
							ExpandTransition expandTransition = new ExpandTransition();
							expandTransition.setExpandStateFrom(expandStateFrom);
							expandTransition.setLabel(synBlockElems.get(0).getActivity());
							expandTransition.setExpandStateTo(expandStateTo);
							expandTransitions.add(expandTransition);
							
						}
					}
				}
	        }
	        return expandTransitions;
	}
	
}
