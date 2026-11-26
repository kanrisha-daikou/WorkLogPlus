package com.example.WorkLogPlus.exception;

public class DupeReportException extends RuntimeException {

//	日報の重複登録エラーを表す例外(ReportService内での判定次第で呼ばれるかも)
	public DupeReportException(String message) {
		super(message);
	}
}
