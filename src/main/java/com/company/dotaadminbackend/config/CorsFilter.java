package com.company.dotaadminbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String origin = request.getHeader("Origin");
        
        // 허용된 도메인 체크
        if (isAllowedOrigin(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", 
            "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cache-Control, " +
            "Pragma, Expires, Last-Modified, If-Modified-Since, If-None-Match");
        response.setHeader("Access-Control-Expose-Headers", 
            "Authorization, Set-Cookie, Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // OPTIONS 요청 처리
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(req, res);
    }
    
    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        
        return origin.equals("https://dota-task.shop") ||
               origin.endsWith(".dota-task.shop") ||
               origin.startsWith("http://localhost:") ||
               origin.startsWith("https://localhost:") ||
               origin.startsWith("http://127.0.0.1:") ||
               origin.startsWith("https://127.0.0.1:") ||
               origin.endsWith(".vercel.app") ||
               origin.endsWith(".netlify.app");
    }
}
