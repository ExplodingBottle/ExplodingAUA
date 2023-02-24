package io.github.explodingbottle.explodingaua;

import java.io.File;

import javax.swing.JOptionPane;

import io.github.explodingbottle.explodingau.ExplodingAULib;
import io.github.explodingbottle.explodingaua.config.ConfigurationReader;

public class AgentMain {

	private static ConfigurationReader reader;
	private static Logger logger;

	public static ConfigurationReader getConfigurationReader() {
		return reader;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static String getVersion() {
		return "1.0.0.6";
	}

	public static void main(String[] args) {
		if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
			JOptionPane.showMessageDialog(null, "This agent can only be started under Windows !", "CopperCart Update",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		int i = JOptionPane.showConfirmDialog(null,
				"Are you sure that you want to start the Coppercart Update Agent ? You can shut it down after through the website.",
				"CopperCart Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == 0) {
			File seekedAu = ExplodingAULib.seekAUFolder();
			if (seekedAu != null) {
				File logFile = new File(seekedAu, "ExplodingAU.log");
				logger = new Logger(logFile);
				logger.open();
			} else {
				logger = new Logger(null);
			}
			reader = new ConfigurationReader("updater.mf");
			if (!reader.loadConfiguration()) {
				JOptionPane.showMessageDialog(null, "Failed to load the Agent's configuration.", "CopperCart Update",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			UpdateServer server = new UpdateServer();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					logger.close();
					server.interrupt();
				}
			});
			server.start();
			logger.write("MAIN", "Agent is loaded.");

		} else {
			JOptionPane.showMessageDialog(null, "CopperCart Update Agent Agent won't be started.", "CopperCart Update",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

}
