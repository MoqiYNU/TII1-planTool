package toolkits.utils.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.ImageIcon;

import net.sourceforge.jpowergraph.defaults.DefaultGraph;
import net.sourceforge.jpowergraph.defaults.TextEdge;
import pipe.gui.CreateGui;
import pipe.gui.widgets.GraphFrame;
import pipe.jpowergraph.RGGeneralState;
import pipe.jpowergraph.RGInitialState;
import pipe.jpowergraph.RGLoopWithTextEdge;
import pipe.jpowergraph.RGNode;
import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;
import toolkits.def.petri.Edge;
import toolkits.def.petri.Marking;
import toolkits.def.petri.Path;
import toolkits.def.petri.RG;
import toolkits.def.petri.Trace;

/**
 * @author Moqi
 * ����ɴ�ͼUtils
 */
public class RGUtils {
	
	
	/***************************��·�������켣�ͱ�Ǩ��*****************************/
	
	@SuppressWarnings("rawtypes")
	public static Trace genTraceFromPath(Path path) {
		
		List<String> events = new ArrayList<String>();
		List sequence = path.getSequence();
		int size = sequence.size();
		for (int i = 0; i < size; i++) {
			if (i%2 == 0) {//ż��Ϊ��ʶ,����Ϊ0
				continue;
			}else {
				String event = (String) sequence.get(i);
				events.add(event);
			}
		}
		Trace trace = new Trace();
		trace.setEvents(events);
	    return trace;
		
	}
	
	@SuppressWarnings("rawtypes")
	public static List<String> genTransFromPath(Path path) {
		
		List<String> events = new ArrayList<String>();
		List sequence = path.getSequence();
		int size = sequence.size();
		for (int i = 0; i < size; i++) {
			if (i%2 == 0) {//ż��Ϊ��ʶ,����Ϊ0
				continue;
			}else {
				String event = (String) sequence.get(i);
				events.add(event);
			}
		}
	    return events;
		
	}
	
	
    /****************************��ȡ�޻�RG�кϷ�·��******************************/
	
	@SuppressWarnings({ "rawtypes"})
	public static List<Path> getLegalPaths(RG rg) {
		
		List<Path> legalPaths = new ArrayList<Path>();
		
		Marking start = rg.getStart();
		List<Marking> visitedQueue = new ArrayList<>();
		Stack route = new Stack();
		genLegalPaths(start, route, visitedQueue, legalPaths, rg);
		return legalPaths;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void genLegalPaths(Marking marking, Stack route, 
			List<Marking> visitedQueue, List<Path> legalPaths, RG rg) {
		
		visitedQueue.add(marking);
		route.add(marking);
		
		//�����marking��ʼ�ı�
		List<Edge> succEdges = getSuccEdges(marking, rg);
		
		if (succEdges.size() == 0) {//marking����Ǩ��
			
			// 1.����úϷ�·��(�轫route����,�����ڻ��ݹ����л����)
			// Note:marking��һ����ֹ��ʶ)
			if (MarkingUtils.markingIsExist(rg.getEnds(), marking)) {
				Path path = new Path();
				List sequence = new ArrayList<>();
				for (int j = 0; j < route.size(); j++) {
					sequence.add(route.get(j));
				}
				path.setSequence(sequence);
				legalPaths.add(path);
				System.out.println("size: " + legalPaths.size() + ", legal path: " + RGUtils.genTraceFromPath(path).getEvents());
			}
			
			// 2.����·��(��ֹԽ��)������Ϊδ����
			if (route.size() != 0) {
				route.pop();
				if (route.size() != 0) {
					//System.out.println("Back: " + rg.getTranLabelMap().get((String)route.peek()));
					route.pop();
				}
			}
			removeMarking(marking, visitedQueue);
		}else {//marking��Ǩ��
			int succSize = succEdges.size();
			for (int i = 0; i < succSize; i++) {//Note:ȷ������ȫ������
				Edge edge = succEdges.get(i);
				String tran = edge.getTran();
				Marking markingTo = edge.getTo();
				if (!MarkingUtils.markingIsExist(visitedQueue, markingTo)) {//markingToδ����
					route.add(tran);
	        		genLegalPaths(markingTo, route, visitedQueue, legalPaths, rg);
				}
				if (i == succSize - 1) {//�������������
					//����·��(��ֹԽ��)������Ϊδ����
					if (route.size() != 0) {
						route.pop();
						if (route.size() != 0) {
							//System.out.println("Back: " + rg.getTranLabelMap().get((String)route.peek()));
							route.pop();
						}
					}
					removeMarking(marking, visitedQueue);
				}
			}
		}
	}
	
	//��visitedQueue���Ƴ�marking
	public static void removeMarking(Marking marking, List<Marking> visitedQueue) {
		for (Marking visitedMarking : visitedQueue) {
			if (MarkingUtils.twoMarkingsIsEqual(marking, visitedMarking)) {
				visitedQueue.remove(visitedMarking);
				break;
			}
		}
	}

	//��ȡ��ʶmarking�����߼�
	public static List<Edge> getSuccEdges(Marking marking, RG rg) {
		List<Edge> succEdges = new ArrayList<Edge>();
		List<Edge> edges = rg.getEdges();
		for (Edge edge : edges) {
			Marking from = edge.getFrom();
			if (MarkingUtils.twoMarkingsIsEqual(marking, from)) {
				succEdges.add(edge);
			}
		}
		return succEdges;
		
	}
	
	
	/******************************���ɴ�ͼתΪLTS*******************************/
	
	public static LTS rg2lts(RG rg) {
		
		//����LTS��״̬���������б�ʶӳ��
		Map<String, Marking> stateMarkingMap = new HashMap<String, Marking>();
		
		LTS lts = new LTS();
		lts.setStart("S" + 0);
		
		List<String> ltsStates = new ArrayList<String>();
		List<Marking> vertexs = rg.getVertexs();
		int size = vertexs.size();
		for (int i = 0; i < size; i++) {
			String ltsState = "S" + i;
			ltsStates.add(ltsState);
			stateMarkingMap.put(ltsState, vertexs.get(i));
		}
		lts.setStates(ltsStates);
		lts.setStateMarkingMap(stateMarkingMap);
		
		List<LTSTran> ltsTrans = new ArrayList<LTSTran>();
		List<Edge> edges = rg.getEdges();
        for (Edge edge : edges) {
			Marking markingFrom = edge.getFrom();
			String tran = edge.getTran();
			Marking markingTo = edge.getTo();
			//ӳ��ΪS(i)
			int from = MarkingUtils.getIndex(vertexs, markingFrom);
			int to = MarkingUtils.getIndex(vertexs, markingTo);
			LTSTran tranLTS = new LTSTran();
			tranLTS.setFrom("S" + from);
			tranLTS.setTran(tran);
			tranLTS.setTo("S" + to);
			ltsTrans.add(tranLTS);
        }
        lts.setLTSTrans(ltsTrans);
        
        //������ֹ��ʶ��Ӧ��LTS״̬
        List<String> ends = new ArrayList<String>();
        List<Marking> finalMarkings = rg.getEnds();
        for (Marking finalMarking : finalMarkings) {
        	int index = MarkingUtils.getIndex(vertexs, finalMarking);
     		ends.add("S" + index);
		}
        lts.setEnds(ends);
        
        //lts.setTranLabelMap(rg.getTranLabelMap());
        
        return lts;
		
	}
	
	/**********************************��PIPE�����ɿɴ�ͼ***********************************/
	
	// 1.��PIPE�в���LTSͼ
 	public static void genGraphFromLTS(LTS lts, int orgNum, int placeNum, int tranNum, int inters){
 	    DefaultGraph graph = createGraphFromLTS(lts);
 	    GraphFrame frame = new GraphFrame();
 	    int nodeNum = lts.getStates().size();
 	    int edgeNum = lts.getLTSTrans().size();
 	    String legend = "orgNum: " + orgNum + "; placeNum: " + placeNum + "; tranNum: " + tranNum + "; inters: " + inters +"\n" +
 	    		 "The RG covers: " + nodeNum + " Nodes, " + edgeNum + " Edges";
 	    frame.constructGraphFrame(graph, legend);
 	    frame.toFront();
 	    frame.setIconImage(new ImageIcon(CreateGui.imgPath + "icon.png").getImage());
 	    frame.setTitle("The RG of" + " " + CreateGui.getTab().getTitleAt(CreateGui.getTab().getSelectedIndex()));
 	}
 	
 	//����LTSͼ
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private static DefaultGraph createGraphFromLTS(LTS lts){
 			
 			DefaultGraph graph = new DefaultGraph();
 			
 			//graph��㼯�ͱ߼�
 			ArrayList nodes = new ArrayList();
 			ArrayList edges = new ArrayList();
 			
 			//����ɴ�ͼ�еĽڵ�
 			List<String> states = lts.getStates();
 			// 1.rg�е�һ��״̬Ϊ��ʼ״̬
 			RGInitialState ltsInitialState = new RGInitialState("S0", lts.getStateMarkingMap().get("S0"));
 			nodes.add(ltsInitialState);
 			
 			// 2.�ӵڶ�����ʼ��Ϊ��ͨ״̬
 			for (int i = 1; i < states.size(); i++) {
 				//labelΪ Si
 				String label = states.get(i);
 				Marking tempMarking = lts.getStateMarkingMap().get(label);
 				RGGeneralState rgGeneralState = new RGGeneralState(label, tempMarking);
 				nodes.add(rgGeneralState);
 			}
 			
 			//����ɴ�ͼ�еı�
 			List<LTSTran> ltsTrans = lts.getLTSTrans();
             for (LTSTran ltsTran : ltsTrans) {
 				String from = ltsTran.getFrom();
 				//����t1_!order
 				String tran = ltsTran.getTran();
 				String to = ltsTran.getTo();
 				TextEdge textEdge = new TextEdge((RGNode)(nodes.get(getIndex(states, from))), (RGNode)(nodes.get(getIndex(states, to))), tran);
			    edges.add(textEdge);
					
 			 }
             graph.addElements(nodes, edges);
             
             return graph;
             
 	}
 	
 	public static int getIndex(List<String> states, String label) {
 		for (int i = 0; i < states.size(); i++) {
			if (states.get(i).equals(label)) {
				return i;
			}
		}
 		return -1;
		
	}
 	
 	// 2.��PIPE�в���RGͼ
 	public static void genGraphFromRG(RG rg, int orgNum, int placeNum, int tranNum, int inters){
 	    DefaultGraph graph = createGraphFromRG(rg);
 	    GraphFrame frame = new GraphFrame();
 	    int nodeNum = rg.getVertexs().size();
 	    int edgeNum = rg.getEdges().size();
 	    String legend = "orgNum: " + orgNum + "; placeNum: " + placeNum + "; tranNum: " + tranNum + "; inters: " + inters +"\n" +
 	    		 "The RG covers: " + nodeNum + " Nodes, " + edgeNum + " Edges";
 	    frame.constructGraphFrame(graph, legend);
 	    frame.toFront();
 	    frame.setIconImage(new ImageIcon(CreateGui.imgPath + "icon.png").getImage());
 	    frame.setTitle("The RG of" + " " + CreateGui.getTab().getTitleAt(CreateGui.getTab().getSelectedIndex()));
 	}
 	
 	//����RGͼ
 	@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
 	private static DefaultGraph createGraphFromRG(RG rg){
 			
 		    PetriUtils petriUtils = new PetriUtils();
 			DefaultGraph graph = new DefaultGraph();
 			
 			//graph��㼯�ͱ߼�
 			ArrayList nodes = new ArrayList();
 			ArrayList edges = new ArrayList();
 			
 			//����ɴ�ͼ�еĽڵ�
 			List<Marking> states = rg.getVertexs();
 			// 1.rg�е�һ��״̬Ϊ��ʼ״̬
 			RGInitialState ltsInitialState = new RGInitialState("S0", states.get(0));
 			nodes.add(ltsInitialState);
 			
 			// 2.�ӵڶ�����ʼ��Ϊ��ͨ״̬
 			for (int i = 1; i < states.size(); i++) {
 				//labelΪ Si
 				String label = "S" + i;
 				Marking tempMarking = states.get(i);
 				RGGeneralState rgGeneralState = new RGGeneralState(label, tempMarking);
 				nodes.add(rgGeneralState);
 			}
 			
 			//����ɴ�ͼ�еı�
 			List<Edge> rgTrans = rg.getEdges();
             for (Edge rgTran : rgTrans) {
 				Marking markingFrom = rgTran.getFrom();
 				//����t1_!order
 				String tran = /*rgTran.getTran() + "_" + */petriUtils.getLabel(rg.getTranLabelMap(), rgTran.getTran());
 				Marking markingTo = rgTran.getTo();
 				//ӳ��ΪSi
 				int from = MarkingUtils.getIndex(states, markingFrom);
 				int to = MarkingUtils.getIndex(states, markingTo);
 				//����ѭ��
 				if (from == to) {//�����ѭ����
 					RGLoopWithTextEdge loopWithTextEdge = new RGLoopWithTextEdge((RGNode)(nodes.get(from)), tran);
 					edges.add(loopWithTextEdge);
 				}else {//����Ƿ�ѭ����
 					TextEdge textEdge = new TextEdge((RGNode)(nodes.get(from)), (RGNode)(nodes.get(to)), tran);
 					edges.add(textEdge);
 				}
 			}
             graph.addElements(nodes, edges);
             
             return graph;
             
 	}


}
