package com.tistory.devyongsik.sample;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 8. 17.
 *
 */
public class AnalysisResultViewer extends JFrame {
	
	private Log logger = LogFactory.getLog(AnalysisResultViewer.class);
	
	public static void main(String args[]) {
		new AnalysisResultViewer();
	}
	
	private AnalysisResultViewer() {
		JLabel jlbHelloWorld = new JLabel("Hello World");
		add(jlbHelloWorld);
		this.setSize(100, 100);
		// pack();
		setVisible(true);
	}
}
