package toolkits.utils.file;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import toolkits.def.petri.Flow;
import toolkits.def.petri.Marking;
import toolkits.def.petri.ProNet;
import toolkits.utils.block.ProTree;
import toolkits.utils.plan.CompFrag;
import toolkits.utils.plan.Plan;
import toolkits.utils.plan.ORDMatrix;

/**
 * @author Moqi
 * 在控制台打印Utils
 */
public class PrintUtils {
	
	//打印矩阵
	public static void printGraph(int[][] graph) {
		for(int i = 0; i < graph.length; i++){  
			for(int j = 0; j < graph[i].length; j++){
				System.out.print(graph[i][j] + ", ");
			}
			System.out.println("\n");
		}
	}
	
	//打印计划集
	public static void printPlans(List<Plan> plans) {
        int index = 0;
        for (Plan plan : plans) {
			String planIndex = "Plan " + index;
			index++;
			System.out.println(planIndex + "...............................");
			List<ORDMatrix> frags = plan.getMatrices();
			for (ORDMatrix frag : frags) {
				System.out.println("events in frag: " + frag.getActs());
				printGraph(frag.getGraph());
			}
		}
	}
	
	//打印组合片段集
	public static void printCompFrag(List<CompFrag> set, List<ProNet> proNets) {
		
		System.out.println("print compFrags..................................." + "\n");
		DotUtils dotUtils = new DotUtils();
		for (int i = 0; i < set.size(); i++) {
			List<ProTree> frags = set.get(i).getFrags();
			for (int j = 0; j < frags.size(); j++) {
				ProTree proTree = frags.get(j);
				String name = "fg" + i + j;
				try {
					dotUtils.pt2Dot(proTree, proNets.get(j).getTranLabelMap(), name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("\n");
			System.out.println("frag: " + i + " end............................");
			System.out.println("\n");
		}
		
	}
	
	//打印过程网
	public static void printNet(ProNet proNet) {
		
		System.out.println("start....................................");
		
		System.out.println("Source: " + proNet.getSource().getPlaces());
		
		List<Marking> sinks = proNet.getSinks();
		for (Marking sink : sinks) {
			System.out.println("Sink: " + sink.getPlaces());
		}
		
		System.out.println("Places: " + proNet.getPlaces());
		
		System.out.println("MsgPlaces: " + proNet.getMsgPlaces());
		
		System.out.println("Trans: " + proNet.getTrans());
		
		List<Flow> flows = proNet.getFlows();
		for (Flow flow : flows) {
			System.out.println("Flow: " + flow.getFlowFrom() + " " + flow.getFlowTo());
		}
		
		Map<String, String> map = proNet.getTranLabelMap();
		for (Entry<String, String> entry : map.entrySet()) {//打印标号函数
			  System.out.println("Tran = " + entry.getKey() + ", Label = " + entry.getValue()); 
		}
		
	}

}
