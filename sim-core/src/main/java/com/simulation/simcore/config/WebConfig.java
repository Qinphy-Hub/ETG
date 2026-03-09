package com.simulation.simcore.config;

import com.simulation.simcore.interceptors.LoginInterceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private LoginInterceptors loginInterceptors;
    @Autowired
    public void setLoginInterceptors(LoginInterceptors loginInterceptors) {
        this.loginInterceptors = loginInterceptors;
    }
    // 获取配置的资源路径
    @Value("${workspace.path}")
    private String workspacePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 开放访问资源路径
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + workspacePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // token 拦截器白名单
        registry.addInterceptor(loginInterceptors).excludePathPatterns("/user/login", "/user/register", "/user/verification-code", "/user/forget-password");
    }
}
