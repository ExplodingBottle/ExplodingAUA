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
