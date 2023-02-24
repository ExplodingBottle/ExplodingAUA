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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	private File logFile;
	private FileOutputStream fos;

	public Logger(File logFile) {
		this.logFile = logFile;
	}

	public void open() {
		try {
			fos = new FileOutputStream(logFile, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void write(String componentName, String message) {
		LocalDateTime ldt = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String toWrite = dtf.format(ldt) + " [" + componentName + "]: " + message + "\r\n";
		System.out.print(toWrite);
		if (fos != null) {
			try {
				fos.write(toWrite.getBytes(), 0, toWrite.getBytes().length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
