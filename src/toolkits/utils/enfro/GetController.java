package toolkits.utils.enfro;

import java.util.ArrayList;
import java.util.List;

import toolkits.def.lts.LTS;
import toolkits.def.lts.LTSTran;
import toolkits.def.petri.Composition;
import toolkits.def.petri.ProNet;
import toolkits.def.petri.RG;
import toolkits.utils.petri.PetriUtils;
import toolkits.utils.petri.RGUtils;

/**
 * @author Moqi
 * 产生控制器
 */
public class GetController {
	
	private MinLTS minLTS;
	private PruneUtils pruneUtils;
	private PetriUtils petriUtils;
	private Composition composition;
	
	public GetController() {
		minLTS = new MinLTS();
		pruneUtils = new PruneUtils();
		petriUtils = new PetriUtils();
		composition = new Composition();
	}
	
	//获得原过程对应的控制器集
	@SuppressWarnings("static-access")
	public List<LTS> generate(List<ProNet> proNets) {
		
		composition.setProNets(proNets);
		ProNet orgPro = composition.compose();
		
		RG rg = petriUtils.genRG(orgPro);
		System.out.println("RG: nodes: " + rg.getVertexs().size() + ", trans: " + rg.getEdges().size());
		LTS orgProLTS = RGUtils.rg2lts(rg);
		
		LTS core = pruneUtils.prune(orgProLTS);
		List<String> coodNames = pruneUtils.getCoodNames(orgProLTS, core);
				
		List<LTS> hideCores = genHideCores(proNets, core, coodNames);
		
		List<LTS> controllers = new ArrayList<>();
		
		int size = proNets.size();
		for (int i = 0; i < size; i++) {
			
			List<LTSTran> ctTrans = new ArrayList<>();
			LTS hideCore = hideCores.get(i);
			List<String> bizProNames = proNets.get(i).getTrans();
			
			LTS interCt = minLTS.min(hideCore, i);
			List<LTSTran> interTrans = interCt.getLTSTrans();
			
			int index = 0;
            for (LTSTran interTran : interTrans) {
				String label = interTran.getTran();
				if (!bizProNames.contains(label) && coodNames.contains(label)) {
					//1.不属于BP(i),但需协调*******************************
					String coodState = "Cor" + i + index;//Cor+业务过程位置+第几个协调状态
					index ++;
					
					LTSTran tempTran1 = new LTSTran();
					tempTran1.setFrom(interTran.getFrom());
					tempTran1.setTran("sync_1_" + label);
					tempTran1.setTo(coodState);
					
					LTSTran tempTran2 = new LTSTran();
					tempTran2.setFrom(coodState);
					tempTran2.setTran("sync_2_" + label);
					tempTran2.setTo(interTran.getTo());
					
					ctTrans.add(tempTran1);
					ctTrans.add(tempTran2);
				}else if (bizProNames.contains(label) && coodNames.contains(label)) {
					//2.属于BP(i)且需协调***********************************
					String coodState1 = "Cor" + i + index;//Cor+业务过程位置+第几个协调状态
					index ++;
					//Note: 必须先增加sync+label,然后再增加label
					LTSTran tran1 = new LTSTran();
					tran1.setFrom(interTran.getFrom());
					tran1.setTran("sync_1_" + label); 
					tran1.setTo(coodState1);
					
					String coodState2 = "Cor" + i + index;//Cor+业务过程位置+第几个协调状态
					index ++;
					
					LTSTran tran2 = new LTSTran();
					tran2.setFrom(coodState1);
					tran2.setTran(label);
					tran2.setTo(coodState2);
					
					LTSTran tran3 = new LTSTran();
					tran3.setFrom(coodState2);
					tran3.setTran("sync_2_" + label); 
					tran3.setTo(interTran.getTo());
					
					ctTrans.add(tran1);
					ctTrans.add(tran2);
					ctTrans.add(tran3);
				}else {
					//3.其余的保持不变
					ctTrans.add(interTran); 
				}
			}
            
            List<String> ctStates = new ArrayList<String>();
            for (LTSTran tran : ctTrans) {
				String from = tran.getFrom();
				String to = tran.getTo();
				if (!ctStates.contains(from)) {
					ctStates.add(from);
				}
				if (!ctStates.contains(to)) {
					ctStates.add(to);
				}
			}
            
            LTS controller = new LTS();
			controller.setStart(interCt.getStart());
			controller.setEnds(interCt.getEnds());
			controller.setStates(ctStates);
			controller.setLTSTrans(ctTrans);
			controllers.add(controller);
			
		}
		
		return controllers;
			
	}
	
	//获取隐藏核(即将每个业务过程中除自身名字集及协调名字外的名字隐藏)
	public List<LTS> genHideCores(List<ProNet> proNets, LTS core, 
			List<String> coodNames) {
		
		List<LTS> hideCores = new ArrayList<>();//返回隐藏核
		
		String initState = core.getStart();
		List<String> ends = core.getEnds();
		List<String> states = core.getStates();
		List<LTSTran> trans = core.getLTSTrans();
		
        for (ProNet proNet : proNets) {
			
        	List<String> visibleName = new ArrayList<>();
        	List<String> bizProNames = proNet.getTrans();
        	//协调名字和自身名字不能隐藏
        	visibleName.addAll(coodNames);
        	visibleName.addAll(bizProNames);
        	
        	List<LTSTran> tempTrans = new ArrayList<>();
        	for (LTSTran tran : trans) {
        		String label = tran.getTran();
        		//将label按'_'切分(若为同步合并变迁,则形如:T1_T2_T3)
        		String[] syncLabels = label.split("\\_");
        		//1)label不是同步合并迁移
        		if (syncLabels.length == 1) {
        			if (!visibleName.contains(label)) {
        				//Note:需要建立临时LTSTran(不能直接更新)
        				LTSTran tempTran = new LTSTran();
            			tempTran.setFrom(tran.getFrom());
            			tempTran.setTran("tau");
            			tempTran.setTo(tran.getTo());
            			tempTrans.add(tempTran);
            		}else {
    					tempTrans.add(tran);
    				}
				}else if (syncLabels.length > 1) {//2)label是同步合并迁移
					if (!isContained(bizProNames, syncLabels)) {
						//Note:需要建立临时LTSTran(不能直接更新)
						LTSTran tempTran = new LTSTran();
            			tempTran.setFrom(tran.getFrom());
            			tempTran.setTran("tau");
            			tempTran.setTo(tran.getTo());
            			tempTrans.add(tempTran);
            		}else {
    					tempTrans.add(tran);
    				}
				}
    		}
        	LTS tempLTS = new LTS();
        	tempLTS.setStart(initState);
        	tempLTS.setEnds(ends);
        	tempLTS.setStates(states);
        	tempLTS.setLTSTrans(tempTrans);
        	hideCores.add(tempLTS);
		}
        return hideCores;
		
	}
	
	//visibleName是否涉及同步活动label
	public boolean isContained(List<String> bizProNames, String[] syncLabels) {
		for (String syncLabel : syncLabels) {
			if (bizProNames.contains(syncLabel)) {
				return true;
			}
		}
		return false;
	}
	
}
