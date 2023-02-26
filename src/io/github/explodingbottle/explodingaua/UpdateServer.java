package io.github.explodingbottle.explodingaua;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import io.github.explodingbottle.explodingau.ExplodingAULib;
import io.github.explodingbottle.explodingaua.updating.InstallResults;
import io.github.explodingbottle.explodingaua.updating.UpdateFinder;
import io.github.explodingbottle.explodingaua.updating.UpdatePackage;
import io.github.explodingbottle.explodingaua.updating.UpdateThread;

public class UpdateServer extends Thread {

	private ServerSocket serSock;
	private UpdateFinder finder;
	private UpdateThread updThread;

	private ArrayList<UpdatePackage> packages;
	private boolean lastSearchFailed;

	private ArrayList<ServerThread> threads;

	static final String KEY_STRING = "/explodingaua/browser_gateway.class?data=";

	public void interrupt() {
		super.interrupt();
		nextInterrupt = true;
		threads.forEach(t -> {
			t.interrupt();
		});
		if (serSock != null) {
			try {
				serSock.close();
			} catch (IOException e) {

			}
		}
	}

	boolean nextInterrupt;

	public UpdateServer() {
		threads = new ArrayList<ServerThread>();
	}

	public String treatAction(String request) {
		if (request.equalsIgnoreCase("PING")) {
			AgentMain.getLogger().write("WSP", "Received a PING query.");
			AgentMain.getLogger().write("WSP", "Doing a connection test.");
			boolean cS = false;
			try {
				URL tCon = new URL(AgentMain.getConfigurationReader().getConfiguration().getMainAttributes()
						.getValue("UpdaterEndpoint") + "/con_test/con_test.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(tCon.openStream()));
				String line = reader.readLine();
				if (line != null && line.equals("connection_test")) {
					cS = true;
				}
				reader.close();
			} catch (IOException e) {

			}
			if (!cS) {
				return "CON_TEST_FAIL";
			}
			return "PONG|" + AgentMain.getVersion();
		}
		if (request.equalsIgnoreCase("INTERRUPT")) {
			AgentMain.getLogger().write("WSP", "Interrupt received from Website.");
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
			if (updThread.getResults() != null) {
				String toRet = updThread.getResults().toString();
				updThread = null;
				return toRet;
			}
			return "INSTALLING";
		}

		if (request.equalsIgnoreCase("UPDATES_HISTORY")) {
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
				return historyRes.toString();
			} catch (Exception e) {
				AgentMain.getLogger().write("WSP", "Failed to load history. " + e.toString());
			}

			return "HISTORY_FAIL";
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
			serSock = new ServerSocket(Integer.parseInt(
					AgentMain.getConfigurationReader().getConfiguration().getMainAttributes().getValue("AgentPort")), 0,
					InetAddress.getLoopbackAddress());
			while (!interrupted() && !serSock.isClosed()) {
				try {
					Socket accepted = serSock.accept();
					ServerThread thread = new ServerThread(accepted, this);
					threads.add(thread);
					thread.start();
				} catch (IOException e) {
				}
			}
			interrupt();
		} catch (IOException e) {
		}
	}

}
