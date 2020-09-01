package toolkits.utils.plan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;
import toolkits.def.petri.Composition;
import toolkits.def.petri.Flow;
import toolkits.def.petri.Marking;
import toolkits.def.petri.ProNet;
import toolkits.utils.block.Node;
import toolkits.utils.block.ProTree;
import toolkits.utils.block.ProTreeUtils;
import toolkits.utils.file.PrintUtils;
import toolkits.utils.petri.MarkingUtils;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * 基于执行计划的正确性迫使方法
 */
public class PlanUtils {
	
	private ProTreeUtils proTreeUtils;
	
	public PlanUtils() {
		proTreeUtils = new ProTreeUtils();
	}

	
	/******************************实现面向执行计划执行*******************************/
	
	public LTS execute(List<ProNet> proNets, List<CompFrag> plans) {
		
		System.out.println("begin execute................................" + "\n");
		
		// Step 1.定义产生LTS
		LTS lts = new LTS();
		List<String> ends = new ArrayList<String>();
		List<String> states = new ArrayList<String>();
		List<LTSTran> ltsTrans = new ArrayList<LTSTran>();
		
		// Step 2.构建组合过程
		Composition composition = new Composition();
		composition.setProNets(proNets);
		ProNet compNet = composition.compose();
		
		// Step 3.定义初始组合状态
		int index = 0;
		String initIdf = "S" + index;
		Marking initMarking = compNet.getSource();
		//构建每个业务过程配置初始包(Note:可用计划为全部计划,已执行活动为空集)
		int size = proNets.size();
		int planNum = plans.size();//确定全部计划
		List<Bag> initBags = new ArrayList<Bag>();
		for (int i = 0; i < size; i++) {
			Bag bag = new Bag();
			for (int j = 0; j < planNum; j++) {
				bag.addPlan(j);
				ProTree frag = plans.get(j).getFrags().get(i);
				//获取frag中所有叶子节点
				List<Node> nodes = frag.getNodes();
				System.out.println("print nodes: " + ", plan: " + j + ", pro: " + i);
				List<String> acts = new ArrayList<String>();
				for (Node node : nodes) {
					if (node.getType().equals("leaf")) {
						acts.add(node.getIdf());
					}
					System.out.println("node: " + node.getIdf());
				}
				//根节点是最后一个
				//Node root = nodes.get(nodes.size()-1);
				//List<String> acts = proTreeUtils.getLeafDesc(root);
				System.out.println("acts: " + acts);
				System.out.println("end...........................................");
				Map<String, List<String>> map = getPreOrdActsMap(frag, acts);
				bag.addMap(map);
			}
			
			initBags.add(bag);
		}
		CompState initCompState = new CompState();
		initCompState.setIdf(initIdf);
		initCompState.setMarking(initMarking);
		initCompState.setBags(initBags);
		
		states.add(initIdf);
		lts.getStateMarkingMap().put(initIdf, initMarking);
		
		//即将访问的队列visitingQueue和已经访问过队列visitedQueue
		Queue<CompState> visitingQueue = new LinkedList<>(); 
		List<CompState> visitedQueue = new ArrayList<>();
		//将初始标识入队并置为已经访问
		visitingQueue.offer(initCompState);
		visitedQueue.add(initCompState);
		
		//迭代计算
		while(visitingQueue.size() > 0){

		    //出对一个标识,以此进行迁移
		    CompState compStateFrom = visitingQueue.poll();
		    String idfFrom = compStateFrom.getIdf();
		    Marking markingFrom = compStateFrom.getMarking();
		    List<String> placesFrom = markingFrom.getPlaces();
		    List<Bag> bagsFrom = compStateFrom.getBags();
		    
		    //System.out.println("idfFrom: " + idfFrom);
		    
		    /*//打印已执行活动序列
		    System.out.println("idfFrom: " + idfFrom + "..............");
		    for (int i = 0; i < size; i++) {
		    	System.out.println("pro: " + i + ", execActs: " + bagsFrom.get(i).getExecActs());
		    }*/
		    
		    //所有使能活动集合
		    List<String> enableTrans = PetriUtils.getEnableTrans(compNet, placesFrom);
		    //System.out.println("enableTrans: " + enableTrans);
			for (String tran : enableTrans) {
				
				// 3.1获取tran对应业务过程标记
				int proNetIndex = PetriUtils.getProNetIndex(proNets, tran);
				
				// 3.2利用交集获取全局可用计划集
				List<Integer> globalAvailPlans = new ArrayList<Integer>();
				globalAvailPlans.addAll(bagsFrom.get(0).getAvailPlans());
				for (int i = 1; i < size; i++) {
					globalAvailPlans = (List<Integer>) CollectionUtils.intersection(globalAvailPlans, bagsFrom.get(i).getAvailPlans());
				}
				//System.out.println("tran: " + tran + ", global plans: " + globalAvailPlans);
				//若全局可用计划为空,则跳过
				if (globalAvailPlans.size() == 0) {
					continue;
				}
				
				// 3.3获取局部可用计划
				List<Integer> localAvailPlans = new ArrayList<>();
                for (Integer plan : globalAvailPlans) {
                	// 3.3.1判断proNetIndex标识的业务过程包中第plan个计划是否包含tran
                	List<String> preActs = bagsFrom.get(proNetIndex).getMaps().get(plan).get(tran);
                	//System.out.println("preActs: " + preActs);
                	if (preActs == null) {//未包含tran
						continue;
					}
                	//System.out.println("cover...");
                	// 3.3.2若包含tran,则判断其前序活动是否已经执行完成
                	List<String> execActs = bagsFrom.get(proNetIndex).getExecActs();
                	//System.out.println("proNetIndex: " + proNetIndex);
                	//System.out.println("test: " + execActs);
                	if (execActs.containsAll(preActs)) {
						localAvailPlans.add(plan);
					}
				}
                //System.out.println("localAvailPlans: " + localAvailPlans);
                //若局部计划为空,则跳过
                if (localAvailPlans.size() == 0) {
					continue;
				}
                
                //定义到达组合状态
                List<String> placesTo = PetriUtils.getPlacesTo(compNet, placesFrom, tran);
				Marking markingTo = new Marking();
				markingTo.setPlaces(placesTo);
                List<Bag> bagsTo = new ArrayList<Bag>();
                for (int i = 0; i < bagsFrom.size(); i++) {
    				if (i != proNetIndex) {
						bagsTo.add(bagsFrom.get(i));
					}else {
						Bag bag = new Bag();
						List<String> execActsTo = new ArrayList<String>();
						execActsTo.addAll(bagsFrom.get(i).getExecActs());
						execActsTo.add(tran);
						bag.setExecActs(execActsTo);
						bag.setAvailPlans(localAvailPlans);
						bag.setMaps(bagsFrom.get(i).getMaps());
						bagsTo.add(bag);
					}
    			}
                //System.out.println("execActsTo: " + bagsTo.get(proNetIndex).getExecActs());
                
                CompState compState = getVisitedCompState(markingTo, bagsTo, visitedQueue);
                if (compState == null) {//组合状态未访问过
                	CompState compStateTo = new CompState();
    				index ++;
    	            String idfTo = "S" + index;
    	            compStateTo.setIdf(idfTo);
    				compStateTo.setMarking(markingTo);
    				compStateTo.setBags(bagsTo);
    				
    				states.add(idfTo);
    				LTSTran ltsTran = new LTSTran();
    				ltsTran.setFrom(idfFrom);
    				ltsTran.setTran(PetriUtils.getLabel(compNet.getTranLabelMap(), tran));
    				ltsTran.setTo(idfTo);
    				System.out.println("from: " + ltsTran.getFrom() + ", tran: " + ltsTran.getTran() + ", to: " + ltsTran.getTo());
    				ltsTrans.add(ltsTran);
    				if (MarkingUtils.markingIsExist(compNet.getSinks(), markingTo)) {
    					ends.add(idfTo);
    				}
    				lts.getStateMarkingMap().put(idfTo, markingTo);
    				
    				visitingQueue.offer(compStateTo);
    				visitedQueue.add(compStateTo);
    				
				}else {//组合状态生成过
					LTSTran ltsTran = new LTSTran();
    				ltsTran.setFrom(idfFrom);
    				ltsTran.setTran(PetriUtils.getLabel(compNet.getTranLabelMap(), tran));
    				ltsTran.setTo(compState.getIdf());
    				System.out.println("from: " + ltsTran.getFrom() + ", tran: " + ltsTran.getTran() + ", to: " + ltsTran.getTo());
    				ltsTrans.add(ltsTran);
				}
                
			}
	    }
	    
	    lts.setStart(initIdf);
	    lts.setStates(states);
	    lts.setLTSTrans(ltsTrans);
	    lts.setEnds(ends);
	    return lts;
		
	}
	
	//获取访问过组合状态
	public CompState getVisitedCompState(Marking markingTo, List<Bag> bagsTo, List<CompState> visitedQueue) {
		for (CompState compState : visitedQueue) {
			Marking tempMarking = compState.getMarking();
			if (MarkingUtils.twoMarkingsIsEqual(tempMarking, markingTo)
					&& twoBagsIsEqual(compState.getBags(), bagsTo)) {
				return compState;
			}
		}
		return null;
		
	}
	
	//判断两个包是否相同
	public boolean twoBagsIsEqual(List<Bag> bags1, List<Bag> bags2) {
		int size1 = bags1.size();
		int size2 = bags2.size();
		if (size1 != size2) {
			return false;
		}else {
			for (int i = 0; i < size1; i++) {
				List<Integer> plans1 = bags1.get(i).getAvailPlans();
				List<Integer> plans2 = bags2.get(i).getAvailPlans();
				List<String> execActs1 = bags1.get(i).getExecActs();
				List<String> execActs2 = bags2.get(i).getExecActs();
				if (CollectionUtils.isEqualCollection(plans1, plans2) &&
						//Note:只需要关注执行活动集,而执行活动间顺序无须考虑
						CollectionUtils.isEqualCollection(execActs1, execActs2)) {
					continue; 
				}else {
					return false;
				}
			}
		}
		return true;
		
	}
	
	//判断两个执行序列是否相同
	public boolean twoExecActsIsEqual(List<String> execActs1, List<String> execActs2) {
		int size1 = execActs1.size();
		int size2 = execActs2.size();
		if (size1 != size2) {
			return false;
		}else {
			for (int i = 0; i < size1; i++) {
				String elem1 = execActs1.get(i);
				String elem2 = execActs2.get(i);
				if (!elem1.equals(elem2)) {
					return false;
				}
			}
		}
		return true;
		
	}
	
	/******************************实现面向执行路径的迫使 *******************************/
	
	// Step4.产生计划,以次序矩阵标识--------------------------------------------------
	
	public List<CompFrag> enforce(List<ProNet> proNets) throws Exception {
		
		// 1.生成每个语义兼容组合片段,即计划
		List<CompFrag> semtCompFrags = getSemtCompaCompFrags(proNets);
		// 2.打印组合片段..............
		PrintUtils.printCompFrag(semtCompFrags, proNets);
		
		return semtCompFrags;
		
	}
	
	//产生片段对应次序矩阵
	public ORDMatrix getORDMatrixFromFrag(ProTree frag) {
		
		// 1.获取根节点及其所有叶子子孙节点
		List<Node> nodes = frag.getNodes();
		//根节点是最后一个
		Node root = nodes.get(nodes.size()-1);
		List<String> acts = proTreeUtils.getLeafDesc(root);
		
		// 2.创建frag对应图graph(邻接矩阵存储),并初始化graph
        int vNum = acts.size();//节点数量
		int[][] graph = new int[vNum][vNum];
		for(int i = 0; i < vNum; i++){//初始化graph为0
			for(int j = 0; j < vNum; j++){
				graph[i][j] = 0;
			}
		}
		
		// 3.基于前序关系重置矩阵
		Map<String, List<String>> map = getPreOrdActsMap(frag, acts);
		for(Map.Entry<String, List<String>> entry : map.entrySet()){
			String act = entry.getKey();
			int index1 = getTranIndex(act, acts); 
		    List<String> preOrdActs = entry.getValue();
		    for (String preOrdAct : preOrdActs) {
				int index2 = getTranIndex(preOrdAct, acts);
				graph[index1][index2] = 1;
			}
		}
		
		ORDMatrix ordMatrix = new ORDMatrix();
		ordMatrix.setActs(acts);
		ordMatrix.setGraph(graph);
		return ordMatrix;
		
	}
	
	
	// Step3.产生语义兼容片段组合集--------------------------------------------------
	
	public List<CompFrag> getSemtCompaCompFrags(List<ProNet> proNets) throws Exception {
		
		List<CompFrag> syncCompaFrags = getSyntCompaCompFrags(proNets);
		List<CompFrag> semtCompaFrags = new ArrayList<CompFrag>();
		for (CompFrag compFrag : syncCompaFrags) {
			int[][] interGraph = creatInterGraph(compFrag, proNets);
			if (isDAG(interGraph)) {
				semtCompaFrags.add(compFrag);
			}
		}
		return semtCompaFrags;
		
	}
	
	//判断graph是否为有向无环图
	public boolean isDAG(int[][] graph) {
		for(int i = 0; i < graph.length; i++){
			//针对节点i进行DFS遍历
			boolean isDAG = detCycleByDFS(i, graph);
			if (!isDAG) {
				System.out.println("some cycles exist!" + "\n");
				return false;
			}
		}
		System.out.println("no cycles exist!" + "\n");
		return true;
		
	}
	
	//DFS遍历graph,判断从节点i出发其是否存在环
	private boolean detCycleByDFS(int i, int[][] graph) {
		
		//定义当前访问路径和状态访问队列
		List<Integer> route = new ArrayList<>();
		route.add(i);
		List<Integer> visitedQueue = new ArrayList<>();
		
		do {
			//取最后元素,即当前状态
			int size = route.size();
			int curState = route.get(size-1);
			
			//获取curState后继状态集
			List<Integer> succStates = getSuccStates(curState, graph);
			int firstNotVisitedState = getFirstNotVisited(visitedQueue, succStates);
			
			// 1.若存在环,则置isDAG为false
			if (CollectionUtils.intersection(succStates, route).size() != 0) {
				return false;
			}
			
			// 2.若curState不能迁移,则回溯当前路径并置其已访问
			if (succStates.size() == 0) {
				route.remove(size-1);//回溯当前路径
				visitedQueue.add(curState);//置curState已访问
			}else {
				// 3.孩子展开状态均已访问,则回溯并置其为已访问
			    if (firstNotVisitedState == -1) {
			    	route.remove(size-1);//回溯路径
					visitedQueue.add(curState);//置curState已访问
				}else {// 4.否则访问firstNotVisitedState
					route.add(firstNotVisitedState);
				}
			}
		} while (route.size() > 0);
		
		return true;
		
	}
	
	//获得未被访问的第一个状态.若所有状态均被访问,则返回-1
	private int getFirstNotVisited(List<Integer> visitedQueue, List<Integer> succeedStates) {
		for (Integer succeedState : succeedStates) {
			if (!visitedQueue.contains(succeedState)) {
				return succeedState;
			}
		}
		return -1;
	}

	//获取curState后继状态集
	private List<Integer> getSuccStates(int curState, int[][] graph) {
		List<Integer> succeedStates = new ArrayList<>();
		for(int j = 0; j < graph.length; j++){
			if(graph[curState][j] != 0){	
				succeedStates.add(j);
			}
		}
		return succeedStates;
	}
	
	// Step2.创建交互图--------------------------------------------------------
	
	public int[][] creatInterGraph(CompFrag compFrag, List<ProNet> proNets) {
		
		// 1.获取compFrag中交互活动集
		List<String> interActsInCompFrag = new ArrayList<String>();
		int size = proNets.size();
		for (int i = 0; i < size; i++) {
			ProTree frag = compFrag.getFrags().get(i);
			ProNet proNet = proNets.get(i);
			interActsInCompFrag.addAll(getInterActsInFrag(frag, proNet));
		}
		
		// 2.创建compFrag对应图graph(邻接矩阵存储),并初始化graph
        int vNum = interActsInCompFrag.size();//节点数量
		int[][] graph = new int[vNum][vNum];
		for(int i = 0; i < vNum; i++){//初始化graph为0
			for(int j = 0; j < vNum; j++){
				graph[i][j] = 0;
			}
		}
		
		// 3.设置每个片段中交互活动间次序关系
		for (int j = 0; j < size; j++) {
			ProTree frag = compFrag.getFrags().get(j);
			ProNet proNet = proNets.get(j);
			//第j个片段中的交互活动集
			List<String> interActsJ = getInterActsInFrag(frag, proNet);
			Map<String, List<String>> map = getPreOrdActsMap(frag, interActsJ);
			for(Map.Entry<String, List<String>> entry : map.entrySet()){
				String act = entry.getKey();
				int index1 = getTranIndex(act, interActsInCompFrag); 
			    List<String> preActs = entry.getValue();
			    for (String preAct : preActs) {
					int index2 = getTranIndex(preAct, interActsInCompFrag);
					graph[index1][index2] = 1;
				}
			}
		}
		
		// 4.设置每个片段中交互活动间消息依赖关系
		
		//Pre1.组合过程网
        Composition comp = new Composition();
        comp.setProNets(proNets);
        ProNet compNet = comp.compose();
        
        for (String act : interActsInCompFrag) {//打印交互活动集
        	System.out.println("interActsInCompFrag: " + compNet.getTranLabelMap().get(act));
		}
        
        // Pre2.获取消息库所包
        List<String> msgPlaces = compNet.getMsgPlaces();
        List<MsgPlaceBag> msgPlaceBags = comp.getMsgPlaceBags(msgPlaces, compNet.getFlows());
		
        // Pre3.确定消息库所关联的边
		for (MsgPlaceBag bag : msgPlaceBags) {
			//判断消息包bag是否在当前组合片段中
			List<String> preSet = (List<String>) CollectionUtils.intersection(bag.getPreSet(), interActsInCompFrag);
			List<String> postSet = (List<String>) CollectionUtils.intersection(bag.getPostSet(), interActsInCompFrag);
			if (preSet.size() != 0) {//若bag与该组合片段中变迁关联(由可组合性,每个消息库所在片段组合中只关联一个发送和接收消息的变迁)
				int index1 = getTranIndex(preSet.get(0), interActsInCompFrag);
				int index2 = getTranIndex(postSet.get(0), interActsInCompFrag);
				graph[index2][index1] = 1;
			}
		 }
		return graph;
		
	}
	
	//获取tran的位置标记
	public int getTranIndex(String tran, List<String> trans) {
		
		int index = 0;
		for (String tempTran : trans) {
			if (tempTran.equals(tran)) {
				return index;
			}
			index ++;
		}
		return -1;
		
	}
	
	//获取前序活动映射(由活动集acts限制)
	public Map<String, List<String>> getPreOrdActsMap(ProTree frag, List<String> acts) {
		
		// 1.初始化映射
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        for (String act : acts) {
			map.put(act, new ArrayList<String>());
		}
        // 2.根据每个顺序节点进行计算
        List<Node> SEQNodes = proTreeUtils.getSEQNode(frag);
        if (SEQNodes.size() == 0) {
			return map;
		}else {
			for (Node node : SEQNodes) {
				List<Node> chaNodes = node.getChaNodes();
				//Note:从第2个孩子节点进行计算,即(i=1)
				for (int i = 1; i < chaNodes.size(); i++) {
					//获取当前节点chaNodes.get(i)的子孙叶子节点中包含在acts中的活动
					List<String> curActs = (List<String>) CollectionUtils.
							intersection(proTreeUtils.getLeafDesc(chaNodes.get(i)), acts);
					//(0,i-1)节点集中含有的叶子节点集(受限活动集acts)
					for (String curAct : curActs) {
						map.put(curAct, getPreOrdActs(chaNodes, i, acts));
					}
				}
			}
		}
        return map;
		
	}
	
	//获取第index个孩子节点之前的叶子节点集(受限活动集acts)
	public List<String> getPreOrdActs(List<Node> chaNodes, int index, List<String> acts) {
		
		List<String> preActs = new ArrayList<String>();
		for (int i = 0; i < index; i++) {
			List<String> tempActs = proTreeUtils.getLeafDesc(chaNodes.get(i));
			preActs.addAll(CollectionUtils.intersection(tempActs, acts));
		}
		return preActs;
		
	}
	
	// Step1.产生语法兼容片段组合集(关键)----------------------------------------------------------
	
	public List<CompFrag> getSyntCompaCompFrags(List<ProNet> proNets) throws Exception {
		
		// 1.获取由每个过程网中的片段组成集合
		List<List<ProTree>> fragSets = new ArrayList<List<ProTree>>();
		int size = proNets.size();
		for (int i = 0; i < size; i++) {
			ProNet proNet = proNets.get(i);
			ProTree proTree = proTreeUtils.genProTreeFromProNet(proNet, i);
			List<ProTree> fragments = proTreeUtils.genFragments(proTree);
			/*for (int j = 0; j < fragments.size(); j++) {
				DotUtils dotUtils = new DotUtils();
				String name = "fg" + i + j;
				dotUtils.pt2Dot(fragments.get(j), proNets.get(j).getTranLabelMap(), name);
			}*/
			fragSets.add(fragments);
		}
		
		// 2.利用笛卡尔积计算语法兼容组合片段
		List<CompFrag> compFrags = descFrags(fragSets, proNets);
		//PrintUtils.printCompFrag(compFrags, proNets);//打印组合片段集
		return compFrags;
		
	}
	
	//利用笛卡尔积计算语法兼容组合片段
	public List<CompFrag> descFrags(List<List<ProTree>> fragSets, List<ProNet> proNets) {
		
		int size = fragSets.size();
		if (size == 1) {//1.只有一个集合,无须迫使
			System.out.println("There is no need to enforce...");
			return null;
		}else {//2.至少有两个集合
			List<CompFrag> set = composeFirstTwoFragSet(fragSets.get(0), fragSets.get(1), proNets);
			for (int i = 2; i < size; i++) {
				set = composeSuccOneFragSet(set, fragSets.get(i), i, proNets);
			}
			//Note:避免组合网中存在发送/接收消息的库所,但无接收/发送该消息的库所与之对应,即要求组合片段是封闭的
			List<CompFrag> finalCompFrags = new ArrayList<CompFrag>();
            for (CompFrag compFrag : set) {
				if (compFrag.getInputMsgs().size() == 0 
						&& compFrag.getOutputMsgs().size() == 0) {
					finalCompFrags.add(compFrag);
				}
			}
			return finalCompFrags;
		}
		
	}
	
	//计算前面两个片段集合的笛卡尔积
	public List<CompFrag> composeFirstTwoFragSet(List<ProTree> list1, List<ProTree> list2, List<ProNet> proNets) {
		
		 List<CompFrag> result = new ArrayList<CompFrag>();
		 
		 for (int i = 0; i < list1.size(); i++) {
			 
			 ProTree frag1 = list1.get(i);
			 List<String> inputMsgs1 = getInputMsgs(frag1, proNets.get(0)); 
			 List<String> outputMsgs1 = getOutputMsgs(frag1, proNets.get(0)); 
			 
	         for (int j = 0; j < list2.size(); j++) {
	        	 
	        	 ProTree frag2 = list2.get(j);
	        	 List<String> inputMsgs2 = getInputMsgs(frag2, proNets.get(1)); 
				 List<String> outputMsgs2 = getOutputMsgs(frag2, proNets.get(1)); 
				 
				 // 1.获取I1|_|I2和O1|_|O2(Note:可以容许重复)
				 List<String> inputMsgsUnion = new ArrayList<String>();
				 inputMsgsUnion.addAll(inputMsgs1);
				 inputMsgsUnion.addAll(inputMsgs2);
				 List<String> outputMsgsUnion = new ArrayList<String>();
				 outputMsgsUnion.addAll(outputMsgs1);
				 outputMsgsUnion.addAll(outputMsgs2);
				 
				 // 2.获取(I1|_|I2\O1|_|O2)和(O1|_|O2\I1|_|I2)
				 List<String> compInputMsgs = (List<String>) CollectionUtils.subtract(inputMsgsUnion, outputMsgsUnion);
				 List<String> compOutputMsgs = (List<String>) CollectionUtils.subtract(outputMsgsUnion, inputMsgsUnion);
				 
				 // 3.若组合中需输入消息是由非过程网[1,2]提供,且需输出消息是由非过程网[1,2]接收,则生成组合片段
				 List<String> proMsgs = new ArrayList<String>();
				 proMsgs.addAll(proNets.get(0).getOutputMsgs());
				 proMsgs.addAll(proNets.get(1).getOutputMsgs());
				 List<String> comMsgs = new ArrayList<String>();
				 comMsgs.addAll(proNets.get(0).getInputMsgs());
				 comMsgs.addAll(proNets.get(1).getInputMsgs());
				 if (CollectionUtils.intersection(compInputMsgs, proMsgs).size() == 0 
						 && CollectionUtils.intersection(compOutputMsgs, comMsgs).size() == 0) {
					CompFrag compFrag = new CompFrag();
					compFrag.addFrag(frag1);
					compFrag.addFrag(frag2);
					compFrag.setInputMsgs(compInputMsgs);
					compFrag.setOutputMsgs(compOutputMsgs);
					result.add(compFrag);
				 }
				 
	         }
		 }
		return result;
		
	}
	
	//依次合并后面的一个片段集
	public List<CompFrag> composeSuccOneFragSet(List<CompFrag> set, List<ProTree> list, int proIndex, List<ProNet> proNets) {
		
		 List<CompFrag> result = new ArrayList<CompFrag>();
		 
		 for (int i = 0; i < set.size(); i++) {
			 
			CompFrag tempCompFrag = set.get(i);
			List<String> inputMsgs1 = tempCompFrag.getInputMsgs(); 
			List<String> outputMsgs1 = tempCompFrag.getOutputMsgs();
			
            for (int j = 0; j < list.size(); j++) {
            	
             ProTree frag = list.get(j);
             List<String> inputMsgs2 = getInputMsgs(frag, proNets.get(proIndex)); 
			 List<String> outputMsgs2 = getOutputMsgs(frag, proNets.get(proIndex)); 
			 
			 // 1.获取I1|_|I2和O1|_|O2(Note:可以容许重复)
			 List<String> inputMsgsUnion = new ArrayList<String>();
			 inputMsgsUnion.addAll(inputMsgs1);
			 inputMsgsUnion.addAll(inputMsgs2);
			 List<String> outputMsgsUnion = new ArrayList<String>();
			 outputMsgsUnion.addAll(outputMsgs1);
			 outputMsgsUnion.addAll(outputMsgs2);
			 
			 // 2.获取(I1|_|I2\O1|_|O2)和(O1|_|O2\I1|_|I2)
			 List<String> compInputMsgs = (List<String>) CollectionUtils.subtract(inputMsgsUnion, outputMsgsUnion);
			 List<String> compOutputMsgs = (List<String>) CollectionUtils.subtract(outputMsgsUnion, inputMsgsUnion);
			 
			 // 3.若组合中需输入消息是由非过程网[0,proIndex]提供,且需输出消息是由非过程网[0,proIndex]接收,则生成组合片段
			 if (CollectionUtils.intersection(compInputMsgs, 
					 getOutputMsgsInProNets(proIndex, proNets)).size() == 0 
					 && CollectionUtils.intersection(compOutputMsgs, 
							 getInputMsgsInProNets(proIndex, proNets)).size() == 0) {
				 //Note:新建组合片段
				 CompFrag compFrag = new CompFrag();
				 compFrag.getFrags().addAll(tempCompFrag.getFrags());
				 compFrag.addFrag(frag);
				 compFrag.setInputMsgs(compInputMsgs);
				 compFrag.setOutputMsgs(compOutputMsgs);
				 result.add(compFrag);
			 }
			 
            }
		 }
		return result;
		
	}
	
	
	/******************************实现基于执行计划迫使Utils*******************************/
	
	//获取过程网[0,proIndex]中输入消息集(Note:可以容许重复)
	public List<String> getInputMsgsInProNets(int proIndex, List<ProNet> proNets) {
		List<String> inputMsgs = new ArrayList<String>();
		for (int i = 0; i <= proIndex; i++) {
			inputMsgs.addAll(proNets.get(i).getInputMsgs());
		}
		return inputMsgs;
	}
	
	//获取过程网[0,proIndex]中输出消息集(Note:可以容许重复)
	public List<String> getOutputMsgsInProNets(int proIndex, List<ProNet> proNets) {
		List<String> outputMsgs = new ArrayList<String>();
		for (int i = 0; i <= proIndex; i++) {
			outputMsgs.addAll(proNets.get(i).getOutputMsgs());
		}
		return outputMsgs;
	}
	
	//获取片段中输入消息集
	public List<String> getInputMsgs(ProTree fragment, ProNet proNet) {
		//1. 获取片段中变迁集
		List<String> leafActs = getActsInFrag(fragment);
		//2. 获取输入消息库所集
		List<String> inputMsgs = new ArrayList<String>();
		for (String act : leafActs) {
			for (Flow flow : proNet.getFlows()) {
				String from = flow.getFlowFrom();
				String to = flow.getFlowTo();
				if (to.equals(act) && proNet.getMsgPlaces().contains(from)) {
					inputMsgs.add(from);
				}
			}
		}
		return inputMsgs;
		
	}
	
	//获取片段中输出消息集
	public List<String> getOutputMsgs(ProTree fragment, ProNet proNet) {
		//1. 获取片段中变迁集
		List<String> leafActs = getActsInFrag(fragment);
		//2. 获取输出消息库所集
		List<String> outputMsgs = new ArrayList<String>();
		for (String act : leafActs) {
			for (Flow flow : proNet.getFlows()) {
				String from = flow.getFlowFrom();
				String to = flow.getFlowTo();
				if (from.equals(act) && proNet.getMsgPlaces().contains(to)) {
					outputMsgs.add(to);
				}
			}
		}
		return outputMsgs;
		
	}
	
	//获取片段中活动集
	public List<String> getActsInFrag(ProTree fragment) {
		
		List<String> acts = new ArrayList<String>();
		List<Node> nodes = fragment.getNodes();
		for (Node node : nodes) {
			if (node.getType().equals("leaf")) {
				acts.add(node.getIdf());
			}
		}
		return acts;
	}
	
	//获取片段fragment中交互活动集
	public List<String> getInterActsInFrag(ProTree fragment, ProNet proNet) {
		
		List<String> interActs = proNet.getInterActs();
		List<String> interActsInFrag = new ArrayList<String>();
		List<Node> nodes = fragment.getNodes();
		for (Node node : nodes) {
			if (node.getType().equals("leaf")) {
				if (interActs.contains(node.getIdf())) {
					interActsInFrag.add(node.getIdf());
				}
			}
		}
		return interActsInFrag;
		
	}

	
}
	