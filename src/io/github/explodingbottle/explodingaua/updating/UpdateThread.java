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
package io.github.explodingbottle.explodingaua.updating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.github.explodingbottle.explodingau.ExplodingAULib;
import io.github.explodingbottle.explodingaua.AgentMain;
import io.github.explodingbottle.explodingaua.UpdatingFrame;

public class UpdateThread extends Thread {

	private ArrayList<UpdatePackage> updates;

	public UpdateThread(ArrayList<UpdatePackage> updates) {
		this.updates = updates;
	}

	private UpdatingFrame frame;
	private boolean done;

	public boolean isDone() {
		return done;
	}

	private byte[] buff = new byte[4096];

	public void run() {
		AgentMain.getLogger().write("UPDI",
				"+++++++++++++++++++++++++++++++ Updates Start +++++++++++++++++++++++++++++++");
		AgentMain.getLogger().write("UPDI", "Will install a total amount of " + updates.size() + " updates.");
		MessageDigest digestor = null;
		try {
			digestor = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			AgentMain.getLogger().write("UPDI", "Failed to initiate MessageDigest with " + e1 + ".");
			done = true;
			return;
		}
		frame = new UpdatingFrame();
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(false);
		File auFolder = ExplodingAULib.seekAUFolder();
		if (auFolder != null) {
			File dls = new File(auFolder, "Download");
			if (!dls.exists()) {
				if (!dls.mkdir()) {
					done = true;
					return;
				}
			}
			int m = updates.size();
			int c = 0;
			AgentMain.getLogger().write("UPDI", "Downloading updates...");
			frame.displayMessage("Downloading the updates...\n\r");
			for (UpdatePackage updPkg : updates) {
				try {
					AgentMain.getLogger().write("UPDI", "Downloading " + updPkg.getDisplayName() + " for version "
							+ updPkg.getLatestVersion() + ".");
					frame.displayMessage("Downloading " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + "...");
					String digested = new BigInteger(digestor.digest(new String(
							updPkg.getDisplayName() + "-" + updPkg.getLatestVersion() + updPkg.getDlLocation())
									.getBytes())).toString(32);
					File dlFile = new File(dls, digested + ".dl");
					if (!dlFile.exists()) {
						URL dl = new URL(updPkg.getDlLocation());
						URLConnection con = dl.openConnection();
						InputStream is = con.getInputStream();
						FileOutputStream fos = new FileOutputStream(dlFile);
						int read = is.read(buff, 0, buff.length);
						while (read != -1) {
							fos.write(buff, 0, read);
							read = is.read(buff, 0, buff.length);
						}
						fos.close();
						is.close();
						AgentMain.getLogger().write("UPDI", "Download of " + updPkg.getDisplayName() + " with version "
								+ updPkg.getLatestVersion() + " finished.");
						frame.displayMessage(" Done.\n\r");
					} else {
						AgentMain.getLogger().write("UPDI", "Download of " + updPkg.getDisplayName() + " with version "
								+ updPkg.getLatestVersion() + " finished because no download was required.");
						frame.displayMessage(" Download not required.\n\r");
					}
				} catch (IOException e) {
					AgentMain.getLogger().write("UPDI", "Download of " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + " finished because the download failed.");
					frame.displayMessage(" Failed!\n\r");
				}
				c++;
				frame.updateProgressBar(c * 100 / m);
			}
			AgentMain.getLogger().write("UPDI", "Finished updates downloading phase.");
			frame.displayMessage("Updates were downloaded !\n\r");
			frame.updateProgressBar(0);
			c = 0;
			AgentMain.getLogger().write("UPDI", "Starting update installation phase.");
			frame.displayMessage("Installing updates...\n\r");
			Properties props = ExplodingAULib.loadPropsFromAUFolder(auFolder);
			if (props == null) {
				props = new Properties();
			}
			if (props != null) {
				for (UpdatePackage updPkg : updates) {
					AgentMain.getLogger().write("UPDI", "Installing " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + "...");
					frame.displayMessage("Installing " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + "...");
					String digested = new BigInteger(digestor.digest(new String(
							updPkg.getDisplayName() + "-" + updPkg.getLatestVersion() + updPkg.getDlLocation())
									.getBytes())).toString(32);
					File dlFile = new File(dls, digested + ".dl");
					File target = new File(updPkg.getLinkedProgram().getpPath());
					if (!dlFile.exists()) {
						frame.displayMessage(" Failed.\n\r");
						AgentMain.getLogger().write("UPDI",
								"Installation of " + updPkg.getDisplayName() + " with version "
										+ updPkg.getLatestVersion() + " failed because the download file was missing.");
						continue;
					}
					try {
						if (updPkg.getMode().equalsIgnoreCase("direct")) {
							AgentMain.getLogger().write("UPDI", "Installation will be done trough 'DIRECT'.");
							Files.copy(dlFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
						}
						if (updPkg.getMode().toLowerCase().startsWith("unzip;")) {
							String uzPath = updPkg.getMode().split(";")[1];
							AgentMain.getLogger().write("UPDI", "Installation will be done trough 'UNZIP'.");
							FileOutputStream fos = new FileOutputStream(target);
							FileInputStream fis = new FileInputStream(dlFile);
							ZipInputStream zis = new ZipInputStream(fis);
							ZipEntry entry = zis.getNextEntry();
							while (entry != null) {
								if (entry.getName().equals(uzPath)) {
									int read = zis.read(buff, 0, buff.length);
									while (read != -1) {
										fos.write(buff, 0, read);
										read = zis.read(buff, 0, buff.length);
									}
								}
								entry = zis.getNextEntry();
							}
							zis.close();
							fis.close();
							fos.close();
						}
						String fHash = ExplodingAULib.hashFile(target);
						if (fHash != null) {
							props.put(target.getAbsolutePath(), updPkg.getLinkedProgram().getpId() + ";" + fHash);
						}
						frame.displayMessage(" Done.\n\r");
						AgentMain.getLogger().write("UPDI", "Installation of " + updPkg.getDisplayName()
								+ " with version " + updPkg.getLatestVersion() + " is now finished.");
					} catch (IOException e) {
						AgentMain.getLogger().write("UPDI",
								"Installation of " + updPkg.getDisplayName() + " with version "
										+ updPkg.getLatestVersion() + " failed with exception: "
										+ e.getLocalizedMessage());
						frame.displayMessage(" Failed.\n\r");
					}
					c++;
					frame.updateProgressBar(c * 100 / m);
				}
			}
			ExplodingAULib.fileCfgRoutine(null, props);
			ExplodingAULib.storePropsToAUFolder(auFolder, props);
		}
		frame.displayMessage("The updates were installed.\n\r");
		frame.installDone();
		AgentMain.getLogger().write("UPDI",
				"+++++++++++++++++++++++++++++++ Updates End +++++++++++++++++++++++++++++++");
		done = true;
	}

}
