package toolkits.utils.lts;

import java.util.List;

/**
 * @author Moqi
 * ���尴�������ֻ��ֵ�ʹ�ܻ��,����:
 * 1)��������name;
 * 2)����name��Ӧʹ�ܻ��;
 */
public class EnableActivityParByName {
	
	private String name;
	private List<EnableActivity> enableActivities;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<EnableActivity> getEnableActivities() {
		return enableActivities;
	}
	public void setEnableActivities(List<EnableActivity> enableActivities) {
		this.enableActivities = enableActivities;
	}
	
	

}
