package com.hybris.caas.test.integration.config;

import com.hybris.caas.security.config.ServiceWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig implements ServiceWebSecurityConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger(TestSecurityConfig.class);

	@Override
	public void configure(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()
				.antMatchers("/security/authenticated").fullyAuthenticated()
//				.antMatchers("/security/authorized").hasAuthority("read_only") // Secured via annotation for additional testing
				.antMatchers("/security/public").permitAll()
				.antMatchers("/**").permitAll();
		LOG.info("CaaS Test - configured security for endpoints at /security");
	}
}