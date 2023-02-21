/*
 *   ExplodingAUA - The automatic update agent for ExplodingBottle projects.
 *   Copyright (C) 2023  ExplodingBottle
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.explodingbottle.explodingaua;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UpdatingFrame extends JFrame implements ActionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea messages;
	private JProgressBar pBar;
	private JScrollPane pane;

	private JButton finish;

	public UpdatingFrame() {
		setTitle("ExplodingAU");
		setSize(700, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
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

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}
