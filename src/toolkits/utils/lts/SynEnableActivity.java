package toolkits.utils.lts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moqi
 * 同步使能活动集合
 */
public class SynEnableActivity {

	private List<EnableActivity> enableActivities;
	
	public SynEnableActivity() {
		enableActivities = new ArrayList<EnableActivity>();
	}

	public List<EnableActivity> getEnableActivities() {
		return enableActivities;
	}

	public void setEnableActivities(EnableActivity enableActivity) {
		enableActivities.add(enableActivity);
	}
	
	public void setEnableActivities(List<EnableActivity> activities) {
		enableActivities.addAll(activities);
	}
	
}
