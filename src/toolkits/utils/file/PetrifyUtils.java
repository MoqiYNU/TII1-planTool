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
 * ����Petrify��Utils
 */
public class PetrifyUtils {
	
	private FileUtils fileUtils;
	
	public PetrifyUtils() {
		fileUtils = new FileUtils();
	}
	
	/***************************����Petrify����״̬ͼ********************************/
	
    public void genStateGraph(String name) {
		
    	//����cmd����
		String path = "C:\\Users\\Moqi\\Documents\\Experiments\\petrify\\";
		String cmdStr = "cmd /c " + "petrify -dead -ip -efc " + path + name + ".g -o " + path + name + ".out";
		
		Runtime rt = Runtime.getRuntime();
		Process ps = null;
		try {
			ps = rt.exec(cmdStr);
			ps.waitFor();//Note:�ȴ��ӽ������������ִ��
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (ps != null) {//�����ӽ���
			ps.destroy();  
	 	    ps = null; 
		}
 	    
	}
	
	
	/************************���������п�����Э������ȷ����***************************/
    
    public ProNet determine(ProNet net, int index) {
		
    	ProNet deteNet = new ProNet();
    	
    	// 1.ȷ����source
    	Marking deteSource = new Marking();
    	Marking source = net.getSource();
    	List<String> sourcePlaces = source.getPlaces();
    	for (String sourcePlace : sourcePlaces) {
    		String deteSourcePlace = sourcePlace + "_" + index;
    		deteSource.addPlace(deteSourcePlace);
		}
    	
    	// 2.ȷ����sink
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
    	
    	// 3.ȷ��������(�ų���Ϣ����)
    	List<String> deteConds = new ArrayList<String>();
    	List<String> conds = net.getPlaces();
    	for (String cond : conds) {
    		String deteCond;
    		if (net.getMsgPlaces().contains(cond)) {//�ų���Ϣ����
				deteCond = cond;
			}else {
				deteCond = cond + "_" + index;
			}
			deteConds.add(deteCond);
		}
    	
    	// 4.ȷ����Э������(�ų�ԭҵ����������б�Ǩ)
    	List<String> deteActs = new ArrayList<String>();
    	List<String> acts = net.getTrans();
    	for (String act : acts) {
			if (act.length() > 4 && act.substring(0, 4).equals("sync")) {
				String deteAct = act + "_" + index;
				deteActs.add(deteAct);
			}else {//�ų����б�Ǩ
				deteActs.add(act);
			}
		}
    	
    	// 5.ȷ������(�ų���Ϣ����)
    	List<Flow> deteFlows = new ArrayList<Flow>();
    	List<Flow> flows = net.getFlows();
    	for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (net.getTrans().contains(from)) {//1)from�Ǳ�Ǩ
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
			}else {//2)from�ǿ���
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
    	
    	// 6.ȷ����ӳ��(�ų����б�Ǩ)
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
	
    
    /*************************����.out�ļ�Ϊ������*******************************/
    
	public ProNet genProNetFromSG(String fileName) {
		
		ProNet proNet = new ProNet();
		Marking source = new Marking();
		List<Marking> sinks = new ArrayList<Marking>();
		List<String> conds = new ArrayList<String>();
		List<String> acts = new ArrayList<String>();
		List<Flow> flows = new ArrayList<Flow>();
		
		File file = new File(fileName);  
        BufferedReader reader = null;  
        
        try {//����Ϊ��λ��ȡ�ļ�����,ÿ�ζ�һ���� 

            reader = new BufferedReader(new FileReader(file));  
            String tempStr = null;  
            //ÿ�ζ���һ��,ֱ������nullΪ�ļ�����  
            while ((tempStr = reader.readLine()) != null) {  
                
            	// 1.ȷ��source
            	if (tempStr.length() > 8 && tempStr.substring(0, 8).equals(".marking")) {
					int preBraIndex = tempStr.indexOf("{");
					int postBraIndex = tempStr.indexOf("}");
					String sourceStr = tempStr.substring(preBraIndex+2, postBraIndex-1);
					//��sourceStr���ո��з�
	        		String[] places = sourceStr.split(" ");
	        		if (places.length == 1) {//source��ֻ��1������
						source.addPlace(places[0]);
						proNet.setSource(source);
					}else {//source���ж������
						for (String place : places) {
							source.addPlace(place);
						}
						proNet.setSource(source);
					}
				}
            	
            	// 2.���������м�����ʱ��
            	List<Flow> tempFlows = new ArrayList<Flow>();//��ʱ��
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
            	
            	// 3.ȷ����ֹ��ʶ��������(�ų�end��)
            	List<String> visitedEnds = new ArrayList<String>();
                for (Flow tempFlow : tempFlows) {
					String to = tempFlow.getFlowTo();
					//3.1Ϊend��,��Ϊ��ʶLTS����ֹ״̬������
					if (to.length() > 3 && to.substring(0, 3).equals("end")) {
						if (visitedEnds.contains(to)) {//�Ѿ�����,������
							continue;
						}
						//��ȡtoǰ���Ӧ������,���1����ֹ��ʶ
						List<String> froms = getFromsByTo(to, tempFlows);
						Marking sink = new Marking();
						sink.addPlaces(froms);
						sinks.add(sink);
						visitedEnds.add(to);//��toΪ�ѷ���
					}else {//3.2��end��,��ֱ�������flow
						flows.add(tempFlow);
					}
				}
            	proNet.setSinks(sinks);
            	proNet.setFlows(flows);
            	
            	// 4.ȷ����Ǩ��
            	// 4.1��ȡδ���ѱ�ŵı�Ǩ
            	if (tempStr.length() > 6 && tempStr.substring(0, 6).equals(".dummy")) {
            		String transStr = tempStr.substring(8, tempStr.length());
            		String[] trans = transStr.split(" ");
            		for (String tran : trans) {
            			if (tran.length() > 3 && tran.substring(0, 3).equals("end")) {//�ų�end��Ǩ
            				continue;
            			}
						acts.add(tran);
					}
            	}
            	for (Flow flow : flows) {//4.2��ȡ���ѱ�ŵı�Ǩ
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
            	
            	// 5.ȷ��������
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
            //���ر�������
            reader.close();  
            
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
        return proNet;
        
	}
	
	//��ȡ����to��froms
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


	/*************************��LTSת��Ϊ.g�ļ�*******************************/
	
	public void lts2StateGraph(LTS lts, String name) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(".model " + name + "\n");
		
		String dummyStr = ".dummy ";
		// 1.�����ʶ��ֹ״̬�Ļ��
		List<String> ends = lts.getEnds();
		int endsSize = ends.size();
		for (int i = 0; i < endsSize; i++) {
			dummyStr = dummyStr + "end" + i + " ";
		}
		// 2.����LTS�л��
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
		// 3.�����ʶ��ֹ״̬��Ǩ�Ƽ�
		for (int i = 0; i < endsSize; i++) {
			String end = ends.get(i);
			String act = "end" + i;
			buffer.append(end + " " + act + " " + "End" + "\n");
		}
		// 4.����LTS��Ǩ�Ƽ�
        for (LTSTran tran : trans) {
			String from = tran.getFrom();
			String label = tran.getTran();
			String to = tran.getTo();
			buffer.append(from + " " + label + " " + to + "\n");
		}
        
        // 5.�����ʼ��ʶ
        buffer.append(".marking {" + lts.getStart() + "}" + "\n");
        buffer.append(".end" + "\n");
		
        //��.g�ļ�������Ӳ��
      	fileUtils.savePetrify(buffer, name);
		
	}
	
	//��ȡ���
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
