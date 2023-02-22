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
