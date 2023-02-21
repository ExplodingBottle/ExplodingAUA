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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import io.github.explodingbottle.explodingaua.updating.UpdateFinder;
import io.github.explodingbottle.explodingaua.updating.UpdatePackage;
import io.github.explodingbottle.explodingaua.updating.UpdateThread;

public class UpdateServer extends Thread {

	private ServerSocket serSock;
	private UpdateFinder finder;
	private UpdateThread updThread;

	private ArrayList<UpdatePackage> packages;
	private boolean lastSearchFailed;

	private static final String KEY_STRING = "/explodingaua/browser_gateway.class?data=";

	public void interrupt() {
		super.interrupt();
		if (serSock != null) {
			try {
				serSock.close();
			} catch (IOException e) {

			}
		}
	}

	private boolean nextInterrupt;

	public String treatAction(String request) {
		if (request.equalsIgnoreCase("PING")) {
			return "PONG|" + AgentMain.getVersion();
		}
		if (request.equalsIgnoreCase("INTERRUPT")) {
			nextInterrupt = true;
			return "OK";
		}
		if (request.equalsIgnoreCase("CHECKSCAN")) {
			if (finder == null) {
				return "NOT_SCANNING";
			} else {
				packages = finder.getPackages();
				lastSearchFailed = finder.lastAttemptFailed();
				if (packages != null) {
					finder = null;
					return "DONE";
				} else {
					return "SCANNING";
				}
			}
		}
		if (request.equalsIgnoreCase("INSTALL_RESULTS")) {
			if (updThread.isDone()) {
				updThread = null;
				return "DONE";
			}
			return "INSTALLING";
		}
		if (request.toLowerCase().startsWith("sinstall:")) {
			if (packages == null) {
				return "NO_PACKAGES";
			}
			if (updThread == null) {
				ArrayList<UpdatePackage> toInstall = new ArrayList<UpdatePackage>();
				String[] spl = request.split(":");
				for (int i = 1; i < spl.length; i++) {
					UpdatePackage fnd = packages.get(Integer.parseInt(spl[i]));
					if (fnd != null && fnd.isRequiresUpdate()) {
						toInstall.add(fnd);
					}
				}
				packages = null;
				updThread = new UpdateThread(toInstall);
				updThread.start();
			}
			return "INSTALLING";
		}
		if (request.equalsIgnoreCase("RETURN_RESULTS")) {
			if (lastSearchFailed) {
				return "SEARCHING_FAILURE";
			}
			if (packages == null) {
				return "NO_PACKAGES";
			}
			StringBuilder bd = new StringBuilder();
			for (int i = 0; i < packages.size(); i++) {
				UpdatePackage curPkg = packages.get(i);
				bd.append(i + "," + curPkg.toStringNoArgs() + "\r\n");
			}
			return bd.toString();
		}
		if (request.equalsIgnoreCase("STARTSCAN")) {
			if (finder == null) {
				finder = new UpdateFinder(AgentMain.getConfigurationReader().getConfiguration().getMainAttributes()
						.getValue("UpdaterEndpoint"));
				finder.start();
			}
			return "SCANNING";
		}
		return "UNSUPPORTED";
	}

	public void run() {
		try {
			serSock = new ServerSocket(7498, 0, InetAddress.getLoopbackAddress());
			while (!interrupted() && !serSock.isClosed()) {
				try {
					Socket accepted = serSock.accept();
					InputStream is = accepted.getInputStream();
					OutputStream os = accepted.getOutputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
					String line = reader.readLine();
					String fl = line;
					if (line != null) {
						while (line != null && !line.trim().isEmpty()) {
							line = reader.readLine();
						}
						String[] split = fl.split(" ");
						if (split.length == 3) {
							if (split[0].equals("GET") && split[1].toLowerCase().startsWith(KEY_STRING)) {
								String data = split[1].toLowerCase()
										.split(KEY_STRING.replace("/", "\\/").replace("?", "\\?"))[1];
								String result = treatAction(data);
								writer.write("HTTP/1.1 200 OK" + "\r\n");
								writer.write("Connection: Close" + "\r\n");
								writer.write("Content-Type: text/html; charset=utf-8" + "\r\n");
								// writer.write("Access-Control-Allow-Origin: *" + "\r\n");
								writer.write("Access-Control-Allow-Origin: " + AgentMain.getConfigurationReader()
										.getConfiguration().getMainAttributes().getValue("Website") + "\r\n");
								writer.write("Content-Length: " + result.getBytes().length + "\r\n");
								writer.write("\r\n");
								writer.write(result);
							} else {
								writer.write("HTTP/1.1 500 Internal Server Error" + "\r\n");
								writer.write("Connection: Close" + "\r\n");
								writer.write("Content-Type: text/html; charset=utf-8" + "\r\n");
								// writer.write("Access-Control-Allow-Origin: *" + "\r\n");
								writer.write("Access-Control-Allow-Origin: " + AgentMain.getConfigurationReader()
										.getConfiguration().getMainAttributes().getValue("Website") + "\r\n");
								writer.write("\r\n");
							}
						} else {
							writer.write("HTTP/1.1 500 Internal Server Error" + "\r\n");
							writer.write("Connection: Close" + "\r\n");
							writer.write("Content-Type: text/html; charset=utf-8" + "\r\n");
							// writer.write("Access-Control-Allow-Origin: *" + "\r\n");
							writer.write("Access-Control-Allow-Origin: " + AgentMain.getConfigurationReader()
									.getConfiguration().getMainAttributes().getValue("Website") + "\r\n");
							writer.write("\r\n");
						}
					} else {
						writer.write("HTTP/1.1 500 Internal Server Error" + "\r\n");
						writer.write("Connection: Close" + "\r\n");
						writer.write("Content-Type: text/html; charset=utf-8" + "\r\n");
						writer.write("Access-Control-Allow-Origin: " + AgentMain.getConfigurationReader()
								.getConfiguration().getMainAttributes().getValue("Website") + "\r\n");
						writer.write("\r\n");
					}
					writer.close();
					reader.close();
					accepted.close();
					if (nextInterrupt) {
						interrupt();
					}
				} catch (IOException e) {
				}
			}
			interrupt();
		} catch (IOException e) {
		}
	}

}
