package com.etterna;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.etterna.services.RoleService;
import com.etterna.site.services.NeoUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private NeoUserDetailsService userDetailsService;
	
	@Autowired
	private DaoAuthenticationProvider authProvider;
	
	@Autowired
	private RoleService roles;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterAfter(new RedirectingFilter(), UsernamePasswordAuthenticationFilter.class)
			.authorizeRequests().expressionHandler(webSecurityExpressionHandler2())
				.antMatchers("/favicon.ico").permitAll()
				.antMatchers("/", "/home", "/register").permitAll() // base site access to public
				.antMatchers("/user/*", "/packs*", "/leaderboard*").permitAll() // front end nav
				.antMatchers("/user/*/*").authenticated() // front end post protected
				.antMatchers("/login").not().authenticated() // cant login twice
				.antMatchers("/admin").hasRole("ADMIN")
				.anyRequest().permitAll() // everything else
				.and()
			.formLogin()
				.loginPage("/login")
				.permitAll()
				.and()
				.logout()
				.permitAll()
				.and()
			.exceptionHandling()
				.accessDeniedHandler(accessDenied());
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
		auth.authenticationProvider(authProvider);
		//auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER").and().withUser("admin").password("{noop}admin").roles("ADMIN");
	}
	
	@Bean
	public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler2() {
		DefaultWebSecurityExpressionHandler h = new DefaultWebSecurityExpressionHandler();
		h.setRoleHierarchy(roles.roleHierarchy());
		return h;
	}
	
	@Bean
	public AccessDeniedHandler accessDenied() {
		return new AccessDeniedHandler() {
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
					AccessDeniedException accessDeniedException) throws IOException, ServletException {
				response.sendRedirect("/");
			}
		};
	}
	
}
