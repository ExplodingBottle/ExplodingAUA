package io.github.explodingbottle.explodingaua.updating;

import io.github.explodingbottle.explodingaua.AgentMain;

public class UpdatePackage {

	private ProgramInformation linkedProgram;
	private String discoveredVerson;
	private String displayName;
	private String latestVersion;
	private String dlLocation;
	private String mode;
	private String description;
	private boolean requiresUpdate;

	public UpdatePackage(ProgramInformation linkedProgram, String discoveredVerson, String displayName,
			String latestVersion, String dlLocation, String mode, boolean requiresUpdate, String description) {
		this.linkedProgram = linkedProgram;
		this.discoveredVerson = discoveredVerson;
		this.displayName = displayName;
		this.latestVersion = latestVersion;
		this.dlLocation = dlLocation;
		this.mode = mode;
		this.requiresUpdate = requiresUpdate;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return "Program=" + linkedProgram.getpPath() + ",Version=" + discoveredVerson + ",DispName=" + displayName
				+ ",Latest=" + latestVersion + ",IsUpdateRequired=" + requiresUpdate + ",IsUpdateRequired="
				+ description;
	}

	public String toStringNoArgs() {
		String path = "null";
		if (Boolean.parseBoolean(
				AgentMain.getConfigurationReader().getConfiguration().getMainAttributes().getValue("SendFilePaths"))) {
			path = linkedProgram.getpPath();
		}
		return path + "," + discoveredVerson + "," + displayName + "," + latestVersion + ","
				+ requiresUpdate + "," + description;
	}

	public boolean isRequiresUpdate() {
		return requiresUpdate;
	}

	public void setRequiresUpdate(boolean requiresUpdate) {
		this.requiresUpdate = requiresUpdate;
	}

	public ProgramInformation getLinkedProgram() {
		return linkedProgram;
	}

	public void setLinkedProgram(ProgramInformation linkedProgram) {
		this.linkedProgram = linkedProgram;
	}

	public String getDiscoveredVerson() {
		return discoveredVerson;
	}

	public void setDiscoveredVerson(String discoveredVerson) {
		this.discoveredVerson = discoveredVerson;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public String getDlLocation() {
		return dlLocation;
	}

	public void setDlLocation(String dlLocation) {
		this.dlLocation = dlLocation;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
