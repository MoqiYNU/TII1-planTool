package toolkits.utils.lts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moqi
 * 可选使能活动集合
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
