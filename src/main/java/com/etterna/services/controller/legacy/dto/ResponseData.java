package com.etterna.services.controller.legacy.dto;

public class ResponseData<T> {

	private T data;
	
	// either ErrorDTO or List<ErrorDTO>
	private Object errors;	
	
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
