package toolkits.utils.file;

import java.util.List;
import java.util.Map;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;
import toolkits.def.petri.Edge;
import toolkits.def.petri.Flow;
import toolkits.def.petri.Marking;
import toolkits.def.petri.ProNet;
import toolkits.def.petri.RG;
import toolkits.utils.block.InnerNet;
import toolkits.utils.block.Node;
import toolkits.utils.block.ProTree;
import toolkits.utils.petri.MarkingUtils;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * 定义绘图工具GraphViz的Utils
 */
public class DotUtils {
	
	private FileUtils fileUtils;
	private PetriUtils petriUtils;
	
	public DotUtils() {
		fileUtils = new FileUtils();
		petriUtils = new PetriUtils();
	}
	
    /***************************将过程树转换为Dot文件*********************************/
	
	public void pt2Dot(ProTree pt, Map<String, String> map, String name) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("digraph pt {" + "\n");
		
		//Note:关键是这一行代码,不是孩子节点不按顺序排列
		buffer.append("\n");
		buffer.append("graph [ordering = " + "\"" + "out" + "\"" + "];" + "\n");
		buffer.append("\n");
		
		List<Node> nodes = pt.getNodes();
		
		for (Node node : nodes) {
			
			String type = node.getType();
			if (type.equals("leaf")) {
				//Mrecord是圆角矩形,"[shape = Mrecord, color = black, style = filled, fillcolor = skyblue];"
				buffer.append( "\"" + map.get(node.getIdf()) + "\"" + "[shape = Mrecord, color = black, fontname=" + "\"" + "Arial" + "\"" + "];" + "\n");
			}else {
				buffer.append(node.getIdf() + "[shape = rectangle, color = red, fontname=" + "\"" + "Arial" + "\"" + "];" + "\n");
			}
			List<Node> chaNodes = node.getChaNodes();
			for (Node chaNode : chaNodes) {
				if (chaNode.getType().equals("leaf")) {
					String edge = node.getIdf() + "->" + "\"" + map.get(chaNode.getIdf()) + "\""  + ";";
					buffer.append(edge + "\n");
				}else {
					String edge = node.getIdf() + "->" + chaNode.getIdf() + ";";
					buffer.append(edge + "\n");
				}
		   }
		   buffer.append("\n");
			
		}
		
		buffer.append("\n");
		buffer.append("\"PT:" + " " + name + "\" [shape = plaintext];" + "\n");
		buffer.append("\n");
        buffer.append("}");
        
		//将dot文件保存至硬盘
		fileUtils.savePT(buffer, name);
		
	}
	
	/***************************将开放网转换为Dot文件*********************************/
	
	public void ipn2Dot(InnerNet net, String name) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("digraph pt {" + "\n");
		
		//图形从左至右排列
		
		List<String> places = net.getPlaces();
        for (String place : places) {
        	//Note: place中可能含有'.',因此将其转为字符串,即"\"" + place  + "\""
    		buffer.append("\"" + place  + "\"" + "[shape = circle, fontname = " 
    	                            + "\"" + "Dialog" + "\"" + ", "
    				                + "fontsize = 8" +"];" + "\n");
		}
        
        List<String> trans = net.getTrans();
        for (String tran : trans) {//变迁(用label标识)
        	//Note: tran中可能含有'.'和'/',因此将其转为字符串,即"\"" + tran  + "\""
        	buffer.append("\"" + tran  + "\"" + "[shape = rectangle, fontname = " 
                    + "\"" + "Dialog" + "\"" + ", "
	                + "fontsize = 8" +"];" + "\n");
        			
		}
        
        List<Flow> flows = net.getFlows();
        for (Flow flow : flows) {
        	String from = flow.getFlowFrom();
        	String to = flow.getFlowTo();
        	//Note:place和tran均转为字符串,故from和to也需转为字符串
        	String node = "\"" + from  + "\"" + "->" + "\"" + to  + "\"" + ";" + "\n";
			buffer.append(node + "\n");
		}
        
        buffer.append("\"" + name + "\" [shape = plaintext];" + "\n");
		buffer.append("}");
		
		//将dot文件保存至硬盘
		fileUtils.saveProNet(buffer, name);
				
	}
	
	@SuppressWarnings("static-access")
	public void pn2Dot(ProNet net, String name) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("digraph pt {" + "\n");
		
		//图形从左至右排列
		
		List<String> places = net.getPlaces();
        for (String place : places) {
        	if (!net.getMsgPlaces().contains(place)) {//普通库所设置为圆圈
        		//Note: place中可能含有'.',因此将其转为字符串,即"\"" + place  + "\""
        		buffer.append("\"" + place  + "\"" + "[shape = circle, fontname = " 
        	                            + "\"" + "Dialog" + "\"" + ", "
        				                + "fontsize = 8" +"];" + "\n");
			}else {//消息库所设置为暗红填充
				buffer.append("\"" + place  + "\"" + "[shape = circle, style = filled, color =" 
			                            + "\"" + "chartreuse3" 
			                            + "\"" + ", " + "fontname = " + "\"" 
			                            + "Dialog" + "\"" + ", "
						                + "fontsize = 8" +"];" + "\n");
			}
		}
        
        List<String> trans = net.getTrans();
        for (String tran : trans) {//变迁(用label标识)
        	//Note: tran中可能含有'.'和'/',因此将其转为字符串,即"\"" + tran  + "\""
        	String label =  petriUtils.getLabel(net.getTranLabelMap(), tran);
        	buffer.append("\"" + tran  + "\"" + "[shape = rectangle, label = " 
        	                       + "\"" + label + "\"" + ", " + "fontname = " 
        			               + "\"" + "Dialog" + "\"" + ", " 
        	                       + "fontsize = 8" +"];" + "\n");
		}
        
        List<Flow> flows = net.getFlows();
        for (Flow flow : flows) {
        	String from = flow.getFlowFrom();
        	String to = flow.getFlowTo();
        	//Note:place和tran均转为字符串,故from和to也需转为字符串
        	String node = "\"" + from  + "\"" + "->" + "\"" + to  + "\"" + ";" + "\n";
			buffer.append(node + "\n");
		}
        
        buffer.append("\"" + name + "\" [shape = plaintext];" + "\n");
		buffer.append("}");
		
		//将dot文件保存至硬盘
		fileUtils.saveProNet(buffer, name);
				
	}

	/***************************将RG转换为Dot文件*********************************/
	
	public void rg2Dot(RG rg, String name) throws Exception {
		 
		 StringBuffer buffer = new StringBuffer();
		 buffer.append("digraph lts {" + "\n");
		 
		 buffer.append("rankdir=\"LR\";" + "\n");//图形的大小及从左至右排列
		 buffer.append("node [shape=circle];" + "\n");//节点形状为圆圈
		 
		 //设置初始状态的样式
		 buffer.append("S0" + "[style = filled, color=lightgrey];" + "\n");
		 
		 List<Marking> vertexs = rg.getVertexs();
		 
		 List<Marking> ends = rg.getEnds();
		 for (Marking end : ends) {
			int index = MarkingUtils.getIndex(vertexs, end);
			String endStr = "S" + index;
			buffer.append(endStr + "[shape = doublecircle];" + "\n");
		 }
		 
		 List<Edge> edges = rg.getEdges();
		 for (Edge edge : edges) {
			Marking markingFrom = edge.getFrom();
			String from = "S" + MarkingUtils.getIndex(vertexs, markingFrom);
			String tran = edge.getTran();
			Marking markingTo = edge.getTo();
			String to = "S" + MarkingUtils.getIndex(vertexs, markingTo);
			String idLabel =  tran + ": " + rg.getTranLabelMap().get(tran);
			String edgeStr = from + "->" + to + "[label=" + "\"" + idLabel + "\"" + "];";
			buffer.append(edgeStr + "\n");
		 }
		 buffer.append("\"LTS:" + " " + name + "\" [shape = plaintext];" + "\n");
		 buffer.append("}");
			
		 //将dot文件保存至硬盘
		 fileUtils.saveLTS(buffer, name);
		 
	 }
	
	
	/***************************将LTS转换为Dot文件*********************************/
	
    public void lts2Dot(LTS lts, String name) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("digraph lts {" + "\n");
		
		
		buffer.append("rankdir=\"LR\";" + "\n");//图形的大小及从左至右排列
		buffer.append("node [shape=circle];" + "\n");//节点形状为圆圈
		
		//设置初始状态的样式
		String initState = lts.getStart();
		buffer.append(initState + "[style = filled, color=lightgrey];" + "\n");
		
		//设置终止状态的样式
		List<String> ends = lts.getEnds();
		for (String end : ends) {
			buffer.append(end + "[shape = doublecircle];" + "\n");
		}
		
		//对每个变迁进行转换
		List<LTSTran> transitions = lts.getLTSTrans();
		for (LTSTran tran : transitions) {
			String from = tran.getFrom();
			String id = tran.getTran();
			String to = tran.getTo();
			String idLabel =  id;
			String node = from + "->" + to + "[label=" + "\"" + idLabel + "\"" + "];";
			buffer.append(node + "\n");
		}
		buffer.append("\"LTS:" + " " + name + "\" [shape = plaintext];" + "\n");
		buffer.append("}");
		
		//将dot文件保存至硬盘
		fileUtils.saveLTS(buffer, name);
		
	}
    

}
