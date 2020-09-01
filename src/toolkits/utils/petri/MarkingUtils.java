package toolkits.utils.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import toolkits.def.petri.Marking;

public class MarkingUtils {
	
	//�ж�markings�Ƿ���ends���ڽ���
	public boolean isInter(List<Marking> markings, List<Marking> ends) {
		for (Marking marking : markings) {
			if (markingIsExist(ends, marking)) {
				return true;
			}
		}
		return false;
	}
	
	//�ж�2����ʶ�Ƿ����
	public static boolean twoMarkingsIsEqual(Marking marking1, Marking marking2) {
		if (CollectionUtils.isEqualCollection(marking1.getPlaces(), marking2.getPlaces())) {
			return true;
		}
		return false;
	}
	
	//��ȡ��ʶλ��
	public static int getIndex(List<Marking> markings, Marking marking) {
		int size = markings.size();
		List<String> places = marking.getPlaces();
		for (int i = 0; i < size; i++) {
			Marking tempMarking = markings.get(i);
			List<String> tempPlaces = tempMarking.getPlaces();
			if (CollectionUtils.isEqualCollection(places, tempPlaces)) {
				return i;
			}
		}
		return -1;
	}
	
	//�жϱ�ʶ�Ƿ��ѱ�����
	public static boolean markingIsExist(List<Marking> markings, Marking marking) {
		List<String> places = marking.getPlaces();
        for (Marking tempMarking : markings) {
			List<String> tempPlaces = tempMarking.getPlaces();
			if (CollectionUtils.isEqualCollection(places, tempPlaces)) {
				return true;
			}
		}
        return false;
	}
	
	//�ж�һ���ʶ�Ƿ����
	@SuppressWarnings("rawtypes")
	public static boolean markingsIsEqual(List<Marking> markings1, List<Marking> markings2) {
		
		if (markings1.size() != markings2.size()) {
			return false;
		}
		
		Map<Marking, Integer> countMap1 = getCardinalityMap(markings1);
		Map<Marking, Integer> countMap2 = getCardinalityMap(markings2);
        if (countMap1.size() != countMap2.size()){
        	return false;
        }
        
        Iterator it = countMap1.keySet().iterator();
        while (it.hasNext()) {
        	Marking marking = (Marking) it.next();
        	if (getCount(marking, countMap1) != getCount(marking, countMap2)) {
				return false;
			}
        }
        return true;
		
	}
	
	//ͳ��markings�б�ʶ����
	public static Map<Marking, Integer> getCardinalityMap(List<Marking> markings) {
    	Map<Marking, Integer> countMap = new HashMap<Marking, Integer>();
        for (Marking marking : markings) {
			int count = getCount(marking, countMap);
			if (count == -1) {
				countMap.put(marking, 1);
			}else {
				countMap.put(marking, count+1);
			}
		}
        return countMap;
        
    }
    
    //��ȡ��ʶmarking��Ӧcount
  	public static int getCount(Marking marking, Map<Marking, Integer> countMap) {
  		for (Entry<Marking, Integer> entry : countMap.entrySet()) { 
  			Marking tempMarking = entry.getKey();
  			int count = entry.getValue();
  			if (CollectionUtils.isEqualCollection(marking.getPlaces(), tempMarking.getPlaces())) {
  				return count;
  			}
  		}
  		return -1;
  	}
  	
    //��ȡmarkingӳ���ʶ
  	public static Marking getMapMarking(Marking marking, Map<Marking, Marking> markingMap) {
  		for (Entry<Marking, Marking> entry : markingMap.entrySet()) { 
  			Marking state = entry.getKey();
  			Marking tempMarking = entry.getValue();
  			if (CollectionUtils.isEqualCollection(marking.getPlaces(), state.getPlaces())) {
  				return tempMarking;
  			}
  		}
  		return null;
  	}
  	
    //��ȡmarkingӳ����ȹ̼�
  	public static List<String> getNoStubSetWithMarking(Marking marking, 
  			Map<Marking, List<String>> noStubSetMap) {
  		for(Map.Entry<Marking, List<String>> entry : noStubSetMap.entrySet()){
  		    Marking tempMarking = entry.getKey();
  		    if (twoMarkingsIsEqual(marking, tempMarking)) {
				return entry.getValue();
			}
  		}
		return null;
		
	}
	
	//��markingת��Ϊ�ַ�����ʽ
	public static String getStrMarking(Marking marking) {
		String strMarking = "";
		List<String> places = marking.getPlaces();
		for (String place : places) {
			strMarking = strMarking + place;
		}
		return strMarking;
	}
	
	//��marking������repPlaces�滻ΪobjPlace,��Ҫ���ڿ����ϲ�
	public static Marking replaceOne(List<String> repPlaces, String objPlace, Marking marking) {
		 Marking repMaking = new Marking();
         List<String> places = marking.getPlaces();
         for (String place : places) {
			if (repPlaces.contains(place)) {
				repMaking.addPlace(objPlace);
			}else {
				repMaking.addPlace(place);
			}
		}
        return repMaking;
	}
	
	//��markings������repPlaces�滻ΪobjPlace,��Ҫ���ڿ����ϲ�
	public static List<Marking> replaceMul(List<String> repPlaces, String objPlace, List<Marking> markings) {
		List<Marking> repMarkings = new ArrayList<Marking>();
		for (Marking marking : markings) {
			Marking repMaking = replaceOne(repPlaces, objPlace, marking);
			repMarkings.add(repMaking);
		}
		return repMarkings;
	}

}
