package toolkits.utils.lts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moqi
 * ��ѡʹ�ܻ����
 */
public class OptEnableActivity {
	
	private List<EnableActivity> enableActivities;
	
	public OptEnableActivity() {
		enableActivities = new ArrayList<EnableActivity>();
	}

	public List<EnableActivity> getEnableActivities() {
		return enableActivities;
	}

	public void setEnableActivities(EnableActivity enableActivity) {
		enableActivities.add(enableActivity);
	}
	

}
