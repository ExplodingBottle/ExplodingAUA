package io.github.explodingbottle.explodingaua.updating;

import java.io.Serializable;

public class ProgramInformation implements Serializable {

	private static final long serialVersionUID = -6066883768942373162L;

	private String pHash;
	private String pPath;
	private String pId;

	public ProgramInformation(String pHash, String pPath, String pId) {
		this.pHash = pHash;
		this.pPath = pPath;
		this.pId = pId;
	}

	public String toString() {
		return pPath + "=" + pId + ";" + pHash;
	}

	public String getpHash() {
		return pHash;
	}

	public void setpHash(String pHash) {
		this.pHash = pHash;
	}

	public String getpPath() {
		return pPath;
	}

	public void setpPath(String pPath) {
		this.pPath = pPath;
	}

	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

}
