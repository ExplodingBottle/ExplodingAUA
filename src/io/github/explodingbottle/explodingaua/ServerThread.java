package io.github.explodingbottle.explodingaua;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerThread extends Thread {
	private Socket soc;
	private UpdateServer parent;

	public ServerThread(Socket soc, UpdateServer parent) {
		this.soc = soc;
		this.parent = parent;
	}

	public void interrupt() {
		super.interrupt();
		if (soc != null) {
			try {
				soc.close();
			} catch (IOException e) {

			}
		}
	}

	public void run() {
		try {
			InputStream is = soc.getInputStream();
			OutputStream os = soc.getOutputStream();
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
					if (split[0].equals("GET") && split[1].toLowerCase().startsWith(UpdateServer.KEY_STRING)) {
						String data = split[1].toLowerCase()
								.split(UpdateServer.KEY_STRING.replace("/", "\\/").replace("?", "\\?"))[1];
						String result = parent.treatAction(data);
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
					writer.write("Access-Control-Allow-Origin: " + AgentMain.getConfigurationReader().getConfiguration()
							.getMainAttributes().getValue("Website") + "\r\n");
					writer.write("\r\n");
				}
			} else {
				writer.write("HTTP/1.1 500 Internal Server Error" + "\r\n");
				writer.write("Connection: Close" + "\r\n");
				writer.write("Content-Type: text/html; charset=utf-8" + "\r\n");
				writer.write("Access-Control-Allow-Origin: "
						+ AgentMain.getConfigurationReader().getConfiguration().getMainAttributes().getValue("Website")
						+ "\r\n");
				writer.write("\r\n");
			}
			writer.close();
			reader.close();
			soc.close();
			if (parent.nextInterrupt) {
				parent.interrupt();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (soc != null) {
				try {
					soc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
