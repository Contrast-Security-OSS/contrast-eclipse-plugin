package com.contrastsecurity.ide.eclipse.core.extended;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TraceStatusRequest {
	
	private List<String> traces;
	private String status;
	private String substatus;
	private String note;
	@SerializedName("comment_preference")
	private boolean commentPrefrence;
	
	public List<String> getTraces() {
		return traces;
	}
	
	public void setTraces(List<String> traces) {
		this.traces = traces;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getSubstatus() {
		return substatus;
	}
	
	public void setSubstatus(String substatus) {
		this.substatus = substatus;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public boolean isCommentPrefrence() {
		return commentPrefrence;
	}
	
	public void setCommentPrefrence(boolean commentPrefrence) {
		this.commentPrefrence = commentPrefrence;
	}

}
