package com.etterna;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.etterna.site.services.NeoUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private NeoUserDetailsService userDetailsService;
	
	@Autowired
	private DaoAuthenticationProvider authProvider;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterAfter(new RedirectingFilter(), UsernamePasswordAuthenticationFilter.class)
			.authorizeRequests()
				.antMatchers("/", "/home", "/register").permitAll() // base site access to public
				.antMatchers("/user/*", "/packs*", "/leaderboard*").permitAll() // front end nav
				.antMatchers("/user/*/*").authenticated() // front end post protected
				.antMatchers("/login").not().authenticated() // cant login twice
				//.anyRequest().authenticated() // everything else
				.anyRequest().permitAll()
				.and()
			.formLogin()
				.loginPage("/login")
				.permitAll()
				.and()
				.logout()
				.permitAll();
	}
	
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
		auth.authenticationProvider(authProvider);
		//auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER").and().withUser("admin").password("{noop}admin").roles("ADMIN");
	}
}
