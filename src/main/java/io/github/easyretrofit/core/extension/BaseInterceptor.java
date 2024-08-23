package io.github.easyretrofit.core.extension;

import io.github.easyretrofit.core.RetrofitResourceContext;
import io.github.easyretrofit.core.util.AntPathMatcher;
import io.github.easyretrofit.core.util.PathMatcher;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Abstract class of Interceptor, The inner interceptor needs to inherit it
 *
 * @author liuziyuan
 */
public abstract class BaseInterceptor implements Interceptor {

    protected RetrofitResourceContext context;
    private Class<?>[] defaultScopeClasses;
    private String[] include;
    private String[] exclude;
    private final PathMatcher pathMatcher = new AntPathMatcher();


    protected BaseInterceptor() {
    }

    protected BaseInterceptor(RetrofitResourceContext context) {
        this.context = context;
    }

    @Override
    public final Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();
        Method method = this.getRequestMethod(request);
        String clazzName = this.getClazzNameByMethod(method);
        if (!functionInDefaultScope(clazzName) || functionInDefaultScope(clazzName) && isMatch(exclude, path)) {
            return chain.proceed(request);
        }
        if (!functionInDefaultScope(clazzName) || functionInDefaultScope(clazzName) && include != null && !isMatch(include, path)) {
            return chain.proceed(request);
        }
        return executeIntercept(chain);
    }

    /**
     * execute intercept for OKHttpClient Interceptor
     *
     * @param chain Chain
     * @return Response
     */
    protected abstract Response executeIntercept(Chain chain) throws IOException;

    private boolean functionInDefaultScope(String className) {
        if (defaultScopeClasses == null) {
            return true;
        }
        for (Class<?> defaultScopeClass : defaultScopeClasses) {
            if (defaultScopeClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMatch(String[] patterns, String path) {
        if (patterns == null) {
            return false;
        }
        for (String pattern : patterns) {
            boolean match = pathMatcher.match(pattern, path);
            if (match) {
                return true;
            }
        }
        return false;
    }


    public void setInclude(String[] include) {
        this.include = include;
    }

    public void setExclude(String[] exclude) {
        this.exclude = exclude;
    }

    public void setDefaultScopeClasses(Class<?>[] defaultScopeClasses) {
        this.defaultScopeClasses = defaultScopeClasses;
    }

    protected String getClazzNameByMethod(Method method) {
        return method.getDeclaringClass().getName();
    }

    protected Method getRequestMethod(Request request) {
        return Objects.requireNonNull(request.tag(Invocation.class)).method();
    }

}
