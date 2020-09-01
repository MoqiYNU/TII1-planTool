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
	
	

	//产生expandStateFrom的后续展开迁移
	public List<ExpandTransition> getSucceedExpandTrans(List<LTS> bizPros, ExpandState expandStateFrom) {
		
       List<ExpandTransition> expandTransitions = new ArrayList<ExpandTransition>();
		
		//获得当前所有的使能活动
		List<EnableActivity> enableActivities = new ArrayList<EnableActivity>();
		List<String> statesFrom = expandStateFrom.getStates();
		for (int i = 0; i < statesFrom.size(); i++) {
		    LTS tempBizPro = bizPros.get(i);
			//以位置标识使能活动,避免P = P1||P1出错
			String curState = statesFrom.get(i);
		    List<EnableActivity> tempEnableActivities = semanticUtils.getEnableActivities(i, 
		    		  curState, tempBizPro);
		    enableActivities.addAll(tempEnableActivities);
		  }
		
	    ////针对每个其他使能活动(即本地或同步活动),进行如下处理:
	    //第1步:获得所有使能活动中的名字
	    List<String> names = new ArrayList<String>();
        for (EnableActivity enableActivity : enableActivities) {
		      String name = enableActivity.getActivity();
			  //如果已经记录(同步名字),则跳过
			  if (names.contains(name)) {
				  continue;
			  }
			  names.add(name);
	      }
        
          //第2步:获得按名字划分使能活动集
		  List<EnableActivityParByName> enableActivityParByNames = new ArrayList<EnableActivityParByName>();
		  for (String name : names) {
		      EnableActivityParByName enableActivityParByName = new EnableActivityParByName();
		      //设置名字
		      enableActivityParByName.setName(name);
		      List<EnableActivity> tempEnableActivities = new ArrayList<EnableActivity>();
		      for (EnableActivity enableActivity : enableActivities) {
		          String taskName = enableActivity.getActivity();
		      	  if (name.equals(taskName)) {
				      tempEnableActivities.add(enableActivity);
				  }
		      	}
		      	//设置使能活动集
		      	enableActivityParByName.setEnableActivities(tempEnableActivities);
		      	enableActivityParByNames.add(enableActivityParByName);
			}
		  
		    //迁移每个活动
	        for (EnableActivityParByName enableActivityParByName : enableActivityParByNames) {
	            
	        	String name = enableActivityParByName.getName();
        	    List<EnableActivity> tempEnableActivities = enableActivityParByName.getEnableActivities();
        	
        	    //如果是自由名字,则异步迁移;否则同步迁移	
				List<Integer> synBizPros = semanticUtils.getSynchronSet(bizPros, name);
				//异步迁移
				if (synBizPros.size() == 1) {
					
					EnableActivity enableActivity = enableActivityParByName.getEnableActivities().get(0);
					
					//获得自由活动的位置,活动以及达到状态
	            	int updateIndex = enableActivity.getIndex();
	            	String activity = enableActivity.getActivity();
	            	String stateTo = enableActivity.getStateTo();
	            	
					//生成新的组合状态集合
	            	List<String> statesTo = new ArrayList<String>();
	            	statesTo.addAll(statesFrom);
	            	statesTo.set(updateIndex, stateTo);
	            	
	            	
	            	//生成expandStateTo
        		    ExpandState expandStateTo = new ExpandState();
					expandStateTo.setStates(statesTo);
					
					//生成此次迁移的展开变迁
					ExpandTransition expandTransition = new ExpandTransition();
					expandTransition.setExpandStateFrom(expandStateFrom);
					expandTransition.setLabel(activity);
					expandTransition.setExpandStateTo(expandStateTo);
					expandTransitions.add(expandTransition);
	            	
				} else {//同步迁移
					
					//获得同步块集合
					List<SynEnableActivity> synEnableActivities = semanticUtils.getSynEnableActivityInOpt(tempEnableActivities);
					for (SynEnableActivity synBlock : synEnableActivities) {
						//判断是否能够同步
						boolean isSyn = semanticUtils.isSynchron(synBizPros, synBlock);
						if (isSyn) {//如果能够同步
							
							//复制stateFrom到stateTo
							List<String> statesTo = new ArrayList<String>();
			            	statesTo.addAll(statesFrom);
			            	
			            	//依次更新同步状态
							List<EnableActivity> synBlockElems = synBlock.getEnableActivities();
							for (EnableActivity synBlockElem : synBlockElems) {
								int updateIndex = synBlockElem.getIndex();
				            	String stateTo = synBlockElem.getStateTo();
				            	statesTo.set(updateIndex, stateTo);
							}
							
			            	
			            	//生成expandStateTo
		            		ExpandState expandStateTo = new ExpandState();
							expandStateTo.setStates(statesTo);
							
							//生成此次迁移的展开变迁
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
