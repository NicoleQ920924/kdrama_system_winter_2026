package com.kdrama.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

import com.kdrama.backend.enums.Role;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    // Expect client to send header `X-User-Role` with values ADMIN or USER
    private Role roleFromHeader(HttpServletRequest request) {
        String v = request.getHeader("X-User-Role");
        if (v == null) return null;
        try {
            return Role.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();

        RequireRole ann = method.getAnnotation(RequireRole.class);
        if (ann == null) {
            // check class-level
            ann = hm.getBeanType().getAnnotation(RequireRole.class);
        }

        if (ann == null) {
            return true; // no restriction
        }

        Role role = roleFromHeader(request);
        if (role == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid X-User-Role header");
            return false;
        }

        for (Role allowed : ann.value()) {
            if (allowed == role) {
                return true;
            }
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient role");
        return false;
    }
}
