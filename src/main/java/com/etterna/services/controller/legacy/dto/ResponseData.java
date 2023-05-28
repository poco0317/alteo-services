package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResponseData<T> {

	private T data;
	
	// either ErrorDTO or List<ErrorDTO>
	private Object errors;
	
	public static <T> ResponseData<T> ok(T data) {
		return new ResponseData<T>(data);
	}
	
	public ResponseData(T data) {
		this.data = data;
	}
	
	public ResponseData() {}
	
	public void error(int status) {
		ErrorDTO e = new ErrorDTO();
		e.setStatus(status);
		this.errors = e;
	}

	@Getter @Setter
	public class ErrorDTO {
		// http status
		private Integer status;
	}
}
