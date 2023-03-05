package io.github.explodingbottle.explodingaua;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

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
		return "1.0.1.1";
	}

	private static File frameIcon;

	public static File getAgentIcon() {
		return frameIcon;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {

		}
		byte[] buffer = new byte[4096];

		boolean launchedAutomatically = false;

		if (args.length == 1 && args[0].equals("auto")) {
			launchedAutomatically = true;
		}

		int i = 1;
		if (!launchedAutomatically) {
			i = JOptionPane.showConfirmDialog(null,
					"Are you sure that you want to start the Agent ?",
					"Update Agent", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		} else {
			i = 0;
		}
		if (i == 0) {

			File seekedAu = ExplodingAULib.seekAUFolder();
			if (seekedAu != null) {
				File logFile = new File(seekedAu, "ExplodingAU.log");
				logger = new Logger(logFile);
				logger.open();
			} else {
				logger = new Logger(null);
			}

			File auCfgFold = new File(seekedAu, "aucfg");

			if (!auCfgFold.exists()) {
				if (!auCfgFold.mkdir()) {
					JOptionPane.showMessageDialog(null, "Failed to load the Agent's configuration system folder.",
							"Fatal error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			ConfigurationReader endpointLoc = new ConfigurationReader("updater.mf");
			if (!endpointLoc.loadConfiguration()) {
				JOptionPane.showMessageDialog(null, "Failed to load the Agent's local configuration.", "Fatal error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String endpoint = endpointLoc.getConfiguration().getMainAttributes().getValue("UpdaterEndpoint");
			String endpointCfg = endpoint + "/config/config.txt";

			logger.write("MAIN", "Will download the latest configuration at: " + endpointCfg);

			File localSavedConfig = new File(auCfgFold, "cfgredir.txt");
			File savedZip = new File(auCfgFold, "config.zip");
			File extracted = new File(auCfgFold, "updater.mf");
			File agentLogo = new File(auCfgFold, "agent_logo.png");
			try {
				URL cfgPointerUrl = new URL(endpointCfg);
				URLConnection c = cfgPointerUrl.openConnection();
				InputStream cfgPointerDl = c.getInputStream();
				FileOutputStream writer = new FileOutputStream(localSavedConfig);
				int read = cfgPointerDl.read(buffer, 0, buffer.length);
				while (read != -1) {
					writer.write(buffer, 0, read);
					read = cfgPointerDl.read(buffer, 0, buffer.length);
				}
				writer.close();
				cfgPointerDl.close();
				BufferedReader reader2 = new BufferedReader(new FileReader(localSavedConfig));
				URL cfgZipUrl = new URL(reader2.readLine());
				reader2.close();
				logger.write("MAIN", "Discovered aucfg.zip location: " + cfgZipUrl + ".");
				c = cfgZipUrl.openConnection();
				cfgPointerDl = c.getInputStream();
				writer = new FileOutputStream(savedZip);
				read = cfgPointerDl.read(buffer, 0, buffer.length);
				while (read != -1) {
					writer.write(buffer, 0, read);
					read = cfgPointerDl.read(buffer, 0, buffer.length);
				}
				writer.close();
				cfgPointerDl.close();
				ZipInputStream zis = new ZipInputStream(new FileInputStream(savedZip));
				ZipEntry entry = zis.getNextEntry();
				boolean weFound = false;
				while (entry != null) {
					if (entry.getName().equalsIgnoreCase("updater.mf") && !entry.isDirectory()) {
						weFound = true;
						FileOutputStream fos = new FileOutputStream(extracted);
						read = zis.read(buffer, 0, buffer.length);
						while (read != -1) {
							fos.write(buffer, 0, read);
							read = zis.read(buffer, 0, buffer.length);
						}
						fos.close();
					}
					if (entry.getName().equalsIgnoreCase("agent.png") && !entry.isDirectory()) {
						FileOutputStream fos = new FileOutputStream(agentLogo);
						read = zis.read(buffer, 0, buffer.length);
						while (read != -1) {
							fos.write(buffer, 0, read);
							read = zis.read(buffer, 0, buffer.length);
						}
						fos.close();
						frameIcon = agentLogo;
					}
					entry = zis.getNextEntry();
					reader = new ConfigurationReader(extracted);
				}
				zis.close();
				if (!weFound) {
					logger.write("MAIN", "Malformed aucfg.zip (no updater.mf)");
					JOptionPane.showMessageDialog(null, "Failed to update the Agent's configuration.", "Fatal error",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {

				logger.write("MAIN", "Failed to download the configuration. " + e.toString());
				JOptionPane.showMessageDialog(null, "Failed to update the Agent's configuration.", "Fatal error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (!reader.loadConfiguration()) {
				JOptionPane.showMessageDialog(null, "Failed to load the Agent's configuration.", "Fatal error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			logger.write("MAIN", "AutoLaunch: " + launchedAutomatically);

			if (Boolean.parseBoolean(
					getConfigurationReader().getConfiguration().getMainAttributes().getValue("OnlyWindows"))) {
				if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
					JOptionPane.showMessageDialog(null,
							"This agent restricts its use to allow it only under Windows systems.", "Fatal error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			File dest = new File(seekedAu, "agent.jar");

			if (Boolean.parseBoolean(
					getConfigurationReader().getConfiguration().getMainAttributes().getValue("AllowWindowsInstall"))
					&& !dest.exists() && System.getProperty("os.name").toLowerCase().contains("windows")) {
				int installInt = JOptionPane.showConfirmDialog(null,
						"Do you want to install the agent ? It will launched every time your session will be opened.",
						"Agent installation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (installInt == 0) {
					logger.write("MAIN", "AgentInstall:true");
					try {
						File location = new File(
								ExplodingAULib.class.getProtectionDomain().getCodeSource().getLocation().toURI());
						Files.copy(location.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						File startup = new File(System.getenv("APPDATA"),
								"Microsoft\\Windows\\Start Menu\\Programs\\Startup");
						if (!startup.exists()) {
							throw new Exception();
						} else {
							File aurunner = new File(startup, getConfigurationReader().getConfiguration()
									.getMainAttributes().getValue("AutoStartFile"));
							FileOutputStream fos = new FileOutputStream(aurunner);
							fos.write(new String("@echo off\r\n").getBytes());
							fos.write(new String("\r\n").getBytes());
							fos.write(new String(
									"start javaw -jar \"" + seekedAu.getAbsolutePath() + "\\agent.jar\" auto\r\n")
											.getBytes());
							fos.write(new String("\r\n").getBytes());
							fos.close();
							Runtime runtime = Runtime.getRuntime();
							runtime.exec("\"" + aurunner.getAbsolutePath() + "\"");
							return;
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Failed to install the agent.", "Agent installation",
								JOptionPane.WARNING_MESSAGE);
					}
				}
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
			JOptionPane.showMessageDialog(null, "The Update Agent won't be started.", "Agent startup",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

}
