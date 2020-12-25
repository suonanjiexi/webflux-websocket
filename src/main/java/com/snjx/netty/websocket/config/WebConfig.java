package com.snjx.netty.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;

import java.util.concurrent.TimeUnit;

/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO
 * @date 12/24/20 2:44 下午
 */
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**/**")
                .addResourceLocations("classpath:/**/**")
                .addResourceLocations("classpath:/static/**")
                .addResourceLocations("classpath:/resources/**")
                .addResourceLocations("classpath:/resources/static/**")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }
    //引入spring-boot-starter-thymeleaf自动会注入该bean
    @Autowired
    private ThymeleafReactiveViewResolver thymeleafReactiveViewResolver;

    /**
     * 加入thymeleaf试图解析器，不然找不到view name
     *
     * @param registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafReactiveViewResolver);
    }
}