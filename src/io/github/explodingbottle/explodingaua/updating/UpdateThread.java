package io.github.explodingbottle.explodingaua.updating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap.SimpleEntry;
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

	private InstallResults results;

	public InstallResults getResults() {
		return results;
	}

	public void setInstallResults(InstallResults results) {
		this.results = results;
	}

	private byte[] buff = new byte[4096];

	public void run() {
		boolean delayBetweenInstalls = Boolean.parseBoolean(AgentMain.getConfigurationReader().getConfiguration()
				.getMainAttributes().getValue("DelayBetweenInstalls"));
		InstallResults results = new InstallResults();
		AgentMain.getLogger().write("UPDI",
				"+++++++++++++++++++++++++++++++ Updates Start +++++++++++++++++++++++++++++++");
		AgentMain.getLogger().write("UPDI", "Will install a total amount of " + updates.size() + " updates.");
		MessageDigest digestor = null;
		try {
			digestor = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			AgentMain.getLogger().write("UPDI", "Failed to initiate MessageDigest with " + e1 + ".");
			updates.forEach((pkg) -> {
				results.getResults().put(pkg,
						new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
			});
			return;
		}
		frame = new UpdatingFrame(this);
		frame.setVisible(true);
		File auFolder = ExplodingAULib.seekAUFolder();
		if (auFolder != null) {
			File dls = new File(auFolder, "Download");
			if (!dls.exists()) {
				if (!dls.mkdir()) {
					updates.forEach((pkg) -> {
						results.getResults().put(pkg,
								new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
					});
					return;
				}
			}
			int m = updates.size();
			int c = 0;
			AgentMain.getLogger().write("UPDI", "Downloading updates...");
			// frame.displayMessage("Downloading the updates...\n\r");
			for (UpdatePackage updPkg : updates) {
				try {
					frame.updatePbStatusText("Preparing for download");
					AgentMain.getLogger().write("UPDI", "Downloading " + updPkg.getDisplayName() + " for version "
							+ updPkg.getLatestVersion() + ".");
					frame.displayMessage("Downloading " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + " (update " + (c + 1) + " of " + m + ")...");
					String digested = new BigInteger(digestor.digest(new String(
							updPkg.getDisplayName() + "-" + updPkg.getLatestVersion() + updPkg.getDlLocation())
									.getBytes())).toString(32);
					File dlFile = new File(dls, digested + ".dl");
					if (delayBetweenInstalls) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}
					if (!dlFile.exists()) {
						URL dl = new URL(updPkg.getDlLocation());
						URLConnection con = dl.openConnection();
						long contentLength = con.getContentLengthLong();
						InputStream is = con.getInputStream();
						FileOutputStream fos = new FileOutputStream(dlFile);
						frame.updatePbStatusText("Downloading");
						int read = is.read(buff, 0, buff.length);
						long raccu = read;
						while (read != -1) {
							fos.write(buff, 0, read);
							read = is.read(buff, 0, buff.length);
							raccu += read;
						}
						frame.updateProgressBar(Math.round((raccu * 100 / contentLength)));
						fos.close();
						is.close();
						if (delayBetweenInstalls) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
						}
						frame.updatePbStatusText("Verifying the download");
						frame.updateProgressBar(0);
						String hash = ExplodingAULib.hashFile(dlFile);
						if (!hash.equals(updPkg.getLatestHash())) {
							dlFile.delete();
							results.getResults().put(updPkg,
									new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
							AgentMain.getLogger().write("UPDI",
									"Download of " + updPkg.getDisplayName() + " with version "
											+ updPkg.getLatestVersion() + " finished because of a hash mismatch.");
							frame.displayMessage(" Failed!\n\r");
						}
						if (delayBetweenInstalls) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
						}
						frame.updateProgressBar(100);
						AgentMain.getLogger().write("UPDI", "Download of " + updPkg.getDisplayName() + " with version "
								+ updPkg.getLatestVersion() + " finished.");
						frame.displayMessage(" Done!\n\r");
					} else {
						frame.updatePbStatusText("Verifying the download");
						frame.updateProgressBar(0);
						String hash = ExplodingAULib.hashFile(dlFile);
						if (!hash.equals(updPkg.getLatestHash())) {
							dlFile.delete();
							AgentMain.getLogger().write("UPDI",
									"Download of " + updPkg.getDisplayName() + " with version "
											+ updPkg.getLatestVersion() + " finished because of a hash mismatch.");
							frame.displayMessage(" Failed!\n\r");
							results.getResults().put(updPkg,
									new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
						} else {
							AgentMain.getLogger().write("UPDI",
									"Download of " + updPkg.getDisplayName() + " with version "
											+ updPkg.getLatestVersion()
											+ " finished because no download was required.");
							frame.displayMessage(" Download not required.\n\r");
						}
					}
				} catch (IOException e) {
					AgentMain.getLogger().write("UPDI", "Download of " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + " finished because the download failed.");
					frame.displayMessage(" Failed!\n\r");
					results.getResults().put(updPkg,
							new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
				}
				c++;
				// frame.updateProgressBar(c * 100 / m);
				if (delayBetweenInstalls) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
			AgentMain.getLogger().write("UPDI", "Finished updates downloading phase.");
			frame.updateProgressBar(0);
			c = 0;
			AgentMain.getLogger().write("UPDI", "Starting update installation phase.");
			frame.displayMessage("Installing updates...\n\r");
			frame.updatePbStatusText("Installing");
			Properties props = ExplodingAULib.loadPropsFromAUFolder(auFolder);
			if (props == null) {
				props = new Properties();
			}
			if (props != null) {
				for (UpdatePackage updPkg : updates) {
					AgentMain.getLogger().write("UPDI", "Installing " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + "...");
					frame.displayMessage("Installing " + updPkg.getDisplayName() + " with version "
							+ updPkg.getLatestVersion() + " (update " + (c + 1) + " of " + m + ")...");
					String digested = new BigInteger(digestor.digest(new String(
							updPkg.getDisplayName() + "-" + updPkg.getLatestVersion() + updPkg.getDlLocation())
									.getBytes())).toString(32);
					File dlFile = new File(dls, digested + ".dl");
					File target = new File(updPkg.getLinkedProgram().getpPath());
					if (!dlFile.exists()) {
						frame.displayMessage(" Failed.\n\r");
						results.getResults().put(updPkg,
								new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
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
						results.getResults().put(updPkg, new SimpleEntry<InstallResult, Long>(InstallResult.SUCCESS,
								System.currentTimeMillis()));
					} catch (IOException e) {
						AgentMain.getLogger().write("UPDI",
								"Installation of " + updPkg.getDisplayName() + " with version "
										+ updPkg.getLatestVersion() + " failed with exception: "
										+ e.getLocalizedMessage());
						frame.displayMessage(" Failed.\n\r");
						results.getResults().put(updPkg,
								new SimpleEntry<InstallResult, Long>(InstallResult.FAIL, System.currentTimeMillis()));
					}
					c++;
					frame.updateProgressBar(c * 100 / m);
					if (delayBetweenInstalls) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}
				}
			}
			ExplodingAULib.fileCfgRoutine(null, props);
			ExplodingAULib.storePropsToAUFolder(auFolder, props);
		}
		AgentMain.getLogger().write("UPDI", "Writing update history.");
		try {
			File history = new File(ExplodingAULib.seekAUFolder(), "update_history.dat");
			InstallResults historyRes = null;
			if (!history.exists()) {
				historyRes = new InstallResults();
			} else {
				FileInputStream input = new FileInputStream(history);
				ObjectInputStream toRead = new ObjectInputStream(input);
				historyRes = (InstallResults) toRead.readObject();
				toRead.close();
				input.close();
			}
			final InstallResults forEachIR = historyRes;
			results.getResults().forEach((k, v) -> {
				forEachIR.getResults().put(k, v);
			});
			FileOutputStream fos = new FileOutputStream(history);
			ObjectOutputStream toWrite = new ObjectOutputStream(fos);
			toWrite.writeObject(historyRes);
			toWrite.close();
			fos.close();
		} catch (Exception e) {
			AgentMain.getLogger().write("UPDI", "Failed to update the history. " + e);
		}
		frame.installDone(results);
		AgentMain.getLogger().write("UPDI",
				"+++++++++++++++++++++++++++++++ Updates End +++++++++++++++++++++++++++++++");
	}

}
