package com.example.WorkLogPlus.entity;

public enum TaskStatus {
	
	TODO("未着手"),
	DOING("進行中"),
	DONE("完了済");
	
	private final String label;
	
	TaskStatus(String label){
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}
}
