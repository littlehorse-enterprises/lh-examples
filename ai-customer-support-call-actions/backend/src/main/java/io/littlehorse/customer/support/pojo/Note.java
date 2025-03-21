package io.littlehorse.customer.support.pojo;

public class Note {
	private boolean containsSensitiveInfo;
	private long followUpTimestamp;
	private String text;

	public Note(boolean containsSensitiveInfo, long followUpTimestamp, String text) {
		this.containsSensitiveInfo = containsSensitiveInfo;
		this.followUpTimestamp = followUpTimestamp;
		this.text = text;
	}

	public Note() {
	}

	public boolean getContainsSensitiveInfo() {
		return containsSensitiveInfo;
	}

	public long getFollowUpTimestamp() {
		return followUpTimestamp;
	}

	public String getText() {
		return text;
	}

	public void setContainsSensitiveInfo(boolean containsSensitiveInfo) {
		this.containsSensitiveInfo = containsSensitiveInfo;
	}

	public void setFollowUpTimestamp(long followUpTimestamp) {
		this.followUpTimestamp = followUpTimestamp;
	}

	public void setText(String text) {
		this.text = text;
	}

}
