package com.robolx.utilities;

import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class for reflection related functions. Helps with unit testing since Reflection classes are final
 * and cannot be mocked.
 */
public class ReflectionUtilities {

    public void forceSetValueOnField(Field field, Object instance, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(instance,value);
    }

    public boolean objectIsOfAnyType(Object object, Class... typesWanted) {
        boolean result = false;
        for(Class typeWanted : typesWanted){
            if(typeWanted.isInstance(object)){
                result = true;
                break;
            }
        }
        return result;
    }

    public <T> T createObjectProxy(Class<T> testSubjectClassType, MethodInterceptor methodInterceptor) {
        return (T)Enhancer.create(testSubjectClassType, methodInterceptor);
    }

    public boolean classIsOfAssignableForm(Class testSubjectClassType, Class<?> fragmentClass) {
        return fragmentClass.isAssignableFrom(testSubjectClassType);
    }

   public static abstract class MethodInterceptor implements org.mockito.cglib.proxy.MethodInterceptor{
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

            return interceptMethod(obj,method,args,proxy);
        }


       public abstract Object interceptMethod(Object obj, Method method, Object[] args, MethodProxy proxy);
    }
}
