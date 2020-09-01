package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import toolkits.def.petri.Flow;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * ͨ��DFS����������ѡ���
 */
public class GetXorBlock {
	
	//����Ԫѡ�񲿼�
	private List<Block> metaXORBlocks;
	
	public GetXorBlock() {
		metaXORBlocks = new ArrayList<Block>();
	}
	
	public List<Block> getMetaXORBlocks() {
		return metaXORBlocks;
	}
	
	public void compute(InnerNet net, List<Block> loopBlocks) {
		
		//����ǰ����ջ���metaXORBlocks
		metaXORBlocks.clear();
		
		// 1.����ѭ�����д���ڳ����Ļ����������ڵĻ��
		List<String> entryActsInLoop = new ArrayList<String>();
		List<String> exitActsInLoop = new ArrayList<String>();
		for (Block block : loopBlocks) {
			entryActsInLoop.addAll(block.getEntryPost());
			exitActsInLoop.addAll(block.getExitPre());
		}
		
		// 2.���ÿ����֧�������м���Ԫѡ���
		List<String> places = net.getPlaces();
		for (String place : places) {
			//��place����������(�ų���ѭ������)
			List<Flow> splitFlows = computeSplitFlows(place, net.getFlows(), entryActsInLoop);
			if (splitFlows.size() > 1) {//Ϊ��֧����
				Block block = creatMetaXORBlock(place, splitFlows, net.getFlows());
				if (block != null) {
					metaXORBlocks.add(block);
				}
			}
		}
		
	}
	
	//�����place����������(Note:�ų�ѭ������)
	private List<Flow> computeSplitFlows(String place, List<Flow> flows, List<String> entryActsInLoop) {
		
		List<Flow> succFlows = new ArrayList<Flow>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (place.equals(from) && !entryActsInLoop.contains(to)) {
				succFlows.add(flow);
			}
		}
		return succFlows;
	}
	
	//����Ԫѡ���
	public Block creatMetaXORBlock(String place, List<Flow> splitFlows, List<Flow> flows) {
		
		List<String> exits = new ArrayList<String>();
		for (Flow flow : splitFlows) {//���ÿ����֧�����м���
			String act = flow.getFlowTo();
			List<String> preSet = PetriUtils.getPreSet(act, flows);
			List<String> postSet = PetriUtils.getPostSet(act, flows);
			if (preSet.size() == 1 && postSet.size() == 1) {
				exits = (List<String>) CollectionUtils.union(exits, postSet);
			}else {
				return null;
			}
		}
        
        if (exits.size() == 1) {//��֧���Ϊһ������,��ΪԪѡ��ṹ
        	Block block = new Block();
			block.setEntry(place);
			//������place�����ı�Ǩ��
			for (Flow flow : splitFlows) {
				block.addEntryPost(flow.getFlowTo());
				block.addExitPre(flow.getFlowTo());
			}
			block.setType("XOR");
			block.setExit(exits.get(0));
			return block;
		}else {
			return null;
		}
		
	}
	
	//�����place�����ı�Ǩ��(Note:�ų���ѭ������)
	public List<String> getToTransNotInLoop(String place, InnerNet net, List<Block> loopBlocks) {
		
		List<String> trans = new ArrayList<String>();
		//����ѭ�������д���ڳ����Ļ����������ڵĻ��
		List<String> entryActsInLoop = new ArrayList<String>();
		List<String> exitActsInLoop = new ArrayList<String>();
		for (Block block : loopBlocks) {
			entryActsInLoop.addAll(block.getEntryPost());
			exitActsInLoop.addAll(block.getExitPre());
		}
		//��place����������(�ų���ѭ������)
		List<Flow> splitFlows = computeSplitFlows(place, net.getFlows(), entryActsInLoop);
		for (Flow flow : splitFlows) {
			String tran = flow.getFlowTo();
			trans.add(tran);
		}
		return trans;
	}
	
}
