package toolkits.utils.block;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import toolkits.def.petri.Flow;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * ����������Ӧ�Ĺ�����
 */
public class GenProTree {
	
	public ProTree compute(InnerNet net) {
		
		ProTree tree = new ProTree();//���������
		
		//˳�򲿼�,��������,ѡ�񲿼���ѭ��������ʶ
		int seqIndex = 0;
		int andIndex = 0;
		int xorIndex = 0;
		int loopIndex = 0;
		
		//��ʶ�滻������������,����ѭ���Ͳ����ṹ(Note:������Ҫע��)
		int pIndex = 0;
		
		//�������ʵĶ���visitingQueue
		Queue<InnerNet> visitingQueue = new LinkedList<>();
		visitingQueue.offer(net);
		
		//��������
		outer:
	    while(visitingQueue.size() > 0){
	    	
	    	//����һ������,�Դ˽���Ǩ��
	    	InnerNet netFrom = visitingQueue.poll();
	    	
	    	/*List<Flow> flows = netFrom.getFlows();
			for (Flow flow : flows) {
				System.out.println("Flow: " + flow.getFlowFrom() + " " + flow.getFlowTo());
			}*/
	    	
	    	//netFromֻ����1����Ǩ,����ΪҶ�ӽڵ�Ȼ���˳�����
	    	if (netFrom.getTrans().size() == 1) {
	    		Node preNode = getNodeByIdf(tree, netFrom.getTrans().get(0));
	    		if (preNode == null) {//��δ���ɹ�
	    			Node node = new Node();
		    		node.setIdf(netFrom.getTrans().get(0));
		    		node.setType("leaf");
		    		tree.addNode(node);
				}
				break;
			}
	    	
	    	//1.�������˳�򲿼�
	    	GetSeqBlock getSeqPart = new GetSeqBlock();
			getSeqPart.compute(netFrom);
			List<Block> maxSEQBlocks = getSeqPart.getMaxSEQBlocks();
			
			//ȡ��һ�����˳�������滻
			if (maxSEQBlocks.size() > 0) {//������1�����˳���
				//System.out.println("SEQ....................................");
				Block block = maxSEQBlocks.get(0);
				Node seqNode = new Node();
				//�滻˳�򲿼��ı�Ǩ
				String seqTran = "SEQ"+ seqIndex;
				seqIndex ++;
				seqNode.setIdf(seqTran);
				seqNode.setType("SEQ");
				
				//System.out.println("˳��ֽ����: " + seqTran + " ���: " + part.getEntry());
				
				List<String> seqActs = block.getSeqActs();
				//ÿ��seqAct��Ӧһ��Ҷ�ӽڵ�
				for (String seqAct : seqActs) {
					//����ȷ���Ƿ��Ѿ����ɽڵ�
					Node node = getNodeByIdf(tree, seqAct);
					if (node == null) {//δ���ɹ�
						//����Ҷ�ӽڵ�,���ֱ������seqNode��tree��
						Node chaNode = new Node();
						chaNode.setIdf(seqAct);
						chaNode.setType("leaf");
						seqNode.addChaNode(chaNode);
						tree.addNode(chaNode);
					}else {//���ɹ�,�������seqNode
						seqNode.addChaNode(node);
					}
				}
				tree.addNode(seqNode);
				
				InnerNet netTo = seqBlockReplace(netFrom, seqActs, seqTran);
				visitingQueue.offer(netTo);
				//Note:���½��е�������,��goto��while
				continue outer;
			}
			
			//2.����Ԫ���в���
			GetAndBlock getAndBlock = new GetAndBlock();
			getAndBlock.compute(netFrom);
			List<Block> metaANDBlocks = getAndBlock.getMetaANDBlocks();
			//ȡ��һ��Ԫ����������滻
			if (metaANDBlocks.size() > 0) {//������1��Ԫ���в���
				//System.out.println("AND....................................");
				Block block = metaANDBlocks.get(0);
				
				//2.1�����滻�Ĳ��нڵ�
				Node ANDNode = new Node();
				String flowTran = "AND"+ andIndex;
				andIndex ++;
				ANDNode.setIdf(flowTran);
				ANDNode.setType("AND");
				
				//2.2�����в��������滻
				List<String> fromPlaces = block.getEntryPost();
				List<String> postActs = new ArrayList<String>();
				for (String place : fromPlaces) {
					List<String> postSet = getPostSet(place, netFrom.getFlows());
					postActs.addAll(postSet);
				}
                for (String postAct : postActs) {//��Բ������е�ÿ��Ǩ�ƽ��м���
                	//����ȷ���Ƿ��Ѿ����ɽڵ�
					Node node = getNodeByIdf(tree, postAct);
					if (node == null) {//δ���ɹ�
						//����Ҷ�ӽڵ�
						Node chaNode = new Node();
						chaNode.setIdf(postAct);
						chaNode.setType("leaf");
						ANDNode.addChaNode(chaNode);
						tree.addNode(chaNode);
					}else {
						ANDNode.addChaNode(node);
					}
				}
				tree.addNode(ANDNode);
				
				String entry = block.getEntry();
				String exit = block.getExit();
				InnerNet netTo = andBlockReplace(netFrom, entry, exit, postActs, flowTran, pIndex);
				//Note:pIndex��andBlockReplace���²���Ӱ��������,������Ӧ����
				pIndex ++;
				pIndex ++;
				visitingQueue.offer(netTo);
				//���½��е�������,��goto��while
				continue outer;
			}
			
			//3.����Ԫѡ���,Ȼ������滻
			GetLoopBlock getLoopBlock = new GetLoopBlock();
			getLoopBlock.compute(netFrom);
			List<Block> loopBlocks = getLoopBlock.getLoopBlocks();
			GetXorBlock getXorBlock = new GetXorBlock();
			getXorBlock.compute(netFrom, loopBlocks);
			List<Block> metaXORBlocks = getXorBlock.getMetaXORBlocks();
			
			//ȡ��һ��Ԫ����������滻
			if (metaXORBlocks.size() > 0) {//������1��Ԫѡ���
				//System.out.println("XOR....................................");
				Block part = metaXORBlocks.get(0);
				
				//�滻��ѡ���Ǩ
				Node XORNode = new Node();
				String xorTran = "XOR"+ xorIndex;
				xorIndex ++;
				XORNode.setIdf(xorTran);
				XORNode.setType("XOR");
				
				String entry = part.getEntry();
				List<String> transInXor = getXorBlock.getToTransNotInLoop(entry, netFrom, loopBlocks);
				for (String tranInXor : transInXor) {
					//����ȷ���Ƿ��Ѿ����ɽڵ�
					Node node = getNodeByIdf(tree, tranInXor);
					if (node == null) {//δ���ɹ�
						//����Ҷ�ӽڵ�
						Node chaNode = new Node();
						chaNode.setIdf(tranInXor);
						chaNode.setType("leaf");
						XORNode.addChaNode(chaNode);
						tree.addNode(chaNode);
					}else {
						XORNode.addChaNode(node);
					}
				}
				//���ѡ��ڵ�
				tree.addNode(XORNode);
				
				InnerNet netTo = xorBlockReplace(netFrom, entry, part.getExit(), transInXor, xorTran);
				visitingQueue.offer(netTo);
				continue outer;
			}
			
			//4.����Ԫѭ����
			List<Block> metaLoopBlocks = getLoopBlock.getMetaLoopBlocks(netFrom);
			//ȡ��һ��Ԫѭ��������滻
			if (metaLoopBlocks.size() > 0) {//������1��Ԫѭ����
				
				//System.out.println("loop....................................");
				
				//��entry���������ڶ��ѭ���ṹ
				List<Block> replacedLoopBlocks = getXORLooBlocks(metaLoopBlocks, loopBlocks);
				if (replacedLoopBlocks.size() > 0) {//��entry������ÿ��������Ԫ��
					String entry = replacedLoopBlocks.get(0).getEntry();
					
					//ѭ���ڵ�
					Node loopNode = new Node();
					String loopTran = "Loop"+ loopIndex;
					loopIndex ++;
					loopNode.setIdf(loopTran);
					loopNode.setType("Loop");
					
					if (replacedLoopBlocks.size() == 1) {//4.1 ����ڳ���ֻ��һ��Ԫ��
						String act = replacedLoopBlocks.get(0).getEntryPost().get(0);
						//����ȷ���Ƿ��Ѿ����ɽڵ�
						Node node = getNodeByIdf(tree, act);
						if (node == null) {//δ���ɹ�
							//����Ҷ�ӽڵ�
							Node chaNode = new Node();
							chaNode.setIdf(act);
							chaNode.setType("leaf");
							loopNode.addChaNode(chaNode);
							tree.addNode(chaNode);
						}else {//���ɹ�,��ֱ�������switNode
							loopNode.addChaNode(node);
						}
						tree.addNode(loopNode);
						
					}else {//4.2 ����ڳ����ж��Ԫ��
					
					//ѡ��ڵ�
					Node xorNode = new Node();
					String xorTran = "XOR"+ xorIndex;
					xorIndex ++;
					xorNode.setIdf(xorTran);
					xorNode.setType("XOR");
					
					for (Block part : replacedLoopBlocks) {
						//���ѭ������ֻ��1��act
						String act = part.getEntryPost().get(0);
						//����ȷ���Ƿ��Ѿ����ɽڵ�
						Node node = getNodeByIdf(tree, act);
						if (node == null) {//δ���ɹ�
							//����Ҷ�ӽڵ�
							Node chaNode = new Node();
							chaNode.setIdf(act);
							chaNode.setType("leaf");
							xorNode.addChaNode(chaNode);
							tree.addNode(chaNode);
						}else {//���ɹ�,��ֱ�������switNode
							xorNode.addChaNode(node);
						}
					}
					tree.addNode(xorNode);
					loopNode.addChaNode(xorNode);
					tree.addNode(loopNode);
				    }
					
					//��ȡѭ���е����л
					List<String> actsInLoop = new ArrayList<String>();
					for (Block block : replacedLoopBlocks) {
						String act = block.getEntryPost().get(0);
						if (!actsInLoop.contains(act)) {
							actsInLoop.add(act);
						}
					}
					
					List<String> transInXor = getXorBlock.getToTransNotInLoop(entry, netFrom, loopBlocks);
					InnerNet netTo = loopBlockReplace(netFrom, actsInLoop, transInXor, entry, loopTran, pIndex);
					//Note:pIndex��loopBlockReplace���²���Ӱ��������,������Ӧ����
					pIndex ++;
					visitingQueue.offer(netTo);
					continue outer;
				}
			}
			
	    }
	    return tree;
		
	}
	

	/**********************����������˳����滻******************************/
	
	public InnerNet seqBlockReplace(InnerNet net, List<String> seqActs, 
			String seqTran) {
		
		InnerNet replaceNet = new InnerNet();
		
		//1.�滻������е�����
		List<Flow> replacedFlows = new ArrayList<Flow>();
		//1.1 �Ƴ�������seqActs�б�Ǩ��������
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (seqActs.contains(from) || seqActs.contains(to)) {
				continue;
			}else {
				replacedFlows.add(flow);
			}
		}
		//1.2 ���˳����Ӧ��ǨseqTran�γ���
		String placeFrom = getEntryPlaceInSeqPart(seqActs.get(0), flows);
		Flow flow1 = new Flow();
		flow1.setFlowFrom(placeFrom);
		flow1.setFlowTo(seqTran);
		replacedFlows.add(flow1);
		String placeTo = getExitPlaceInSeqPart(seqActs.get(seqActs.size()-1), flows);
		Flow flow2 = new Flow();
		flow2.setFlowFrom(seqTran);
		flow2.setFlowTo(placeTo); 
		replacedFlows.add(flow2);
		
		//2.�����滻�����еĿ���
		List<String> places = net.getPlaces();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (places.contains(from)) {
				replaceNet.addPlace(from);
			}
			if (places.contains(to)) {
				replaceNet.addPlace(to);
			}
		}
        
        //3.�����滻�����еı�Ǩ
        List<String> trans = net.getTrans();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (trans.contains(from)) {
				replaceNet.addTran(from);
			}
			if (trans.contains(to)) {
				replaceNet.addTran(to);
			}
		}
        replaceNet.addTran(seqTran);
        
        //�����滻��
        replaceNet.setSource(net.getSource());
        replaceNet.setSink(net.getSink());
        replaceNet.setFlows(replacedFlows);
		return replaceNet;
		
	}
	
	/**********************���������в������滻******************************/
	
	private InnerNet andBlockReplace(InnerNet net, String entry, String exit, 
			List<String> postActs, String flowTran, int pIndex) {
		
		InnerNet replaceNet = new InnerNet();
		
		//1.�滻�����е�����
		List<Flow> replacedFlows = new ArrayList<Flow>();
		//1.1�����벢�����л�����Ŀ�����
		List<String> relPlaces = new ArrayList<String>();
		for (String postAct : postActs) {//��Բ������е�ÿ��Ǩ�ƽ��м���
			relPlaces.addAll(PetriUtils.getPreSet(postAct, net.getFlows()));
			relPlaces.addAll(PetriUtils.getPostSet(postAct, net.getFlows()));
		}
		//1.2�Ƴ�����relPlaces��������
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (relPlaces.contains(from) || relPlaces.contains(to)) {
				continue;
			}else {
				replacedFlows.add(flow);
			}
		}
		
		//1.3��Ӳ�������flowTran�γ���,��entry->rp1->flowTran->rp2->exit
		// Note:pIndexδ����������.....................................
		Flow flow1 = new Flow();
		flow1.setFlowFrom(entry);
		String rp1 = "rp" + pIndex;
		pIndex ++;
		flow1.setFlowTo(rp1);
		Flow flow2 = new Flow();
		flow2.setFlowFrom(rp1);
		flow2.setFlowTo(flowTran);
		Flow flow3 = new Flow();
		flow3.setFlowFrom(flowTran);
		String rp2 = "rp" + pIndex;
		pIndex ++;
		flow3.setFlowTo(rp2);
		Flow flow4 = new Flow();
		flow4.setFlowFrom(rp2);
		flow4.setFlowTo(exit);
		
		replacedFlows.add(flow1);
		replacedFlows.add(flow2);
		replacedFlows.add(flow3);
		replacedFlows.add(flow4);
		
		//2.�����滻�����еĿ���
		List<String> places = net.getPlaces();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (places.contains(from)) {
				replaceNet.addPlace(from);
			}
			if (places.contains(to)) {
				replaceNet.addPlace(to);
			}
		}
        replaceNet.addPlace(rp1);
        replaceNet.addPlace(rp2);
        
        //3.�����滻�����еı�Ǩ
        List<String> trans = net.getTrans();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (trans.contains(from)) {
				replaceNet.addTran(from);
			}
			if (trans.contains(to)) {
				replaceNet.addTran(to);
			}
		}
        replaceNet.addTran(flowTran);
        
        //�����滻��
        replaceNet.setSource(net.getSource());
        replaceNet.setSink(net.getSink());
        replaceNet.setFlows(replacedFlows);
        
		return replaceNet;
		
	}
	
	/**********************����������ѡ����滻******************************/
	
	public InnerNet xorBlockReplace(InnerNet net, String entry, String exit, 
			List<String> transInXor, String xorTran) {
		
		//�����滻�������
		InnerNet replaceNet = new InnerNet();
		
		//1.�����滻�����
		List<Flow> replacedFlows = new ArrayList<Flow>();
		//�Ƴ�������transInXor�б�Ǩ��������
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (transInXor.contains(from) || transInXor.contains(to)) {
				continue;
			}else {
				replacedFlows.add(flow);
			}
		}
		//���ѡ�񲿼���Ӧ��ǨxorTran�γ���
		Flow flow1 = new Flow();
		flow1.setFlowFrom(entry);
		flow1.setFlowTo(xorTran);
		replacedFlows.add(flow1);
		Flow flow2 = new Flow();
		flow2.setFlowFrom(xorTran);
		flow2.setFlowTo(exit); 
		replacedFlows.add(flow2);
		
		//2.�����滻������п���
		List<String> places = net.getPlaces();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (places.contains(from)) {
				replaceNet.addPlace(from);
			}
			if (places.contains(to)) {
				replaceNet.addPlace(to);
			}
		}
        
        //3.�����滻�����еı�Ǩ
        List<String> trans = net.getTrans();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (trans.contains(from)) {
				replaceNet.addTran(from);
			}
			if (trans.contains(to)) {
				replaceNet.addTran(to);
			}
		}
        //���xorTran
        replaceNet.addTran(xorTran);
        
        //�����滻��
        replaceNet.setSource(net.getSource());
        replaceNet.setSink(net.getSink());
        replaceNet.setFlows(replacedFlows);
		return replaceNet;
		
	}
	
	/**********************����������ѭ�����滻****************************/
	
	private InnerNet loopBlockReplace(InnerNet net, List<String> actsInLoop, 
			List<String> transInXor, String entry, String loopTran, int pIndex) {
		InnerNet replaceNet = new InnerNet();
		//1.�滻������е�����
		List<Flow> replacedFlows = new ArrayList<Flow>();
		//1.1 �Ƴ�������actsInLoop�б�Ǩ������������entry��������
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (actsInLoop.contains(from) || actsInLoop.contains(to) 
					|| from.equals(entry)) {//��entry������
				continue;
			}else {
				replacedFlows.add(flow);
			}
		}
		
		Flow flow1 = new Flow();
		flow1.setFlowFrom(entry);
		flow1.setFlowTo(loopTran);
		replacedFlows.add(flow1);
		Flow flow2 = new Flow();
		flow2.setFlowFrom(loopTran);
		String rp = "rp" + pIndex;
		pIndex ++;
		flow2.setFlowTo(rp);
		replacedFlows.add(flow2);
		
		for (String tranInXor : transInXor) {
			Flow flow = new Flow();
			flow.setFlowFrom(rp);
			flow.setFlowTo(tranInXor);
			replacedFlows.add(flow);
		}
		
		//2.�����滻�����еĿ���
		List<String> places = net.getPlaces();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (places.contains(from)) {
				replaceNet.addPlace(from);
			}
			if (places.contains(to)) {
				replaceNet.addPlace(to);
			}
		}
        replaceNet.addPlace(rp);
        
        //3.�����滻�����еı�Ǩ
        List<String> trans = net.getTrans();
        for (Flow flow : replacedFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (trans.contains(from)) {
				replaceNet.addTran(from);
			}
			if (trans.contains(to)) {
				replaceNet.addTran(to);
			}
		}
        replaceNet.addTran(loopTran);
        
        //�����滻��
        replaceNet.setSource(net.getSource());
        replaceNet.setSink(net.getSink());
        replaceNet.setFlows(replacedFlows);
		return replaceNet;
		
	}
	
	//��ȡ˳�򲿼������(��Ǩ)����Ŀ���
	public String getEntryPlaceInSeqPart(String entry, List<Flow> flows) {
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (to.equals(entry)) {
				return from;
			}
		}
		return null;
	}
	
	//��ȡ˳�򲿼��г���(��Ǩ)����Ŀ���
	public String getExitPlaceInSeqPart(String exit, List<Flow> flows) {
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (from.equals(exit)) {
				return to;
			}
		}
		return null;
	}
	
	//ͨ��idf��ȡ�������нڵ�
	public Node getNodeByIdf(ProTree tree, String idf) {
		List<Node> nodes = tree.getNodes();
		for (Node node : nodes) {
			String tempIdf = node.getIdf();
			if (idf.equals(tempIdf)) {
				return node;
			}
		}
		return null;
	}
	
	//��ȡԪ��elem�ĺ�
	public List<String> getPostSet(String elem, List<Flow> flows) {
		List<String> postSet = new ArrayList<String>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (elem.equals(from)) {
				postSet.add(to);
			}
		}
		return postSet;
	}
	
	//ȷ����entry���������л��ṹ����Ԫ�ṹ
	public List<Block> getXORLooBlocks(List<Block> maxLoopBlocks, List<Block> loopBlocks) {
		List<String> entries = getEntriesInLoopBlocks(maxLoopBlocks);
		for (String entry : entries) {
			List<Block> blocksInMax = getBlocksWithTheSameEntry(entry, maxLoopBlocks);
			List<Block> blocksInAll = getBlocksWithTheSameEntry(entry, loopBlocks);
			if (blocksInMax.size() == blocksInAll.size()) {//��entry���������л���������
				return blocksInMax;
			}
		}
		return null;
	}
	
	//��ô�entry���������л��ṹ
	public List<Block> getBlocksWithTheSameEntry(String entry, List<Block> loopBlocks) {
		List<Block> blacks = new ArrayList<Block>();
		for (Block loopBlock : loopBlocks) {
			String tempEntry = loopBlock.getEntry();
			if (entry.equals(tempEntry)) {
				blacks.add(loopBlock);
			}
		}
		return blacks;
	}
	
	//��ȡlooParts�е��������(����)
	public List<String> getEntriesInLoopBlocks(List<Block> looParts) {
		List<String> entries = new ArrayList<String>();
		for (Block part : looParts) {
			String entry = part.getEntry();
			if (!entries.contains(entry)) {
				entries.add(entry);
			}
		}
		return entries;
	}
	
}
