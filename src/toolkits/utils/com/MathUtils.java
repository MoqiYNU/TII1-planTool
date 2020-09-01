package toolkits.utils.com;

import java.util.ArrayList;
import java.util.List;
import toolkits.def.petri.Path;

/**
 * @author Moqi
 * 定义一些数学Utils
 */
public class MathUtils {

	
	//计算路径集合的笛卡尔积****************************************************
	
	public static List<List<Path>> descPath(List<List<Path>> sets) {
		
		int size = sets.size();
		if (size == 1) {//1.只有一个集合
			return sets;
		}else {//2.至少有两个集合
			List<List<Path>> set = composeFirstTwoPathSet(sets.get(0), sets.get(1));
			for (int i = 2; i < size; i++) {
				set = composeTwoPathSet(set, sets.get(i));
			}
			return set;
		}
		
	}
	
	//计算前面两个路径集合的笛卡尔积
	private static List<List<Path>> composeFirstTwoPathSet(List<Path> list1,
			List<Path> list2) {
		
		 List<List<Path>> result = new ArrayList<List<Path>>();
		 for (int i = 0; i < list1.size(); i++) {
			 Path elem1 = list1.get(i);
			 //System.out.println("elem1 :" + RGUtils.genTransFromPath(elem1));
	         for (int j = 0; j < list2.size(); j++) {
	        	 Path elem2 = list2.get(j);
	        	 //System.out.println("elem2 :" + RGUtils.genTransFromPath(elem2));
	        	 List<Path> product = new ArrayList<>();
				 product.add(elem1);
				 product.add(elem2);
				 result.add(product);
	         }
		 }
		return result;
		
	}

	private static List<List<Path>> composeTwoPathSet(List<List<Path>> set, List<Path> list) {
		
		 List<List<Path>> result = new ArrayList<List<Path>>();
		 for (int i = 0; i < set.size(); i++) {
			 List<Path> tempList = set.get(i);
             for (int j = 0; j < list.size(); j++) {
            	 Path elem = list.get(j);
            	 List<Path> product = new ArrayList<>();
				 product.addAll(tempList);
				 product.add(elem);
				 result.add(product);
             }
		 }
		return result;
		
	}
	
}
