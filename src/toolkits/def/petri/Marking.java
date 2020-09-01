package toolkits.def.petri;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moqi
 * ����������еı�ʶ,����һ��������
 */
public class Marking {

	private List<String> places;
	
	public Marking() {
		places = new ArrayList<String>();
	}

	public void addPlace(String place) {
		places.add(place);
	}
	public void addPlaces(List<String> tempPlaces) {
		places.addAll(tempPlaces);
	}
	public List<String> getPlaces() {
		return places;
	}
	public void setPlaces(List<String> places) {
		this.places = places;
	}
	
}
