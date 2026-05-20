package org.lab.stall_manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lab.stall_manage.annotation.RequireRole;
import org.lab.stall_manage.config.JwtProperties;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.interceptor.LoginInterceptor;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.utils.JwtToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginInterceptorTest {

    private LoginInterceptor loginInterceptor;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        loginInterceptor = new LoginInterceptor();
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("test-secret-key-1234567890");
        jwtProperties.setTime(60000L);
        ReflectionTestUtils.setField(loginInterceptor, "jwtProperties", jwtProperties);
    }

    @AfterEach
    void tearDown() {
        BaseContext.RemoveCurrentUser();
    }

    @Test
    void preHandleReturnsTrueWhenHandlerIsNotHandlerMethod() throws Exception {
        boolean allowed = loginInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertTrue(allowed);
    }

    @Test
    void preHandleReturnsUnauthorizedWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleReturnsUnauthorizedWhenAuthorizationHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleReturnsUnauthorizedWhenBearerTokenIsBlank() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleReturnsUnauthorizedWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleReturnsUnauthorizedWhenTokenIsExpired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(UserRole.ADMIN, -1000L, jwtProperties.getSecretKey()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleReturnsUnauthorizedWhenTokenSignatureIsWrong() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(UserRole.ADMIN, 60000L, "wrong-secret-key"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleAllowsRequestAndStoresCurrentUserWhenNoRoleRequired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(UserRole.USER, jwtProperties.getTime(), jwtProperties.getSecretKey()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new OpenController(), "open"));

        assertTrue(allowed);
        CurrentUser currentUser = BaseContext.getCurrentUser();
        assertEquals(1, currentUser.getId());
        assertEquals("alice01", currentUser.getUsername());
        assertEquals(UserRole.USER, currentUser.getRole());
    }

    @Test
    void preHandleAllowsRequestWhenMethodRoleMatches() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(UserRole.MERCHANT, jwtProperties.getTime(), jwtProperties.getSecretKey()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new MethodRoleController(), "merchantOnly"));

        assertTrue(allowed);
    }

    @Test
    void preHandleAllowsRequestWhenClassRoleMatches() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(UserRole.ADMIN, jwtProperties.getTime(), jwtProperties.getSecretKey()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new ClassRoleController(), "dashboard"));

        assertTrue(allowed);
    }

    @Test
    void preHandleReturnsForbiddenWhenRoleDoesNotMatch() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(UserRole.USER, jwtProperties.getTime(), jwtProperties.getSecretKey()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = loginInterceptor.preHandle(request, response, methodHandler(new MethodRoleController(), "merchantOnly"));

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
    }

    @Test
    void afterCompletionClearsCurrentUser() throws Exception {
        BaseContext.setCurrentUser(new CurrentUser(1, "alice01", UserRole.USER));

        loginInterceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);

        assertNull(BaseContext.getCurrentUser());
    }

    private HandlerMethod methodHandler(Object bean, String methodName) throws NoSuchMethodException {
        return new HandlerMethod(bean, bean.getClass().getMethod(methodName));
    }

    private String createToken(UserRole role, long time, String secretKey) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 1);
        claims.put("username", "alice01");
        claims.put("role", role.name());
        return JwtToken.createToken(secretKey, time, claims);
    }

    static class OpenController {
        public void open() {
        }
    }

    static class MethodRoleController {
        @RequireRole({UserRole.MERCHANT})
        public void merchantOnly() {
        }
    }

    @RequireRole({UserRole.ADMIN})
    static class ClassRoleController {
        public void dashboard() {
        }
    }
}
