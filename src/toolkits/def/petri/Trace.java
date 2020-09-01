package toolkits.def.petri;

import java.util.List;

/**
 * @author Moqi
 * ����켣
 */
public class Trace {
	
	private List<String> events;
	
	public List<String> getEvents() {
		return events;
	}

	public void setEvents(List<String> events) {
		this.events = events;
	}
	
	//�жϹ켣�Ƿ����
	public static boolean isEventTraceExist(List<Trace> eventTraces, Trace eventTrace) {
		for (Trace tempEventTrace : eventTraces) {
			if (isEqualEventTraces(tempEventTrace, eventTrace)) {
				return true;
			}
		}
		return false;
	}
	
	//�ж������켣�Ƿ����
	public static boolean isEqualEventTraces(Trace eventTrace1, Trace eventTrace2) {
		int size1 = eventTrace1.getEvents().size();
		int size2 = eventTrace2.getEvents().size();
		if (size1 != size2) {
			return false;
		}else {
			for (int i = 0; i < size1; i++) {
				String event1 = eventTrace1.getEvents().get(i);
				String event2 = eventTrace2.getEvents().get(i);
				if (!event1.equals(event2)) {
					return false;
				}
			}
		}
		return true;
	}

}
