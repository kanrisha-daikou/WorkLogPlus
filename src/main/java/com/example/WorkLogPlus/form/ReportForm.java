package com.example.WorkLogPlus.form;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ReportForm {

	@NotNull(message = "日付は年月日の全てが必須項目です")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reportDate;
	
	@NotBlank(message = "業務内容は必須項目です")
	@Size(max = 1000, message = "業務内容は最大１０００文字以内で入力してください")
	private String content;

	@Size(max = 1000, message = "所感は最大１０００文字以内で入力してください")
	private String impressions;

	@Size(max = 1000, message = "課題は最大１０００文字以内で入力してください")
	private String issues;
}
