package toolkits.utils.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;
import toolkits.def.petri.Flow;
import toolkits.def.petri.Marking;
import toolkits.def.petri.ProNet;

/**
 * @author Moqi
 * 定义Petrify的Utils
 */
public class PetrifyUtils {
	
	private FileUtils fileUtils;
	
	public PetrifyUtils() {
		fileUtils = new FileUtils();
	}
	
	/***************************利用Petrify生成状态图********************************/
	
    public void genStateGraph(String name) {
		
    	//定义cmd命令
		String path = "C:\\Users\\Moqi\\Documents\\Experiments\\petrify\\";
		String cmdStr = "cmd /c " + "petrify -dead -ip -efc " + path + name + ".g -o " + path + name + ".out";
		
		Runtime rt = Runtime.getRuntime();
		Process ps = null;
		try {
			ps = rt.exec(cmdStr);
			ps.waitFor();//Note:等待子进程完成再往下执行
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (ps != null) {//销毁子进程
			ps.destroy();  
	 	    ps = null; 
		}
 	    
	}
	
	
	/************************将过程网中库所和协调因子确定化***************************/
    
    public ProNet determine(ProNet net, int index) {
		
    	ProNet deteNet = new ProNet();
    	
    	// 1.确定化source
    	Marking deteSource = new Marking();
    	Marking source = net.getSource();
    	List<String> sourcePlaces = source.getPlaces();
    	for (String sourcePlace : sourcePlaces) {
    		String deteSourcePlace = sourcePlace + "_" + index;
    		deteSource.addPlace(deteSourcePlace);
		}
    	
    	// 2.确定化sink
    	List<Marking> deteSinks = new ArrayList<Marking>();
    	List<Marking> sinks = net.getSinks();
    	for (Marking sink : sinks) {
    		Marking deteSink = new Marking();
        	List<String> sinkPlaces = sink.getPlaces();
        	for (String sinkPlace : sinkPlaces) {
        		String deteSinkPlace = sinkPlace + "_" + index;
        		deteSink.addPlace(deteSinkPlace);
    		}
        	deteSinks.add(deteSink);
		}
    	
    	// 3.确定化库所(排除消息库所)
    	List<String> deteConds = new ArrayList<String>();
    	List<String> conds = net.getPlaces();
    	for (String cond : conds) {
    		String deteCond;
    		if (net.getMsgPlaces().contains(cond)) {//排除消息库所
				deteCond = cond;
			}else {
				deteCond = cond + "_" + index;
			}
			deteConds.add(deteCond);
		}
    	
    	// 4.确定化协调因子(排除原业务过程中已有变迁)
    	List<String> deteActs = new ArrayList<String>();
    	List<String> acts = net.getTrans();
    	for (String act : acts) {
			if (act.length() > 4 && act.substring(0, 4).equals("sync")) {
				String deteAct = act + "_" + index;
				deteActs.add(deteAct);
			}else {//排除已有变迁
				deteActs.add(act);
			}
		}
    	
    	// 5.确定化流(排除消息库所)
    	List<Flow> deteFlows = new ArrayList<Flow>();
    	List<Flow> flows = net.getFlows();
    	for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (net.getTrans().contains(from)) {//1)from是变迁
				String deteFrom;
				if (from.length() > 4 && from.substring(0, 4).equals("sync")) {
					deteFrom = from + "_" + index;
				}else {
					deteFrom = from;
				}
				String deteTo;
				if (net.getMsgPlaces().contains(to)) {
					deteTo = to;
				}else {
					deteTo = to + "_" + index;
				}
				Flow deteFlow = new Flow();
				deteFlow.setFlowFrom(deteFrom);
				deteFlow.setFlowTo(deteTo);
				deteFlows.add(deteFlow);
			}else {//2)from是库所
				String deteFrom;
				if (net.getMsgPlaces().contains(from)) {
					deteFrom = from;
				}else {
					deteFrom = from + "_" + index;
				}
				String deteTo;
				if (to.length() > 4 && to.substring(0, 4).equals("sync")) {
					deteTo = to + "_" + index;
				}else {
					deteTo = to;
				}
				Flow deteFlow = new Flow();
				deteFlow.setFlowFrom(deteFrom);
				deteFlow.setFlowTo(deteTo);
				deteFlows.add(deteFlow);
			}
		}
    	
    	// 6.确定化映射(排除已有变迁)
    	Map<String, String> deteTranLabelMap = new HashMap<String, String>();
    	Map<String, String> tranLabelMap = net.getTranLabelMap();
    	for (Entry<String, String> entry : tranLabelMap.entrySet()) {
    		 String key = entry.getKey();
    		 String value = entry.getValue();
    		 String deteKey;
    		 if (key.length() > 4 && key.substring(0, 4).equals("sync")) {
  				deteKey = key + "_" + index;
  			}else {
  				deteKey = key;
  			}
    		 deteTranLabelMap.put(deteKey, value);
    	}
    	
    	deteNet.setSource(deteSource);
    	deteNet.setSinks(deteSinks); 
    	deteNet.setPlaces(deteConds);
    	deteNet.setTrans(deteActs);
    	deteNet.setFlows(deteFlows);
    	deteNet.setMsgPlaces(net.getMsgPlaces());
    	deteNet.setTranLabelMap(deteTranLabelMap);
    	return deteNet;
    	
	}
	
    
    /*************************解析.out文件为过程网*******************************/
    
	public ProNet genProNetFromSG(String fileName) {
		
		ProNet proNet = new ProNet();
		Marking source = new Marking();
		List<Marking> sinks = new ArrayList<Marking>();
		List<String> conds = new ArrayList<String>();
		List<String> acts = new ArrayList<String>();
		List<Flow> flows = new ArrayList<Flow>();
		
		File file = new File(fileName);  
        BufferedReader reader = null;  
        
        try {//以行为单位读取文件内容,每次读一整行 

            reader = new BufferedReader(new FileReader(file));  
            String tempStr = null;  
            //每次读入一行,直到读入null为文件结束  
            while ((tempStr = reader.readLine()) != null) {  
                
            	// 1.确定source
            	if (tempStr.length() > 8 && tempStr.substring(0, 8).equals(".marking")) {
					int preBraIndex = tempStr.indexOf("{");
					int postBraIndex = tempStr.indexOf("}");
					String sourceStr = tempStr.substring(preBraIndex+2, postBraIndex-1);
					//将sourceStr按空格切分
	        		String[] places = sourceStr.split(" ");
	        		if (places.length == 1) {//source中只有1个库所
						source.addPlace(places[0]);
						proNet.setSource(source);
					}else {//source中有多个库所
						for (String place : places) {
							source.addPlace(place);
						}
						proNet.setSource(source);
					}
				}
            	
            	// 2.根据流进行计算临时流
            	List<Flow> tempFlows = new ArrayList<Flow>();//临时流
            	if (!tempStr.substring(0, 1).equals(".") && !tempStr.substring(0, 1).equals("#")) {
            		String[] elems = tempStr.split(" ");
            		if (elems.length == 2) {
						Flow flow = new Flow();
						flow.setFlowFrom(elems[0]);
						flow.setFlowTo(elems[1]);
						tempFlows.add(flow);
					}else {
						String from = elems[0];
						for (int i = 1; i < elems.length; i++) {
							Flow flow = new Flow();
							flow.setFlowFrom(from);
 							flow.setFlowTo(elems[i]);
							tempFlows.add(flow);
						}
					}
				}
            	
            	// 3.确定终止标识集及流集(排除end流)
            	List<String> visitedEnds = new ArrayList<String>();
                for (Flow tempFlow : tempFlows) {
					String to = tempFlow.getFlowTo();
					//3.1为end流,即为标识LTS中终止状态引入流
					if (to.length() > 3 && to.substring(0, 3).equals("end")) {
						if (visitedEnds.contains(to)) {//已经访问,则跳过
							continue;
						}
						//获取to前面对应库所集,组成1个终止标识
						List<String> froms = getFromsByTo(to, tempFlows);
						Marking sink = new Marking();
						sink.addPlaces(froms);
						sinks.add(sink);
						visitedEnds.add(to);//置to为已访问
					}else {//3.2非end流,则直接添加至flow
						flows.add(tempFlow);
					}
				}
            	proNet.setSinks(sinks);
            	proNet.setFlows(flows);
            	
            	// 4.确定变迁集
            	// 4.1获取未分裂标号的变迁
            	if (tempStr.length() > 6 && tempStr.substring(0, 6).equals(".dummy")) {
            		String transStr = tempStr.substring(8, tempStr.length());
            		String[] trans = transStr.split(" ");
            		for (String tran : trans) {
            			if (tran.length() > 3 && tran.substring(0, 3).equals("end")) {//排除end变迁
            				continue;
            			}
						acts.add(tran);
					}
            	}
            	for (Flow flow : flows) {//4.2获取分裂标号的变迁
            		String from = flow.getFlowFrom();
        			String to = flow.getFlowTo();
        			if (from.contains("/")) {
        				if (!acts.contains(from)) {
        					acts.add(from);
						}
					}
        			if (to.contains("/")) {
        				if (!acts.contains(to)) {
        					acts.add(to);
						}
					}
            	}
            	proNet.setTrans(acts);
            	
            	// 5.确定库所集
            	for (Flow flow : flows) {
            		String from = flow.getFlowFrom();
        			String to = flow.getFlowTo();
        			if (acts.contains(from)) {
        				if (!conds.contains(to)) {
        					conds.add(to);
						}
					}
        			if (acts.contains(to)) {
        				if (!conds.contains(from)) {
        					conds.add(from);
						}
					}
            	}
                proNet.setPlaces(conds);
            	
            } 
            //最后关闭输入流
            reader.close();  
            
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
        return proNet;
        
	}
	
	//获取到达to的froms
	private List<String> getFromsByTo(String to, List<Flow> tempFlows) {
		List<String> froms = new ArrayList<String>();
		for (Flow tempFlow : tempFlows) {
			String tempFrom = tempFlow.getFlowFrom();
			String tempTo = tempFlow.getFlowTo();
			if (tempTo.equals(to)) {
				froms.add(tempFrom);
			}
		}
		return froms;
	}


	/*************************将LTS转换为.g文件*******************************/
	
	public void lts2StateGraph(LTS lts, String name) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(".model " + name + "\n");
		
		String dummyStr = ".dummy ";
		// 1.定义标识终止状态的活动集
		List<String> ends = lts.getEnds();
		int endsSize = ends.size();
		for (int i = 0; i < endsSize; i++) {
			dummyStr = dummyStr + "end" + i + " ";
		}
		// 2.定义LTS中活动集
		List<LTSTran> trans = lts.getLTSTrans();
		List<String> acts = getActs(trans);
		int index = 0;
		for (String act : acts) {
			if (index == acts.size()-1) {
				dummyStr = dummyStr + act + "\n";
			}else {
				dummyStr = dummyStr + act + " ";
			}
			index ++;
		}
		buffer.append(dummyStr);
		
		buffer.append(".state graph" + "\n");
		// 3.定义标识终止状态的迁移集
		for (int i = 0; i < endsSize; i++) {
			String end = ends.get(i);
			String act = "end" + i;
			buffer.append(end + " " + act + " " + "End" + "\n");
		}
		// 4.定义LTS中迁移集
        for (LTSTran tran : trans) {
			String from = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			buffer.append(from + " " + label + " " + to + "\n");
		}
        
        // 5.定义初始标识
        buffer.append(".marking {" + lts.getStart() + "}" + "\n");
        buffer.append(".end" + "\n");
		
        //将.g文件保存至硬盘
      	fileUtils.savePetrify(buffer, name);
		
	}
	
	//获取活动集
	public List<String> getActs(List<LTSTran> trans) {
		List<String> acts = new ArrayList<String>();
		for (LTSTran tran : trans) {
			if (!acts.contains(tran.getTran())) {
				acts.add(tran.getTran());
			}
		}
		return acts;
	}

}
