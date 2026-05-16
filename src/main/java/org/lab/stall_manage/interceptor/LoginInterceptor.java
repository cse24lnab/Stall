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

    private final String AUTHORISATION="Authorisation";
    private  final String BEARER="Bearer";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if(!(handler instanceof HandlerMethod)) {
            return true;
        }
        String auth = request.getHeader(AUTHORISATION);
        if(auth == null || auth.isEmpty())
        {
            log.info("令牌为空");
            response.setStatus(401);
            return  false;
        }
        String token=auth.substring(BEARER.length()).trim();
        if(token.isEmpty())
        {
            log.info("令牌为空");
            response.setStatus(401);
            return  false;
        }
        try {
            Map<String,Object> claims = JwtToken.parseToken(jwtProperties.getSecretKey(), token);
            CurrentUser currentUser=new CurrentUser();
            //强转成Integer的父类再取值，防止类型转换错误
            currentUser.setId(((Number) claims.get("id")).intValue());
            currentUser.setUsername((String) claims.get("username"));
            currentUser.setRole( UserRole.valueOf((String) claims.get("role")) );
            BaseContext.setCurrentUser(currentUser);
            RequireRole requireRole=((HandlerMethod) handler).getMethodAnnotation(RequireRole.class);
            this.checkRole(requireRole,currentUser);
        }
        catch (Exception ex)
        {
            //afterCompletion是返回true才会执行
            BaseContext.RemoveCurrentUser();
            log.info("令牌校验失败");
            response.setStatus(401);
            return  false;
        }
        log.info("令牌校验通过");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 Exception ex) throws Exception {
        BaseContext.RemoveCurrentUser();
    }

    private void checkRole(RequireRole requireRole,CurrentUser currentUser) throws Exception
    {
        if(requireRole == null)
        {
            return;
        }
        if(!Arrays.asList(requireRole.value()).contains(currentUser.getRole()))
        {
            throw new Exception("error");
        }
    }
}
