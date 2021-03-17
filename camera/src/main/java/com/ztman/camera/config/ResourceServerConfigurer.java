package com.ztman.camera.config;

import com.ztman.common.security.component.ResourceAuthExceptionEntryPoint;
import com.ztman.common.security.component.SparkAccessDeniedHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;


@Configuration
@EnableResourceServer
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {
    private final SparkAccessDeniedHandler sparkAccessDeniedHandler;
    private final ResourceAuthExceptionEntryPoint resourceAuthExceptionEntryPoint;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/actuator/**", "/v2/api-docs", "/platform/**", "/index", "/**").permitAll()
                .anyRequest().authenticated()
                .and().csrf().disable();
    }

    /**
     * why add  resourceId
     * https://stackoverflow.com/questions/28703847/how-do-you-set-a-resource-id-for-a-token
     *
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.authenticationEntryPoint(resourceAuthExceptionEntryPoint)
                .accessDeniedHandler(sparkAccessDeniedHandler);
    }
}
