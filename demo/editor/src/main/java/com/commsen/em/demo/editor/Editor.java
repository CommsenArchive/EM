package com.commsen.em.demo.editor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import com.commsen.em.demo.markup.Markup;

public class Editor {

	private Markup markup = new Markup();

	public Editor() {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void createAndShowGUI() {

		JFrame frame = new JFrame("Fantastic Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JTextArea text = new JTextArea("1.1 + 1.33 is <math>1.1 + 1.33</math> and 2.2 + 2.2 is <math>2.2 + 2.2</math>");
		final JButton button = new JButton("Preview");
		final JLabel preview = new JLabel();

		GridLayout layout = new GridLayout(3, 0);
		layout.setHgap(10);
		layout.setVgap(10);

		frame.setLayout(layout);
		
		frame.getContentPane().add(text);
		frame.getContentPane().add(button);
		frame.getContentPane().add(preview);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preview.setText(markup.transform(text.getText()));
			}
		});

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Editor();
	}
}
