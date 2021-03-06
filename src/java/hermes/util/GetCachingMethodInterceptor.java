/* 
 * Copyright 2003,2004,2005 Colin Crist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package hermes.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.log4j.Logger;

/**
 * An interceptor that caches the parameter for a setter and returns it from a
 * getter.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public final class GetCachingMethodInterceptor implements MethodInterceptor {
	private static final Logger log = Logger.getLogger(GetCachingMethodInterceptor.class);

	private final Map<String, Object> properties = new HashMap<String, Object>();

	private Object altObject;

	public GetCachingMethodInterceptor() {
		
	}
	public GetCachingMethodInterceptor(Object altObject) {
		this.altObject = altObject;
	}

	public final Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
		if (ReflectUtils.isGetter(method)) {
			return properties.get(ReflectUtils.getPropertyName(method));
		}
		
		Object rval = null ;
		if (altObject != null) {
			try {
				Method m = altObject.getClass().getMethod(method.getName(), method.getParameterTypes()) ;
				rval = m.invoke(altObject, args) ;

			} catch (NoSuchMethodException ex) {
				log.info("ignoring: " + ex) ;
			} catch (InvocationTargetException ex) {
				throw ex.getCause() ;
			}
		} else {
			 rval = proxy.invokeSuper(object, args);
		}

		if (ReflectUtils.isSetter(method)) {
			String propertyName = ReflectUtils.getPropertyName(method);

			properties.put(propertyName, args[0]);

//			log.debug("GetCachingMethodInterceptor setter for " + ReflectUtils.getPropertyName(method) + " with " + args[0]);
		}

		return rval;
	}
}
