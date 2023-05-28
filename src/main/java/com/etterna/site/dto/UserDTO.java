package com.etterna.site.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDTO {
	
	private String username;
	private String password;
	private String confirmedPassword;
	
	public boolean validUsername() {
		return username != null && !username.isBlank();
	}
	public boolean validPassword() {
		return password != null && !password.isBlank();
	}
	public boolean validConfirmedPassword() {
		return confirmedPassword != null && !confirmedPassword.isBlank() && confirmedPassword.equals(password);
	}

}
