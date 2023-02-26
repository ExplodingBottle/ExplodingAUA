package io.github.explodingbottle.explodingaua.updating;

import java.io.Serializable;
import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;

public class InstallResults implements Serializable {

	private static final long serialVersionUID = 7285463774913714212L;

	private HashMap<UpdatePackage, SimpleEntry<InstallResult, Long>> results;

	public HashMap<UpdatePackage, SimpleEntry<InstallResult, Long>> getResults() {
		return results;
	}

	public void setResults(HashMap<UpdatePackage, SimpleEntry<InstallResult, Long>> results) {
		this.results = results;
	}

	public InstallResults() {
		this.results = new HashMap<UpdatePackage, SimpleEntry<InstallResult, Long>>();
	}

	public String toString() {
		StringBuilder resultsList = new StringBuilder();
		results.forEach((k, v) -> {
			resultsList.append(k.toStringNoArgs() + "," + v.getKey() + "," + v.getValue() + "\r\n");
		});
		return resultsList.toString();
	}

}
