package com.example.WorkLogPlus.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CommentForm {
	
	@NotBlank
	@Size(max = 1000, message = "最大１０００文字以内で入力してください")
	private String body;
}
