package com.tistory.devyongsik.sample;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

public class Listener implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("Command: " + e.getActionCommand());
		System.out.println("Command: " + e.paramString());
		System.out.println("Command: " + e.toString());
		if ("분석".equals(e.getActionCommand())) {

			StringBuffer resultString = new StringBuffer();
			resultString.append("Text =");
			resultString.append(AnalysisResultViewer.textTextArea.getText());
			resultString.append("\n");
			resultString.append("URL =");
			resultString.append(AnalysisResultViewer.urlTextArea.getText());
			AnalysisResultViewer.resultTextArea.setText(resultString.toString());			
			
			JOptionPane.showMessageDialog(AnalysisResultViewer.keywordAnalysis, "분석이 완료 되었습니다.", "분석", JOptionPane.INFORMATION_MESSAGE);
		} else if ("취소".equals(e.getActionCommand())) {
			AnalysisResultViewer.textTextArea.setText("");
			AnalysisResultViewer.urlTextArea.setText("");
			AnalysisResultViewer.resultTextArea.setText("");
		}		
	}

}
