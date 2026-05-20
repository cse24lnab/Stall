package org.lab.stall_manage.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.annotation.RequireRole;
import org.lab.stall_manage.config.JwtProperties;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Map;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        try {
            String token = this.getToken(request);
            Map<String,Object> claims = JwtToken.parseToken(jwtProperties.getSecretKey(), token);
            CurrentUser currentUser = this.createCurrentUser(claims);
            BaseContext.setCurrentUser(currentUser);
            RequireRole requireRole = this.getRequireRole(handlerMethod);
            this.checkRole(requireRole, currentUser);
            log.info("令牌校验通过");
            return true;
        }
        catch (IllegalAccessException ex)
        {
            BaseContext.RemoveCurrentUser();
            //todo 异常
            log.info("权限不足", ex);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        catch (Exception ex)
        {
            BaseContext.RemoveCurrentUser();
            //todo 异常
            log.info("令牌校验失败", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                  Exception ex) throws Exception {
        BaseContext.RemoveCurrentUser();
    }

    private void checkRole(RequireRole requireRole, CurrentUser currentUser) throws IllegalAccessException
    {
        if (requireRole == null)
        {
            return;
        }
        if (!Arrays.asList(requireRole.value()).contains(currentUser.getRole()))
        {
            throw new IllegalAccessException("无访问权限");
        }
    }

    private String getToken(HttpServletRequest request)
    {
        String auth = request.getHeader(AUTHORIZATION);
        if (auth == null || auth.isBlank())
        {
            throw new IllegalArgumentException("令牌为空");
        }
        if (!auth.startsWith(BEARER_PREFIX))
        {
            throw new IllegalArgumentException("令牌格式错误");
        }

        String token = auth.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank())
        {
            throw new IllegalArgumentException("令牌为空");
        }

        return token;
    }

    private CurrentUser createCurrentUser(Map<String,Object> claims)
    {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(((Number) claims.get("id")).intValue());
        currentUser.setUsername((String) claims.get("username"));
        currentUser.setRole(UserRole.valueOf((String) claims.get("role")));
        return currentUser;
    }

    private RequireRole getRequireRole(HandlerMethod handlerMethod)
    {
        RequireRole methodRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodRole != null)
        {
            return methodRole;
        }
        return handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }
}
