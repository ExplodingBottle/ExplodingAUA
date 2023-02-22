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
		int i = JOptionPane.showConfirmDialog(null,
				"Are you sure that you want to start the ExplodingAU Agent ? You can shut it down after through the website.",
				"Agent startup", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
				JOptionPane.showMessageDialog(null, "Failed to load the Agent's configuration.", "Fatal error",
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
			JOptionPane.showMessageDialog(null, "ExplodingAU Agent won't be started.", "Agent startup",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

}
