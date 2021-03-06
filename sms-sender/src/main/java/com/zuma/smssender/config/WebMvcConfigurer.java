package com.zuma.smssender.config;

import com.zuma.smssender.interceptor.IpInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * author:ZhengXing
 * datetime:2017/10/18 0018 17:00
 * 拦截器配置
 */
@Configuration
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器
        registry.addInterceptor(new IpInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
