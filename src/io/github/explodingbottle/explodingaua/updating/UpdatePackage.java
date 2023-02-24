package io.github.explodingbottle.explodingaua.updating;

public class UpdatePackage {

	private ProgramInformation linkedProgram;
	private String discoveredVerson;
	private String displayName;
	private String latestVersion;
	private String dlLocation;
	private String mode;
	private boolean requiresUpdate;

	public UpdatePackage(ProgramInformation linkedProgram, String discoveredVerson, String displayName,
			String latestVersion, String dlLocation, String mode, boolean requiresUpdate) {
		this.linkedProgram = linkedProgram;
		this.discoveredVerson = discoveredVerson;
		this.displayName = displayName;
		this.latestVersion = latestVersion;
		this.dlLocation = dlLocation;
		this.mode = mode;
		this.requiresUpdate = requiresUpdate;
	}

	public String toString() {
		return "Program=" + linkedProgram.getpPath() + ",Version=" + discoveredVerson + ",DispName=" + displayName
				+ ",Latest=" + latestVersion + ",IsUpdateRequired=" + requiresUpdate;
	}

	public String toStringNoArgs() {
		return linkedProgram.getpPath() + "," + discoveredVerson + "," + displayName + "," + latestVersion + ","
				+ requiresUpdate;
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
