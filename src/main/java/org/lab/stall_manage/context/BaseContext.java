package org.lab.stall_manage.context;

import org.springframework.stereotype.Component;

//切面+threadlocal+拦截器
@Component
public class BaseContext {
    private static final ThreadLocal<CurrentUser> localUser=new ThreadLocal<>();

    public static void setCurrentUser(CurrentUser currUser)
    {
        localUser.set(currUser);
    }

    public static CurrentUser getCurrentUser()
    {
        return localUser.get();
    }

    public static void RemoveCurrentUser()
    {
        localUser.remove();
    }
}
