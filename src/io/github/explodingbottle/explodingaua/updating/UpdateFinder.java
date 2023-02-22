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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import io.github.explodingbottle.explodingau.ExplodingAULib;
import io.github.explodingbottle.explodingaua.AgentMain;

public class UpdateFinder extends Thread {

	private String endpointUrl;
	private ArrayList<UpdatePackage> packages;

	public UpdateFinder(String endpointUrl) {
		this.endpointUrl = endpointUrl;
		packages = null;
	}

	private boolean lastFailed;

	public boolean lastAttemptFailed() {
		return lastFailed;
	}

	public ArrayList<UpdatePackage> getPackages() {
		return packages;
	}

	public void run() {
		AgentMain.getLogger().write("UPDF", "Started a local scan.");
		ArrayList<ProgramInformation> infos = new ArrayList<ProgramInformation>();
		ArrayList<UpdatePackage> packages = new ArrayList<UpdatePackage>();
		File auFolder = ExplodingAULib.seekAUFolder();
		if (auFolder != null) {
			Properties props = ExplodingAULib.loadPropsFromAUFolder(auFolder);
			if (props == null) {
				props = new Properties();
			}
			ExplodingAULib.fileCfgRoutine(null, props);
			props.forEach((k, v) -> {
				String path = (String) k;
				String vals = (String) v;
				String[] splt = vals.split(";");
				String pName = splt[0];
				String pHash = splt[1];
				infos.add(new ProgramInformation(pHash, path, pName));
			});
			ExplodingAULib.storePropsToAUFolder(auFolder, props);

		} else {
			AgentMain.getLogger().write("UPDF", "Failed the local scan because we couldn't find the AU folder.");
			lastFailed = true;
		}
		AgentMain.getLogger().write("UPDF", "Local scan ended.");
		if (lastFailed) {
			AgentMain.getLogger().write("UPDF", "Local scan failed, we won't continue the scan.");
			return;
		}
		infos.forEach(pinfo -> {
			AgentMain.getLogger().write("UPDF", "\t" + pinfo.toString());
		});
		AgentMain.getLogger().write("UPDF", "+++++++++++++++++++++++++++++++ Online scan - Using endpoint: "
				+ endpointUrl + " +++++++++++++++++++++++++++++++");
		HashMap<String, Manifest> manifestPinfos = new HashMap<String, Manifest>();
		HashMap<String, Properties> manifestPhashes = new HashMap<String, Properties>();
		int[] qtToUpd = new int[1];
		infos.forEach(pinfo -> {
			try {
				Manifest manifPinfo = null;
				Properties propsHash = null;
				if (!manifestPhashes.containsKey(pinfo.getpId()) || !manifestPhashes.containsKey(pinfo.getpId())) {
					AgentMain.getLogger().write("UPDF", "Must fetch scan informations for " + pinfo.getpId());
					URL hashesUrl = new URL(endpointUrl + "/hashes/" + pinfo.getpId() + ".hsh");
					URL pinfoUrl = new URL(endpointUrl + "/dl/" + pinfo.getpId() + ".mf");
					URLConnection conn = hashesUrl.openConnection();
					URLConnection conn2 = pinfoUrl.openConnection();
					InputStream hashesIs = conn.getInputStream();
					InputStream pinfoIs = conn2.getInputStream();
					Properties propsHash2 = new Properties();
					Manifest manifPinfo2 = new Manifest(pinfoIs);
					propsHash2.load(hashesIs);
					pinfoIs.close();
					hashesIs.close();
					manifestPinfos.put(pinfo.getpId(), manifPinfo2);
					manifestPhashes.put(pinfo.getpId(), propsHash2);
					manifPinfo = manifPinfo2;
					propsHash = propsHash2;
				} else {
					AgentMain.getLogger().write("UPDF", "Scan result already found for " + pinfo.getpId());
					manifPinfo = manifestPinfos.get(pinfo.getpId());
					propsHash = manifestPhashes.get(pinfo.getpId());
				}

				String matchingVersion = propsHash.getProperty(pinfo.getpHash());
				if (matchingVersion != null) {
					Attributes manif = manifPinfo.getMainAttributes();
					UpdatePackage uPackage = new UpdatePackage(pinfo, matchingVersion, manif.getValue("DisplayName"),
							manif.getValue("Latest"), manif.getValue("DlLoc"), manif.getValue("Mode"),
							!matchingVersion.equals(manif.getValue("Latest")));
					if (uPackage.isRequiresUpdate()) {
						qtToUpd[0]++;
					}
					packages.add(uPackage);
				}
			} catch (IOException e) {
				AgentMain.getLogger().write("UPDF", "We failed to download informations for " + pinfo.getpId()
						+ " because of " + e.toString() + ".");
				e.printStackTrace();
			}
		});
		AgentMain.getLogger().write("UPDF",
				"+++++++++++++++++++++++++++++++ Online scan - Done +++++++++++++++++++++++++++++++");
		packages.forEach(pckg -> {
			AgentMain.getLogger().write("UPDF", "\t" + pckg.toString());
		});
		this.packages = packages;
	}

}
