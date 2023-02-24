package io.github.explodingbottle.explodingaua.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import io.github.explodingbottle.explodingaua.AgentMain;

public class ConfigurationReader {

	private String fileName;
	private Manifest manifest;

	public ConfigurationReader(String fileName) {
		this.fileName = fileName;
	}

	public boolean loadConfiguration() {
		boolean success = false;
		InputStream input = getClass().getClassLoader().getResourceAsStream(fileName);
		if (input != null) {
			try {
				manifest = new Manifest(input);
				success = true;
			} catch (IOException e1) {
				AgentMain.getLogger().write("CFG", "Failed to load configuration.");
				e1.printStackTrace();
			}
			try {
				input.close();
			} catch (IOException e) {
				AgentMain.getLogger().write("CFG", "Failed to close configuration.");
				e.printStackTrace();
			}
		}
		return success;
	}

	public Manifest getConfiguration() {
		return manifest;
	}

}
