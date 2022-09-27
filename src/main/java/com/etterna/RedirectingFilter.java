package com.etterna;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class RedirectingFilter extends GenericFilterBean {

	private boolean isAuthenticated() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication == null || AnonymousAuthenticationToken.class.
	      isAssignableFrom(authentication.getClass())) {
	        return false;
	    }
	    return authentication.isAuthenticated();
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		if (isAuthenticated() && ("/register".equals(req.getRequestURI()) || "/login".equals(req.getRequestURI()))) {
			String redir = resp.encodeRedirectURL(req.getContextPath() + "/home");
			resp.setStatus(HttpStatus.SC_TEMPORARY_REDIRECT);
			resp.setHeader("Location",  redir);
		}
		
		chain.doFilter(req, resp);
	}
	
}
