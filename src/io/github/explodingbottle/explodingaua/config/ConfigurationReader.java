package io.github.explodingbottle.explodingaua.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import io.github.explodingbottle.explodingaua.AgentMain;

public class ConfigurationReader {

	private String fileName;
	private Manifest manifest;

	private File file;

	public ConfigurationReader(String fileName) {
		this.fileName = fileName;
	}

	public ConfigurationReader(File file) {
		this.file = file;
	}

	public boolean loadConfiguration() {
		boolean success = false;
		InputStream input = null;
		if (file != null) {
			try {
				input = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				AgentMain.getLogger().write("CFG", "Failed to load the configuration file.");
			}
		} else {
			input = getClass().getClassLoader().getResourceAsStream(fileName);
		}
		if (input != null) {
			try {
				manifest = new Manifest(input);
				success = true;
			} catch (IOException e1) {
				AgentMain.getLogger().write("CFG", "Failed to load configuration.");
			}
			try {
				input.close();
			} catch (IOException e) {
				AgentMain.getLogger().write("CFG", "Failed to close configuration.");
			}
		}
		return success;
	}

	public Manifest getConfiguration() {
		return manifest;
	}

}
