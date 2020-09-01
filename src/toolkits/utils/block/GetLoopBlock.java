package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.petri.Flow;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * ͨ��DFS���������е�����
 */
public class GetLoopBlock {
	
	private List<Block> loopBlocks;//�洢���е�����
	private List<Block> metaLoopBlocks;//�洢����Ԫ������
	private PetriUtils petriUtils;
	
	public GetLoopBlock() {
		loopBlocks = new ArrayList<Block>();
		metaLoopBlocks = new ArrayList<Block>();
		petriUtils = new PetriUtils();
	}
	
	//��ȡ���е�����
	public List<Block> getLoopBlocks() {
		return loopBlocks;
	}

	@SuppressWarnings("static-access")
	public List<Block> getMetaLoopBlocks(InnerNet net) {//��ȡ����Ԫ������
		compute(net);
        for (Block loopBlock : loopBlocks) {
        	List<String> entryActs = loopBlock.getEntryPost();
			List<String> exitActs = loopBlock.getExitPre();
			if (entryActs.size() == 1 && exitActs.size() == 1) {//������������Ϊ1��
				String entryAct = entryActs.get(0);
				String exitAct = exitActs.get(0);
				if (entryAct.equals(exitAct)) {
					List<String> preSet = petriUtils.getPreSet(entryAct, net.getFlows());
					List<String> postSet = petriUtils.getPostSet(exitAct, net.getFlows());
					if (CollectionUtils.isEqualCollection(preSet, postSet)) {
						metaLoopBlocks.add(loopBlock);
					}
				}
			}
		}
        return metaLoopBlocks;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void compute(InnerNet net) {
		
		//����֮ǰ����ջ���
		loopBlocks.clear();
		metaLoopBlocks.clear();
		
		//��ȡsource
		String initState = net.getSource();
		List route = new ArrayList();
		route.add(initState);
		
		List<String> visitedQueue = new ArrayList<>();
		
		do {
			int size = route.size();
			//ȡ���Ԫ��,����ǰ״̬
			String curState = (String) route.get(size-1);
			//�����curState��ʼ����
			List<Flow> succeedFlows = getSucceedTrans(net, curState, route, size-1);
			if (succeedFlows.size() == 0) {//����Ǩ��,�����
				//����·��
				route.remove(size-1);
				//�����Ѿ�����
				visitedQueue.add(curState);
			}else {
				Flow firstNotVisitedFlow = getFirstNotVisited(visitedQueue, succeedFlows);
				//����չ��״̬���Ѿ�����,����ݲ���Ϊ�ѷ���
			    if (firstNotVisitedFlow == null) {
			    	//����·��
			    	route.remove(size-1);
					//�����Ѿ�����
					visitedQueue.add(curState);
				}else {
					String succeedeState = firstNotVisitedFlow.getFlowTo();
					route.add(succeedeState);
				}
			}
		} while (route.size() > 0);
    }

	//���δ�����ʵĵ�һ����,����������������,�򷵻�null
	private Flow getFirstNotVisited(List<String> visitedQueue, List<Flow> succeedFlows) {
		for (Flow flow : succeedFlows) {
			String stateTo = flow.getFlowTo();
			if (!stateIsVisited(visitedQueue, stateTo)) {
				return flow;
			}
		}
		return null;
	}
	
	//�жϺ��״̬�Ƿ����
	public boolean stateIsVisited(List<String> visitedQueue, String succeedState) {
		for (String state : visitedQueue) {
			if (state.equals(succeedState)) {
				return true;
			}
		}
		return false;
	}

	//���state�������
	@SuppressWarnings("rawtypes")
	private List<Flow> getSucceedTrans(InnerNet net, String state, List route, int curIndex) {
		List<Flow> succeedFlows = new ArrayList<>();
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (state.equals(from)) {
				if (curIndex%2 == 1) {//��fromΪ��Ǩ(����ǰλ��Ϊ����),to�ǿ���,������Ƿ����ѭ���ṹ
					int index = isLoop(route, to);
					if (index == -1) {//��toδ���ɻ��ṹ
						succeedFlows.add(flow);
					}else {//��to���ɻ��ṹ,������ѭ������,��entry->act->...->from/state->to/entry
						String entry = (String) route.get(index);
						String act = (String) route.get(index+1);
						//����������
						Block block = new Block();
						//�˿ںͳ��ھ�Ϊ����
						block.setEntry(entry);
						block.setExit(entry);
						//��ڳ����
						block.addEntryPost(act);
						//������ڻ
						block.addExitPre(state);
						block.setType("Loop");
						loopBlocks.add(block);
					}
				}else {//��to�Ǳ�Ǩ,��ֱ�����
					succeedFlows.add(flow);
				}
			}
		}
		return succeedFlows;
	}
	
	//�ж��Ƿ����ѭ���ṹ
	@SuppressWarnings("rawtypes")
	public int isLoop(List route, String to) {
		int routeSize = route.size();
		for (int i = 0; i < routeSize; i++) {
			if (i%2 == 0) {//ż��Ϊ��ʶ,����Ϊ0
				String state = (String) route.get(i);
				if (to.equals(state)) {
					return i;
				}
			}else {
				continue;
			}
		}
		return -1;
	}

}
