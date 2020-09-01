package pipe.gui.action;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.dataLayer.AnnotationNote;

@SuppressWarnings("serial")
public class CalFinalMarkingAction extends AbstractAction {

	@SuppressWarnings("unused")
	private Container contentPane;
	private AnnotationNote selected;

	 public CalFinalMarkingAction(Container contentPane, AnnotationNote place) {
	     this.contentPane = contentPane;
	     selected = place;
	 }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		selected.setFinalMarkingNote(false);
		selected.repaint();//�ػ浱ǰ��ע

	}

}
