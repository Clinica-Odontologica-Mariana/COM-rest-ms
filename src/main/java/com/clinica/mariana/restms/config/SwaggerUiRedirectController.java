package com.clinica.mariana.restms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerUiRedirectController {

    @Bean
    ServletRegistrationBean<jakarta.servlet.http.HttpServlet> swaggerUiRootRedirectServlet(
            @Value("${spring.mvc.servlet.path:}") String servletPath
    ) {
        jakarta.servlet.http.HttpServlet redirectServlet = new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp)
                    throws java.io.IOException {
                String normalizedServletPath = servletPath == null ? "" : servletPath;
                if (normalizedServletPath.endsWith("/")) {
                    normalizedServletPath = normalizedServletPath.substring(0, normalizedServletPath.length() - 1);
                }
                resp.sendRedirect(normalizedServletPath + "/swagger-ui.html");
            }

            @Override
            protected void doHead(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp)
                    throws java.io.IOException {
                doGet(req, resp);
            }
        };

        ServletRegistrationBean<jakarta.servlet.http.HttpServlet> registrationBean =
                new ServletRegistrationBean<>(redirectServlet, "/swagger-ui.html");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }
}
