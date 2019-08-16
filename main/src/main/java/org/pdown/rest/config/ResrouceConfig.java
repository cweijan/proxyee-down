package org.pdown.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author cweijan
 * @version 2019/8/16 11:42
 */
@Configuration
public class ResrouceConfig implements WebMvcConfigurer {

    /**
     * 静态资源路径配置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Field registrations = registry.getClass().getDeclaredField("registrations");
            registrations.setAccessible(true);
            ArrayList<ResourceHandlerRegistration> registrationList = (ArrayList<ResourceHandlerRegistration>) registrations.get(registry);
            registrationList.forEach(registration->{
                registration.addResourceLocations("classpath:/http/");
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
