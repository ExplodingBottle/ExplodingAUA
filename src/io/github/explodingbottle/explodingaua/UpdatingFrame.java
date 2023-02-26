package io.github.explodingbottle.explodingaua;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import io.github.explodingbottle.explodingaua.updating.InstallResult;
import io.github.explodingbottle.explodingaua.updating.InstallResults;
import io.github.explodingbottle.explodingaua.updating.UpdateThread;

public class UpdatingFrame extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea messages;
	private JProgressBar pBar;
	private JScrollPane pane;

	private JLabel updatingText;

	private JButton finish;

	private JLabel statusText;

	private JLabel installationStatus;

	private UpdateThread parent;
	private InstallResults linkedResults;

	public UpdatingFrame(UpdateThread parent) {
		Image img = null;
		if (AgentMain.getAgentIcon() != null) {
			try {
				Toolkit kit = Toolkit.getDefaultToolkit();
				img = kit.createImage(AgentMain.getAgentIcon().toURI().toURL());
			} catch (MalformedURLException e) {
			}
		}
		if (img != null) {
			setIconImage(img);
		}
		setTitle("Installing Updates");
		setSize(600, 500);
		setLocationRelativeTo(null);
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		messages = new JTextArea();
		messages.setFont(messages.getFont().deriveFont(12.0f));
		pane = new JScrollPane(messages);
		messages.setEditable(false);
		messages.setLineWrap(true);
		messages.setWrapStyleWord(true);
		pBar = new JProgressBar();
		pBar.setMaximum(100);
		finish = new JButton("Close");
		finish.addActionListener(this);
		setLayout(new GridBagLayout());
		JPanel mainContainer = new JPanel();
		JLabel agentIcon = new JLabel();
		this.parent = parent;
		statusText = new JLabel("");
		updatingText = new JLabel("The updates are being downloaded and installed");
		updatingText.setVerticalAlignment(SwingConstants.CENTER);

		installationStatus = new JLabel("Installation status:");

		updatingText.setFont(updatingText.getFont().deriveFont(Font.BOLD).deriveFont(16.0f));
		if (img != null) {
			agentIcon.setIcon(new ImageIcon(img.getScaledInstance(64, 64, Image.SCALE_DEFAULT)));
		}
		mainContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		mainContainer.setLayout(new GridBagLayout());
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.insets = new Insets(10, 10, 70, 10);
		add(mainContainer, layoutConstraints);
		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.NONE;
		layoutConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.insets = new Insets(10, 10, 10, 10);
		mainContainer.add(agentIcon, layoutConstraints);
		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.ipady = 32 + 16;
		layoutConstraints.insets = new Insets(10, 30 + 64, 10, 10);

		mainContainer.add(updatingText, layoutConstraints);
		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.insets = new Insets(160, 30, 120, 30);

		mainContainer.add(pane, layoutConstraints);

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.insets = new Insets(40, 30, 120, 30);

		mainContainer.add(installationStatus, layoutConstraints);

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.ipady = 10;
		layoutConstraints.insets = new Insets(300, 30, 10, 30);

		mainContainer.add(pBar, layoutConstraints);

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.insets = new Insets(250, 30, 10, 30);

		mainContainer.add(statusText, layoutConstraints);

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.NONE;
		layoutConstraints.anchor = GridBagConstraints.LAST_LINE_END;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 1;
		layoutConstraints.insets = new Insets(10, 10, 10, 10);

		finish.setVisible(false);

		add(finish, layoutConstraints);
	}

	public void installDone(InstallResults results) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		finish.setVisible(true);
		updatingText.setText("Installation complete");
		pBar.setVisible(false);
		statusText.setVisible(false);

		boolean[] hasFailed = new boolean[1];
		results.getResults().forEach((pkg, result) -> {
			if (result.getKey() == InstallResult.FAIL) {
				hasFailed[0] = true;
			}
		});

		if (!hasFailed[0]) {
			pane.setVisible(false);
			installationStatus.setVisible(false);
		} else {
			updatingText.setText("Some updates were not installed");
			installationStatus.setText("The following updates were not installed:");
			messages.setText("");
			results.getResults().forEach((pkg, result) -> {
				if (result.getKey() == InstallResult.FAIL) {
					messages.append(pkg.getDisplayName() + " version " + pkg.getLatestVersion() + " on file "
							+ pkg.getLinkedProgram().getpPath() + "\r\n");
				}
			});
		}
		linkedResults = results;

	}

	public void displayMessage(String message) {
		messages.append(message);
		messages.setCaretPosition(messages.getDocument().getLength());
	}

	public void updateProgressBar(int percentage) {
		pBar.setValue(percentage);
	}

	public void updatePbStatusText(String pbText) {
		statusText.setText(pbText + ":");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == finish) {
			parent.setInstallResults(linkedResults);
			setVisible(false);
			dispose();
		}

	}
}
