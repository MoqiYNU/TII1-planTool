package toolkits.utils.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.petri.Flow;
import toolkits.def.petri.ProNet;
import toolkits.utils.file.DotUtils;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * 定义与过程树相关的Utils
 */
public class ProTreeUtils {
	
	private GenProTree genProTree;
	private DotUtils dotUtils;
	
	public ProTreeUtils() {
		genProTree = new GenProTree();
		dotUtils = new DotUtils();
	}
	
	/****************************从过程网中生成过程树 ******************************/
	
	public ProTree genProTreeFromProNet(ProNet proNet, int index) throws Exception {
		
		//1.获取proNet内网及移除并发结构中冗余库所
		InnerNet interNet = proNet.getInnerNet();
		InnerNet petri = removeReduPlaces(interNet);
		//2.获得初始过程树
		ProTree initProTree = genProTree.compute(petri);
		//dotUtils.pt2Dot(initProTree, proNet.getTranLabelMap(), "pt" + index);
		//3.压缩初始过程树
		ProTree proTree = compress(initProTree);
		dotUtils.pt2Dot(proTree, proNet.getTranLabelMap(), "cpt" + index);
		System.out.println("\n");
		return proTree;
		
	}
	
	/************************将过程树分解为片段 (自顶向下,避免生成重复片段)**************************/
	
	public List<ProTree> genFragments(ProTree proTree/*, InnerNet innerNet*/) throws Exception {
		
		//根据XOR节点分解后得到片段
		List<ProTree> fragments = new ArrayList<>();
		
		//即将访问的队列visitingQueue并将proTree入队
		Queue<ProTree> visitingQueue = new LinkedList<>(); 
		visitingQueue.offer(proTree);
		//迭代计算
	    while(visitingQueue.size() > 0){
	    	
	    	ProTree proTreeFrom = visitingQueue.poll();
	    	
	    	// 1.proTreeFrom不可分解,则直接添加到片段中并跳出本次循环
	    	Node xorNode = isDecompose(proTreeFrom);
	    	
	    	if (xorNode == null) {
	    		//Note:最终片段需压缩处理
				fragments.add(compress(proTreeFrom));
				continue;
			}
	    	
	    	// 2.proTreeFrom可分解,则首先获得其父亲节点
			Node xorNodeFather = getFatherNode(proTreeFrom, xorNode);
			
			if (xorNodeFather == null) {// 2.1若父亲节点为空,则以每个孩子节点为根生成一颗过程树
				
				List<Node> chaNodes = xorNode.getChaNodes();
				for (Node chaNode : chaNodes) {
					List<Node> desc = getDesc(chaNode);
					ProTree depProTree = new ProTree();//分解后过程树
					depProTree.setNodes(desc);
					visitingQueue.offer(depProTree);		
				}
				
			}else {// 2.2若父亲节点非空,则以xorNode节点的每个孩子节点生成一颗对应片段
				
				//System.out.println("test............................");
				
				List<Node> chaNodes = xorNode.getChaNodes();
				for (Node chaNode : chaNodes) {
					
					/*if (chaNode.getType().equals("leaf")) {
                		System.out.println("chaNode: " + innerNet.getTranLabelMap().get(chaNode.getIdf()));
					}else {
						System.out.println("chaNode: " + chaNode.getIdf());
					}*/
					
					//2.2.1 构建片段中节点及其关联孩子节点集
					List<Node> depNodes = new ArrayList<Node>();
					for (Node node : proTreeFrom.getNodes()) {
						//1.是父亲节点,则将其孩子节点中xorNode替换为chaNode
						if (node.getIdf().equals(xorNodeFather.getIdf())) {
							List<Node> updaChaNodesFather = new ArrayList<Node>();
							List<Node> chaNodesFather = xorNodeFather.getChaNodes();
							for (Node chaNodeFather : chaNodesFather) {
								if (chaNodeFather.getIdf().equals(xorNode.getIdf())) {
									//System.out.println("add node: " + chaNode.getIdf());
									updaChaNodesFather.add(chaNode);
								}else {
									updaChaNodesFather.add(chaNodeFather);
								}
							}
							//Note:必须新建,否则出错
							Node newXorNodeFather = new Node();
							newXorNodeFather.setIdf(xorNodeFather.getIdf());
							newXorNodeFather.setType(xorNodeFather.getType());
							newXorNodeFather.setChaNodes(updaChaNodesFather);
							depNodes.add(newXorNodeFather);
						}else {//2.否则直接添加
							depNodes.add(node);
						}
					}
					
					//2.2.2 构建分解过程树
					ProTree depProTree = new ProTree();
					depProTree.setNodes(depNodes);
					
					//2.2.3 移除xorNode节点
                    depProTree.removeNode(xorNode);
                    
                    //2.2.4 移除非chaNode节点及其子孙节点
                    List<Node> restNodes = getRestNodes(chaNodes, chaNode);
                    for (Node restNode : restNodes) {
                    	List<Node> desc = getDesc(restNode);
						for (Node descNode : desc) {
							//System.out.println("remove node:" + descNode.getIdf());
							depProTree.removeNode(descNode);
						}
					}
                    
                   /* List<Node> nodes = depProTree.getNodes();
                    for (Node node : nodes) {
                    	if (node.getType().equals("leaf")) {
                    		System.out.println("updated nodes: " + innerNet.getTranLabelMap().get(node.getIdf()));
						}else {
							System.out.println("updated nodes: " + node.getIdf());
						}
					}*/
          
					//2.2.4添加第j个分解树到访问队列
                    visitingQueue.offer(depProTree);
					
				}
			}
		}
	    return fragments;
	    
	}
	
	//获取chaNodes中除node外节点集
	public List<Node> getRestNodes(List<Node> chaNodes, Node node) {
		List<Node> restNodes = new ArrayList<Node>();
		for (Node chaNode : chaNodes) {
			if (chaNode.getIdf().equals(node.getIdf())) {
				continue;
			}
			restNodes.add(chaNode);
		}
		return restNodes;
		
	}
	
	//获取以node为根的所有叶子子孙节点的Idf
	public List<String> getLeafDesc(Node node) {
		
		List<String> desc = new ArrayList<>();
		Queue<Node> queue = new LinkedList<>();
		queue.add(node);
		while(queue.size() > 0){
			Node tempNode = queue.poll();
			if (tempNode.getType().equals("leaf")) {
				desc.add(tempNode.getIdf());
			}else {
				List<Node> tempNodeChaNodes = tempNode.getChaNodes();
				queue.addAll(tempNodeChaNodes);
			}
		}
		return desc;
		
	}
	
	//获取以node为根的所有子孙节点
	public List<Node> getDesc(Node node) {
		List<Node> desc = new ArrayList<Node>();
		desc.add(node);//Note:包含自己
		List<Node> chaNodes = node.getChaNodes();
		desc.addAll(chaNodes);
		Queue<Node> queue = new LinkedList<>();
		queue.addAll(chaNodes);
		while(queue.size() > 0){
			Node tempNode = queue.poll();
			if (tempNode.getType().equals("leaf")) {
				continue;
			}
			List<Node> tempNodeChaNodes = tempNode.getChaNodes();
			desc.addAll(tempNodeChaNodes);
			queue.addAll(tempNodeChaNodes);
		}
		return desc;
	}
	
	//获取xorNode的父亲节点
	public Node getFatherNode(ProTree proTree, Node xorNode) {
		List<Node> nodes = proTree.getNodes();
		for (Node node : nodes) {
			List<Node> chaNodes = node.getChaNodes();
			if (isChaNode(chaNodes, xorNode)) {
				return node;
			}
		}
		return null;
	}	
		
	//判断node是否为孩子节点
	public boolean isChaNode(List<Node> chaNodes, Node node) {
		for (Node chaNode : chaNodes) {
			if (chaNode.getIdf().equals(node.getIdf())) {
				return true;
			}
		}
		return false;
	}
	
	//判断proTree是否可以分解,即存在XOR节点
	public Node isDecompose(ProTree proTree) {
		List<Node> nodes = proTree.getNodes();
		int size = nodes.size();
		//Note:从根开始访问,避免生成重复的片段(测试B31)
		for (int i = size-1; i >= 0; i--) {
			Node node = nodes.get(i);
			String type = node.getType();
			if (type.equals("XOR")) {
				return node;
			}
		}
		return null;
	}
	
	//获取proTree中SEQ节点集
	public List<Node> getSEQNode(ProTree proTree) {
		
		List<Node> SEQNodes = new ArrayList<Node>();
		List<Node> nodes = proTree.getNodes();
		for (Node node : nodes) {
			String type = node.getType();
			if (type.equals("SEQ")) {
				SEQNodes.add(node);
			}
		}
		return SEQNodes;
		
	}
	

	/********************************预处理内网**********************************/
	
	//在对生成过程树之前,先移除内网中元并发部件中的冗余库所
	public InnerNet removeReduPlaces(InnerNet net) {
		
		InnerNet interNet = new InnerNet();//待生成内网
		List<Flow> noReduFlows = new ArrayList<Flow>();
		int pIndex = 0;//库所计数器
		
		GetAndBlock getAndBlock = new GetAndBlock();
		getAndBlock.compute(net);
		//Note:移除元并发部件中的冗余库所
		List<Block> andBlocks = getAndBlock.getMetaANDBlocks();
		
		//存储net中所有冗余库所
		List<String> reduPlaces = new ArrayList<String>();
		Map<String, String> map = new HashMap<String, String>();
		
		for (Block block : andBlocks) {//对每个并发块计算
			
			List<String> reduPlacesInPart = new ArrayList<String>();//part中冗余库所
			
			String entry = block.getEntry();
			String exit = block.getExit();
			List<String> entryPlaces = block.getEntryPost();
			for (String place : entryPlaces) {
				List<String> fromTrans = PetriUtils.getPostSet(place, net.getFlows());
				//若place满足(entry, place, exit),则为冗余
				if (fromTrans.size() == 1 && fromTrans.get(0).equals(exit)) {
					reduPlaces.add(place);
					reduPlacesInPart.add(place);
				}
			}
			//该并行部件中所有的库所均是冗余库所
			if (CollectionUtils.isEqualCollection(reduPlacesInPart, entryPlaces)) {
				map.put(entry, exit);
			}
		}
		
		//1.生成interNet中的流集
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (reduPlaces.contains(from) || reduPlaces.contains(to)) {
				continue;
			}else {
				noReduFlows.add(flow);
			}
		}
		
		for (Entry<String, String> elem : map.entrySet()) { 
			  
			String entry = elem.getKey();
			  String exit = elem.getValue();
			  
			  //新建中间库所rp
			  String rp = "rp" + pIndex;
			  pIndex ++;
			  interNet.addPlace(rp);//添加中间库所到interNet
			  
			  Flow flow1 = new Flow();
			  flow1.setFlowFrom(entry);
			  flow1.setFlowTo(rp);
			  noReduFlows.add(flow1);
			  
			  Flow flow2 = new Flow();
			  flow2.setFlowFrom(rp);
			  flow2.setFlowTo(exit);
			  noReduFlows.add(flow2);
			}
		
		    //2.生成interNet中的库所
			List<String> places = net.getPlaces();
	        for (Flow flow : noReduFlows) {
	        	String from = flow.getFlowFrom();
				String to = flow.getFlowTo();
				if (places.contains(from)) {
					interNet.addPlace(from);
				}
				if (places.contains(to)) {
					interNet.addPlace(to);
				}
			}
	        
	        //3.生成interNet中的变迁
	        List<String> trans = net.getTrans();
	        for (Flow flow : noReduFlows) {
	        	String from = flow.getFlowFrom();
				String to = flow.getFlowTo();
				if (trans.contains(from)) {
					interNet.addTran(from);
				}
				if (trans.contains(to)) {
					interNet.addTran(to);
				}
			}
	        
	        //返回interNet
	        interNet.setSource(net.getSource());
	        interNet.setSink(net.getSink());
	        interNet.setFlows(noReduFlows);
			return interNet;
		
	}
	
	/********************************压缩过程树**********************************/
	
	//将过程树进行压缩,以保证其中每个孩子节点与父亲节点的类型不同
	public ProTree compress(ProTree proTree) {
		
		//即将访问的队列visitingQueue并将proTree入队
		Queue<ProTree> visitingQueue = new LinkedList<>(); 
		visitingQueue.offer(proTree);
		
		//迭代计算
	    while(visitingQueue.size() > 0){
	    	
	    	ProTree proTreeFrom = visitingQueue.poll();
	    	
	    	// 1.若proTreeFrom不可压缩,则直接返回
		    Node compNode = isCompress(proTreeFrom);
		    if (compNode == null) {
				return proTreeFrom;
			}
		    
		    // 2.可以压缩,则新建过程树(Note:必须新建避免出错)
		    Node compNodeFather = getFatherNode(proTreeFrom, compNode);
		     //存储压缩过程树中的节点
			List<Node> compNodes = new ArrayList<Node>();
			for (Node node : proTreeFrom.getNodes()) {
				if (node.getIdf().equals(compNode.getIdf())) {//跳过待压缩节点
					continue;
				}
				if (node.getIdf().equals(compNodeFather.getIdf())) {//重新排列父亲节点下面的孩子节点
					//存储压缩后父节点的孩子节点
					List<Node> updaChaNodesFather = new ArrayList<Node>();
					for (Node chaNode : compNodeFather.getChaNodes()) {
						if (chaNode.getIdf().equals(compNode.getIdf())) {
							updaChaNodesFather.addAll(compNode.getChaNodes());
						}else {
							updaChaNodesFather.add(chaNode);
						}
					}
					//Note:必须新建,否则出错
					Node newCompNodeFather = new Node();
					newCompNodeFather.setIdf(compNodeFather.getIdf());
					newCompNodeFather.setType(compNodeFather.getType());
					newCompNodeFather.setChaNodes(updaChaNodesFather);
					compNodes.add(newCompNodeFather);
				}else {
					compNodes.add(node);
				}
			}
			
			//创建新压缩过程树
			ProTree compProTree = new ProTree();
			compProTree.setNodes(compNodes);
			visitingQueue.offer(compProTree);
			
	    }
		return null;
		
	}
	
	//判断pt能否压缩,即存在一个节点且其类型与其父亲节点一致
	public Node isCompress(ProTree pt) {
		List<Node> nodes = pt.getNodes();
		for (Node node : nodes) {//叶子节点无须压缩
			if (node.getType().equals("leaf")) {
				continue;
			}
			Node nodeFather = getFatherNode(pt, node);
			if (nodeFather == null) {//父节点为空无须压缩
				continue;
			}
			if (nodeFather.getType().equals(node.getType())) {
				return node;
			}
		}
		return null;
		
	}
	
}
