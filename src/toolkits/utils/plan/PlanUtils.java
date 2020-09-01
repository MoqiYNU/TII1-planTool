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
 * ����ִ�мƻ�����ȷ����ʹ����
 */
public class PlanUtils {
	
	private ProTreeUtils proTreeUtils;
	
	public PlanUtils() {
		proTreeUtils = new ProTreeUtils();
	}

	
	/******************************ʵ������ִ�мƻ�ִ��*******************************/
	
	public LTS execute(List<ProNet> proNets, List<CompFrag> plans) {
		
		System.out.println("begin execute................................" + "\n");
		
		// Step 1.�������LTS
		LTS lts = new LTS();
		List<String> ends = new ArrayList<String>();
		List<String> states = new ArrayList<String>();
		List<LTSTran> ltsTrans = new ArrayList<LTSTran>();
		
		// Step 2.������Ϲ���
		Composition composition = new Composition();
		composition.setProNets(proNets);
		ProNet compNet = composition.compose();
		
		// Step 3.�����ʼ���״̬
		int index = 0;
		String initIdf = "S" + index;
		Marking initMarking = compNet.getSource();
		//����ÿ��ҵ��������ó�ʼ��(Note:���üƻ�Ϊȫ���ƻ�,��ִ�лΪ�ռ�)
		int size = proNets.size();
		int planNum = plans.size();//ȷ��ȫ���ƻ�
		List<Bag> initBags = new ArrayList<Bag>();
		for (int i = 0; i < size; i++) {
			Bag bag = new Bag();
			for (int j = 0; j < planNum; j++) {
				bag.addPlan(j);
				ProTree frag = plans.get(j).getFrags().get(i);
				//��ȡfrag������Ҷ�ӽڵ�
				List<Node> nodes = frag.getNodes();
				System.out.println("print nodes: " + ", plan: " + j + ", pro: " + i);
				List<String> acts = new ArrayList<String>();
				for (Node node : nodes) {
					if (node.getType().equals("leaf")) {
						acts.add(node.getIdf());
					}
					System.out.println("node: " + node.getIdf());
				}
				//���ڵ������һ��
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
		
		//�������ʵĶ���visitingQueue���Ѿ����ʹ�����visitedQueue
		Queue<CompState> visitingQueue = new LinkedList<>(); 
		List<CompState> visitedQueue = new ArrayList<>();
		//����ʼ��ʶ��Ӳ���Ϊ�Ѿ�����
		visitingQueue.offer(initCompState);
		visitedQueue.add(initCompState);
		
		//��������
		while(visitingQueue.size() > 0){

		    //����һ����ʶ,�Դ˽���Ǩ��
		    CompState compStateFrom = visitingQueue.poll();
		    String idfFrom = compStateFrom.getIdf();
		    Marking markingFrom = compStateFrom.getMarking();
		    List<String> placesFrom = markingFrom.getPlaces();
		    List<Bag> bagsFrom = compStateFrom.getBags();
		    
		    //System.out.println("idfFrom: " + idfFrom);
		    
		    /*//��ӡ��ִ�л����
		    System.out.println("idfFrom: " + idfFrom + "..............");
		    for (int i = 0; i < size; i++) {
		    	System.out.println("pro: " + i + ", execActs: " + bagsFrom.get(i).getExecActs());
		    }*/
		    
		    //����ʹ�ܻ����
		    List<String> enableTrans = PetriUtils.getEnableTrans(compNet, placesFrom);
		    //System.out.println("enableTrans: " + enableTrans);
			for (String tran : enableTrans) {
				
				// 3.1��ȡtran��Ӧҵ����̱��
				int proNetIndex = PetriUtils.getProNetIndex(proNets, tran);
				
				// 3.2���ý�����ȡȫ�ֿ��üƻ���
				List<Integer> globalAvailPlans = new ArrayList<Integer>();
				globalAvailPlans.addAll(bagsFrom.get(0).getAvailPlans());
				for (int i = 1; i < size; i++) {
					globalAvailPlans = (List<Integer>) CollectionUtils.intersection(globalAvailPlans, bagsFrom.get(i).getAvailPlans());
				}
				//System.out.println("tran: " + tran + ", global plans: " + globalAvailPlans);
				//��ȫ�ֿ��üƻ�Ϊ��,������
				if (globalAvailPlans.size() == 0) {
					continue;
				}
				
				// 3.3��ȡ�ֲ����üƻ�
				List<Integer> localAvailPlans = new ArrayList<>();
                for (Integer plan : globalAvailPlans) {
                	// 3.3.1�ж�proNetIndex��ʶ��ҵ����̰��е�plan���ƻ��Ƿ����tran
                	List<String> preActs = bagsFrom.get(proNetIndex).getMaps().get(plan).get(tran);
                	//System.out.println("preActs: " + preActs);
                	if (preActs == null) {//δ����tran
						continue;
					}
                	//System.out.println("cover...");
                	// 3.3.2������tran,���ж���ǰ���Ƿ��Ѿ�ִ�����
                	List<String> execActs = bagsFrom.get(proNetIndex).getExecActs();
                	//System.out.println("proNetIndex: " + proNetIndex);
                	//System.out.println("test: " + execActs);
                	if (execActs.containsAll(preActs)) {
						localAvailPlans.add(plan);
					}
				}
                //System.out.println("localAvailPlans: " + localAvailPlans);
                //���ֲ��ƻ�Ϊ��,������
                if (localAvailPlans.size() == 0) {
					continue;
				}
                
                //���嵽�����״̬
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
                if (compState == null) {//���״̬δ���ʹ�
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
    				
				}else {//���״̬���ɹ�
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
	
	//��ȡ���ʹ����״̬
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
	
	//�ж��������Ƿ���ͬ
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
						//Note:ֻ��Ҫ��עִ�л��,��ִ�л��˳�����뿼��
						CollectionUtils.isEqualCollection(execActs1, execActs2)) {
					continue; 
				}else {
					return false;
				}
			}
		}
		return true;
		
	}
	
	//�ж�����ִ�������Ƿ���ͬ
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
	
	/******************************ʵ������ִ��·������ʹ *******************************/
	
	// Step4.�����ƻ�,�Դ�������ʶ--------------------------------------------------
	
	public List<CompFrag> enforce(List<ProNet> proNets) throws Exception {
		
		// 1.����ÿ������������Ƭ��,���ƻ�
		List<CompFrag> semtCompFrags = getSemtCompaCompFrags(proNets);
		// 2.��ӡ���Ƭ��..............
		PrintUtils.printCompFrag(semtCompFrags, proNets);
		
		return semtCompFrags;
		
	}
	
	//����Ƭ�ζ�Ӧ�������
	public ORDMatrix getORDMatrixFromFrag(ProTree frag) {
		
		// 1.��ȡ���ڵ㼰������Ҷ������ڵ�
		List<Node> nodes = frag.getNodes();
		//���ڵ������һ��
		Node root = nodes.get(nodes.size()-1);
		List<String> acts = proTreeUtils.getLeafDesc(root);
		
		// 2.����frag��Ӧͼgraph(�ڽӾ���洢),����ʼ��graph
        int vNum = acts.size();//�ڵ�����
		int[][] graph = new int[vNum][vNum];
		for(int i = 0; i < vNum; i++){//��ʼ��graphΪ0
			for(int j = 0; j < vNum; j++){
				graph[i][j] = 0;
			}
		}
		
		// 3.����ǰ���ϵ���þ���
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
	
	
	// Step3.�����������Ƭ����ϼ�--------------------------------------------------
	
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
	
	//�ж�graph�Ƿ�Ϊ�����޻�ͼ
	public boolean isDAG(int[][] graph) {
		for(int i = 0; i < graph.length; i++){
			//��Խڵ�i����DFS����
			boolean isDAG = detCycleByDFS(i, graph);
			if (!isDAG) {
				System.out.println("some cycles exist!" + "\n");
				return false;
			}
		}
		System.out.println("no cycles exist!" + "\n");
		return true;
		
	}
	
	//DFS����graph,�жϴӽڵ�i�������Ƿ���ڻ�
	private boolean detCycleByDFS(int i, int[][] graph) {
		
		//���嵱ǰ����·����״̬���ʶ���
		List<Integer> route = new ArrayList<>();
		route.add(i);
		List<Integer> visitedQueue = new ArrayList<>();
		
		do {
			//ȡ���Ԫ��,����ǰ״̬
			int size = route.size();
			int curState = route.get(size-1);
			
			//��ȡcurState���״̬��
			List<Integer> succStates = getSuccStates(curState, graph);
			int firstNotVisitedState = getFirstNotVisited(visitedQueue, succStates);
			
			// 1.�����ڻ�,����isDAGΪfalse
			if (CollectionUtils.intersection(succStates, route).size() != 0) {
				return false;
			}
			
			// 2.��curState����Ǩ��,����ݵ�ǰ·���������ѷ���
			if (succStates.size() == 0) {
				route.remove(size-1);//���ݵ�ǰ·��
				visitedQueue.add(curState);//��curState�ѷ���
			}else {
				// 3.����չ��״̬���ѷ���,����ݲ�����Ϊ�ѷ���
			    if (firstNotVisitedState == -1) {
			    	route.remove(size-1);//����·��
					visitedQueue.add(curState);//��curState�ѷ���
				}else {// 4.�������firstNotVisitedState
					route.add(firstNotVisitedState);
				}
			}
		} while (route.size() > 0);
		
		return true;
		
	}
	
	//���δ�����ʵĵ�һ��״̬.������״̬��������,�򷵻�-1
	private int getFirstNotVisited(List<Integer> visitedQueue, List<Integer> succeedStates) {
		for (Integer succeedState : succeedStates) {
			if (!visitedQueue.contains(succeedState)) {
				return succeedState;
			}
		}
		return -1;
	}

	//��ȡcurState���״̬��
	private List<Integer> getSuccStates(int curState, int[][] graph) {
		List<Integer> succeedStates = new ArrayList<>();
		for(int j = 0; j < graph.length; j++){
			if(graph[curState][j] != 0){	
				succeedStates.add(j);
			}
		}
		return succeedStates;
	}
	
	// Step2.��������ͼ--------------------------------------------------------
	
	public int[][] creatInterGraph(CompFrag compFrag, List<ProNet> proNets) {
		
		// 1.��ȡcompFrag�н������
		List<String> interActsInCompFrag = new ArrayList<String>();
		int size = proNets.size();
		for (int i = 0; i < size; i++) {
			ProTree frag = compFrag.getFrags().get(i);
			ProNet proNet = proNets.get(i);
			interActsInCompFrag.addAll(getInterActsInFrag(frag, proNet));
		}
		
		// 2.����compFrag��Ӧͼgraph(�ڽӾ���洢),����ʼ��graph
        int vNum = interActsInCompFrag.size();//�ڵ�����
		int[][] graph = new int[vNum][vNum];
		for(int i = 0; i < vNum; i++){//��ʼ��graphΪ0
			for(int j = 0; j < vNum; j++){
				graph[i][j] = 0;
			}
		}
		
		// 3.����ÿ��Ƭ���н����������ϵ
		for (int j = 0; j < size; j++) {
			ProTree frag = compFrag.getFrags().get(j);
			ProNet proNet = proNets.get(j);
			//��j��Ƭ���еĽ������
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
		
		// 4.����ÿ��Ƭ���н��������Ϣ������ϵ
		
		//Pre1.��Ϲ�����
        Composition comp = new Composition();
        comp.setProNets(proNets);
        ProNet compNet = comp.compose();
        
        for (String act : interActsInCompFrag) {//��ӡ�������
        	System.out.println("interActsInCompFrag: " + compNet.getTranLabelMap().get(act));
		}
        
        // Pre2.��ȡ��Ϣ������
        List<String> msgPlaces = compNet.getMsgPlaces();
        List<MsgPlaceBag> msgPlaceBags = comp.getMsgPlaceBags(msgPlaces, compNet.getFlows());
		
        // Pre3.ȷ����Ϣ���������ı�
		for (MsgPlaceBag bag : msgPlaceBags) {
			//�ж���Ϣ��bag�Ƿ��ڵ�ǰ���Ƭ����
			List<String> preSet = (List<String>) CollectionUtils.intersection(bag.getPreSet(), interActsInCompFrag);
			List<String> postSet = (List<String>) CollectionUtils.intersection(bag.getPostSet(), interActsInCompFrag);
			if (preSet.size() != 0) {//��bag������Ƭ���б�Ǩ����(�ɿ������,ÿ����Ϣ������Ƭ�������ֻ����һ�����ͺͽ�����Ϣ�ı�Ǩ)
				int index1 = getTranIndex(preSet.get(0), interActsInCompFrag);
				int index2 = getTranIndex(postSet.get(0), interActsInCompFrag);
				graph[index2][index1] = 1;
			}
		 }
		return graph;
		
	}
	
	//��ȡtran��λ�ñ��
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
	
	//��ȡǰ��ӳ��(�ɻ��acts����)
	public Map<String, List<String>> getPreOrdActsMap(ProTree frag, List<String> acts) {
		
		// 1.��ʼ��ӳ��
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        for (String act : acts) {
			map.put(act, new ArrayList<String>());
		}
        // 2.����ÿ��˳��ڵ���м���
        List<Node> SEQNodes = proTreeUtils.getSEQNode(frag);
        if (SEQNodes.size() == 0) {
			return map;
		}else {
			for (Node node : SEQNodes) {
				List<Node> chaNodes = node.getChaNodes();
				//Note:�ӵ�2�����ӽڵ���м���,��(i=1)
				for (int i = 1; i < chaNodes.size(); i++) {
					//��ȡ��ǰ�ڵ�chaNodes.get(i)������Ҷ�ӽڵ��а�����acts�еĻ
					List<String> curActs = (List<String>) CollectionUtils.
							intersection(proTreeUtils.getLeafDesc(chaNodes.get(i)), acts);
					//(0,i-1)�ڵ㼯�к��е�Ҷ�ӽڵ㼯(���޻��acts)
					for (String curAct : curActs) {
						map.put(curAct, getPreOrdActs(chaNodes, i, acts));
					}
				}
			}
		}
        return map;
		
	}
	
	//��ȡ��index�����ӽڵ�֮ǰ��Ҷ�ӽڵ㼯(���޻��acts)
	public List<String> getPreOrdActs(List<Node> chaNodes, int index, List<String> acts) {
		
		List<String> preActs = new ArrayList<String>();
		for (int i = 0; i < index; i++) {
			List<String> tempActs = proTreeUtils.getLeafDesc(chaNodes.get(i));
			preActs.addAll(CollectionUtils.intersection(tempActs, acts));
		}
		return preActs;
		
	}
	
	// Step1.�����﷨����Ƭ����ϼ�(�ؼ�)----------------------------------------------------------
	
	public List<CompFrag> getSyntCompaCompFrags(List<ProNet> proNets) throws Exception {
		
		// 1.��ȡ��ÿ���������е�Ƭ����ɼ���
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
		
		// 2.���õѿ����������﷨�������Ƭ��
		List<CompFrag> compFrags = descFrags(fragSets, proNets);
		//PrintUtils.printCompFrag(compFrags, proNets);//��ӡ���Ƭ�μ�
		return compFrags;
		
	}
	
	//���õѿ����������﷨�������Ƭ��
	public List<CompFrag> descFrags(List<List<ProTree>> fragSets, List<ProNet> proNets) {
		
		int size = fragSets.size();
		if (size == 1) {//1.ֻ��һ������,������ʹ
			System.out.println("There is no need to enforce...");
			return null;
		}else {//2.��������������
			List<CompFrag> set = composeFirstTwoFragSet(fragSets.get(0), fragSets.get(1), proNets);
			for (int i = 2; i < size; i++) {
				set = composeSuccOneFragSet(set, fragSets.get(i), i, proNets);
			}
			//Note:����������д��ڷ���/������Ϣ�Ŀ���,���޽���/���͸���Ϣ�Ŀ�����֮��Ӧ,��Ҫ�����Ƭ���Ƿ�յ�
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
	
	//����ǰ������Ƭ�μ��ϵĵѿ�����
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
				 
				 // 1.��ȡI1|_|I2��O1|_|O2(Note:���������ظ�)
				 List<String> inputMsgsUnion = new ArrayList<String>();
				 inputMsgsUnion.addAll(inputMsgs1);
				 inputMsgsUnion.addAll(inputMsgs2);
				 List<String> outputMsgsUnion = new ArrayList<String>();
				 outputMsgsUnion.addAll(outputMsgs1);
				 outputMsgsUnion.addAll(outputMsgs2);
				 
				 // 2.��ȡ(I1|_|I2\O1|_|O2)��(O1|_|O2\I1|_|I2)
				 List<String> compInputMsgs = (List<String>) CollectionUtils.subtract(inputMsgsUnion, outputMsgsUnion);
				 List<String> compOutputMsgs = (List<String>) CollectionUtils.subtract(outputMsgsUnion, inputMsgsUnion);
				 
				 // 3.���������������Ϣ���ɷǹ�����[1,2]�ṩ,���������Ϣ���ɷǹ�����[1,2]����,���������Ƭ��
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
	
	//���κϲ������һ��Ƭ�μ�
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
			 
			 // 1.��ȡI1|_|I2��O1|_|O2(Note:���������ظ�)
			 List<String> inputMsgsUnion = new ArrayList<String>();
			 inputMsgsUnion.addAll(inputMsgs1);
			 inputMsgsUnion.addAll(inputMsgs2);
			 List<String> outputMsgsUnion = new ArrayList<String>();
			 outputMsgsUnion.addAll(outputMsgs1);
			 outputMsgsUnion.addAll(outputMsgs2);
			 
			 // 2.��ȡ(I1|_|I2\O1|_|O2)��(O1|_|O2\I1|_|I2)
			 List<String> compInputMsgs = (List<String>) CollectionUtils.subtract(inputMsgsUnion, outputMsgsUnion);
			 List<String> compOutputMsgs = (List<String>) CollectionUtils.subtract(outputMsgsUnion, inputMsgsUnion);
			 
			 // 3.���������������Ϣ���ɷǹ�����[0,proIndex]�ṩ,���������Ϣ���ɷǹ�����[0,proIndex]����,���������Ƭ��
			 if (CollectionUtils.intersection(compInputMsgs, 
					 getOutputMsgsInProNets(proIndex, proNets)).size() == 0 
					 && CollectionUtils.intersection(compOutputMsgs, 
							 getInputMsgsInProNets(proIndex, proNets)).size() == 0) {
				 //Note:�½����Ƭ��
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
	
	
	/******************************ʵ�ֻ���ִ�мƻ���ʹUtils*******************************/
	
	//��ȡ������[0,proIndex]��������Ϣ��(Note:���������ظ�)
	public List<String> getInputMsgsInProNets(int proIndex, List<ProNet> proNets) {
		List<String> inputMsgs = new ArrayList<String>();
		for (int i = 0; i <= proIndex; i++) {
			inputMsgs.addAll(proNets.get(i).getInputMsgs());
		}
		return inputMsgs;
	}
	
	//��ȡ������[0,proIndex]�������Ϣ��(Note:���������ظ�)
	public List<String> getOutputMsgsInProNets(int proIndex, List<ProNet> proNets) {
		List<String> outputMsgs = new ArrayList<String>();
		for (int i = 0; i <= proIndex; i++) {
			outputMsgs.addAll(proNets.get(i).getOutputMsgs());
		}
		return outputMsgs;
	}
	
	//��ȡƬ����������Ϣ��
	public List<String> getInputMsgs(ProTree fragment, ProNet proNet) {
		//1. ��ȡƬ���б�Ǩ��
		List<String> leafActs = getActsInFrag(fragment);
		//2. ��ȡ������Ϣ������
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
	
	//��ȡƬ���������Ϣ��
	public List<String> getOutputMsgs(ProTree fragment, ProNet proNet) {
		//1. ��ȡƬ���б�Ǩ��
		List<String> leafActs = getActsInFrag(fragment);
		//2. ��ȡ�����Ϣ������
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
	
	//��ȡƬ���л��
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
	
	//��ȡƬ��fragment�н������
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
	