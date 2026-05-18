package com.example.OnlineMedicalStore.config;

import com.example.OnlineMedicalStore.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (isPublicPath(uri)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            if (isAdminOnlyPath(uri)) {
                response.sendRedirect("/auth/admin/login?error=Please+login+first");
            } else {
                response.sendRedirect("/auth/customer/login?error=Please+login+first");
            }
            return false;
        }

        if (isAdminOnlyPath(uri) && !"ADMIN".equals(user.getRoleString())) {
            response.sendRedirect("/?error=Access+denied");
            return false;
        }

        return true;
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("/auth")
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.equals("/")
                || isPublicMedicinePath(uri)
                || uri.startsWith("/reviews/medicine");
    }

    private boolean isPublicMedicinePath(String uri) {
        return uri.equals("/medicines") || uri.matches("/medicines/\\d+");
    }

    private boolean isAdminOnlyPath(String uri) {
        return uri.startsWith("/admin")
                || uri.startsWith("/reviews/admin")
                || uri.equals("/medicines/add")
                || uri.matches("/medicines/\\d+/edit")
                || uri.matches("/medicines/\\d+/delete")
                || uri.matches("/prescriptions/\\d+/review");
    }
}
