package io.github.easyretrofit.core.generator;

import io.github.easyretrofit.core.Generator;
import io.github.easyretrofit.core.RetrofitResourceContext;
import io.github.easyretrofit.core.extension.BaseInterceptor;
import io.github.easyretrofit.core.resource.RetrofitInterceptorBean;
import okhttp3.Interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Generate OkHttpInterceptor instance
 *
 * @author liuziyuan
 */
public abstract class OkHttpInterceptorGenerator implements Generator<Interceptor> {
    private final Class<? extends BaseInterceptor> interceptorClass;
    private final RetrofitInterceptorBean retrofitInterceptor;
    private final RetrofitResourceContext resourceContext;

    public OkHttpInterceptorGenerator(RetrofitInterceptorBean retrofitInterceptor, RetrofitResourceContext resourceContext) {
        this.retrofitInterceptor = retrofitInterceptor;
        this.interceptorClass = retrofitInterceptor.getHandler();
        this.resourceContext = resourceContext;
    }

    public abstract BaseInterceptor buildInjectionObject(Class<? extends BaseInterceptor> clazz);

    @Override
    public Interceptor generate() {
        BaseInterceptor interceptor = buildInjectionObject(retrofitInterceptor.getHandler());
        BaseInterceptor clonedInterceptor = null;
        if (interceptor != null) {
            clonedInterceptor = interceptor.clone();
        }
        if (interceptor == null && interceptorClass != null) {
            Constructor<? extends BaseInterceptor> constructor;
            BaseInterceptor interceptorInstance;
            try {
                constructor = interceptorClass.getConstructor(RetrofitResourceContext.class);
                interceptorInstance = constructor.newInstance(resourceContext);
            } catch (NoSuchMethodException exception) {
                try {
                    interceptorInstance = interceptorClass.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            clonedInterceptor = interceptorInstance;
        }
        if (clonedInterceptor != null) {
            clonedInterceptor.setInclude(retrofitInterceptor.getInclude());
            clonedInterceptor.setExclude(retrofitInterceptor.getExclude());
            clonedInterceptor.setDefaultScopeClasses(retrofitInterceptor.getDefaultScopeClasses());
        }
        return clonedInterceptor;

    }
}
