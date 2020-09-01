package toolkits.utils.petri;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import toolkits.def.petri.Flow;
import toolkits.utils.block.InnerNet;

/**
 * @author Moqi
 * ��PNML����������(����������,��������Ϣ���������ڲ��Խṹ�ֽ�)
 */
public class ParsePetri {
	
	@SuppressWarnings("unchecked")
	public InnerNet parse(String filePath) throws Exception {
		
		//����������
		InnerNet interNet = new InnerNet();
		List<String> places = new ArrayList<String>();
		List<String> trans = new ArrayList<String>();
		List<Flow> flows = new ArrayList<Flow>();
		Map<String, String> tranLabelMap = new HashMap<String, String>();
		
		//Dom4j��ʼ���ĵ�
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(new File(filePath));
		
		//��ȡ��Ԫ��,��pnml
        Element root = document.getRootElement();
        
        //��ȡ��Ԫ��,��net
        Element netElem = root.element("net");
        
        //��ȡ��Ԫ���п���Ԫ��
        List<Element> placeElems = netElem.elements("place");
        for (Element placeElem : placeElems) {
        	
        	//��ÿ���
        	String placeId = placeElem.attributeValue("id");
        	
        	Element msgPlaceElem = placeElem.element("msgPlace");
        	String msgPlaceText = msgPlaceElem.getText();
        	if (msgPlaceText.equals("true")) {//�ų���Ϣ����
				continue;
			}
        	//����,�������������
        	places.add(placeId);
		}
        
        //��ȡ��Ԫ���б�ǨԪ��
        List<Element> tranElems = netElem.elements("transition");
        for (Element tranElem : tranElems) {
        	//��ñ�Ǩ
        	String tranId = tranElem.attributeValue("id");
        	Element nameElem = tranElem.element("name");
        	Element textElem = nameElem.element("value");
        	String tranName = textElem.getText();
        	trans.add(tranId);
        	//System.out.println("tranId: " + tranId + " tranName: " + tranName);
        	tranLabelMap.put(tranId, tranName);
		}
		
        //��ȡ��Ԫ����arcԪ��
        List<Element> arcElems = netElem.elements("arc");
        for (Element arcElem : arcElems) {
        	//��ñ�Ǩ
        	String source = arcElem.attributeValue("source");
        	String target = arcElem.attributeValue("target");
        	Flow flow = new Flow();
        	flow.setFlowFrom(source);
        	flow.setFlowTo(target);
        	flows.add(flow);
		}
        
        //����petri��������
        interNet.setPlaces(places);
        interNet.setTrans(trans);
        interNet.setFlows(flows);
        interNet.setTranLabelMap(tranLabelMap);
		return interNet;
		
	}
	
	
}
