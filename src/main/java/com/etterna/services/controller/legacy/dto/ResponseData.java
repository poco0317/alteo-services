package com.etterna.services.controller.legacy.dto;

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
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Object getErrors() {
		return errors;
	}

	public void setErrors(Object errors) {
		this.errors = errors;
	}
	
	public void error(int status) {
		ErrorDTO e = new ErrorDTO();
		e.setStatus(status);
		this.errors = e;
	}

	public class ErrorDTO {
		// http status
		private Integer status;

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}
		
	}
	
}
