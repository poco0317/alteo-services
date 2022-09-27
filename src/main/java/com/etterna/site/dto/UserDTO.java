package com.etterna.site.dto;

public class UserDTO {
	
	private String username;
	private String password;
	private String confirmedPassword;
	
	public String getConfirmedPassword() {
		return confirmedPassword;
	}
	public void setConfirmedPassword(String confirmedPassword) {
		this.confirmedPassword = confirmedPassword;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
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
