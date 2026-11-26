package com.example.WorkLogPlus.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j //←クラス内で「log.error」や「log.warn」が使えるようになる
public class GlobalExceptionHandler {
//	isAjaxRequest()	を最下部に定義
	
	
//	403 権限エラー（AccessDeniedException）
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request){
		//	コンソールに出すエラーログ
		log.warn("アクセス拒否が発生しました。URL: {}, 原因: {}", request.getRequestURI(), ex.getMessage());
		
		//	エラーの原因が「JavaScriptからの通信（fetch）」の場合
		if(isAjaxRequest(request)) {
			
			ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "エラー：この操作を行う権限がありません。");
			problem.setTitle("Access Denied"); // エラーのタイトル
			
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
		}
		
		//	エラーの原因が「普通の画面遷移（URL直叩き）」の場合
		return "error/403";
	}
	
	
	
//	404 存在しないページやリソースへのアクセス
	@ExceptionHandler({NoHandlerFoundException.class, EntityNotFoundException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Object handleNotFound(Exception ex, HttpServletRequest request) {
		
		log.warn("リソースが見つかりません。URL: {}", request.getRequestURI());
		
		if(isAjaxRequest(request)) {
			
			ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "エラー：対象のデータ、またはページが見つかりません。");
			problem.setTitle("Not Found");
			
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
		}
		
		return "error/404";
	}
	
	
	
//	500 サーバー内部で発生した予期せぬエラー
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Object handleGeneralError(Exception ex, HttpServletRequest request) {
		
		log.error("サーバー内で予期せぬエラー（500）が発生しました。URL: {}", request.getRequestURI(), ex);
		
		if(isAjaxRequest(request)) {
			
			ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "サーバー内で予期せぬエラーが発生しました。");
			problem.setTitle("Internal Server Error");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
		}
		
		return "error/500";
	}
	
	
	
//	通信が JS（Fetch / AJAX）からのモノか判定するメソッド
	private boolean isAjaxRequest(HttpServletRequest request) {
		String requestedWith = request.getHeader("X-Requested-With");
		
		return "XMLHttpRequest".equals(requestedWith) || 
				(request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));
	}
}
