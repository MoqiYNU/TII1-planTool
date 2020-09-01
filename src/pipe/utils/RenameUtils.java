package pipe.utils;

import pipe.gui.CreateGui;

/**
 * @author Moqi
 * �ж�Tab�����Ƿ��ظ�
 */
public class RenameUtils {
	
	//�ж�Tab�����Ƿ��ظ�
	public static boolean isDuplicatedTabName(String name){
		int size = CreateGui.getTab().getTabCount();
        for (int i = 0; i < size; i++) {
			String tempName = CreateGui.getTab().getTitleAt(i);
			if (name.equals(tempName)) {
				return true;
			}
	    }
        return false;
	}

}
