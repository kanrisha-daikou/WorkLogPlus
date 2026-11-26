package com.example.WorkLogPlus.dto;

public record TaskDto(
		Long id,
		Long userId,
		String username,
		String title,
		String status,
		String statusLabel,
		String dueDate,
		String inputDueDate,
		String createdAt,
		String updatedAt){
}
