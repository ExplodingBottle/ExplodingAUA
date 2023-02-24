package io.github.explodingbottle.explodingaua;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UpdatingFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea messages;
	private JProgressBar pBar;
	private JScrollPane pane;

	private JButton finish;

	public UpdatingFrame() {
		setTitle("Downloading and Installing updates... - CopperCart Update");
		setSize(700, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		messages = new JTextArea();
		pane = new JScrollPane(messages);
		messages.setEditable(false);
		messages.setLineWrap(true);
		pBar = new JProgressBar();
		pBar.setMaximum(100);
		finish = new JButton("Finish");
		finish.addActionListener(this);
		add(BorderLayout.SOUTH, pBar);
		add(BorderLayout.CENTER, pane);
	}

	public void installDone() {
		remove(pBar);
		add(BorderLayout.SOUTH, finish);
		displayMessage("All the updates were installed !\r\n");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		revalidate();
		repaint();
	}

	public void displayMessage(String message) {
		messages.append(message);
		messages.setCaretPosition(messages.getDocument().getLength());
	}

	public void updateProgressBar(int percentage) {
		pBar.setValue(percentage);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == finish) {
			setVisible(false);
			dispose();
		}

	}
}
