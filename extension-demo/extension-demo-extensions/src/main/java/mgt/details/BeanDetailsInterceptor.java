package mgt.details;

import java.util.logging.Logger;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@BeanDetailsInterceptorBinding
public class BeanDetailsInterceptor {
    @AroundInvoke
    public Object intercept(final InvocationContext ctx) throws Exception {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).
                warning(">" + ctx.getTarget().toString());
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).
                warning(">" + ctx.getMethod().toString());
        return ctx.proceed();
    }
}
