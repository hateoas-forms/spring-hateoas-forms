package com.github.hateoas.forms.spring;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.web.bind.annotation.RequestMapping;

public class CachedMappingDiscoverer implements MappingDiscoverer {

	private final MappingDiscoverer discoverer = new AnnotationMappingDiscoverer(RequestMapping.class);

	private final Map<Key, String> cached = new ConcurrentHashMap<Key, String>();

	private static final int CACHE_SIZE = 200;

	public CachedMappingDiscoverer() {

	}

	private void doPut(final Key key, final String mapping) {
		if (cached.size() > CACHE_SIZE) {
			cached.clear();
		}
		cached.put(key, mapping);
	}

	@Override
	public String getMapping(final Class<?> type) {
		Key key = new Key(type, null);
		String mapping = cached.get(key);
		if (mapping == null) {
			mapping = discoverer.getMapping(type);
			doPut(key, mapping);
		}
		return mapping;
	}

	@Override
	public String getMapping(final Method method) {
		return getMapping(method.getDeclaringClass(), method);
	}

	@Override
	public String getMapping(final Class<?> type, final Method method) {
		Key key = new Key(type, method);
		String mapping = cached.get(key);
		if (mapping == null) {
			mapping = discoverer.getMapping(type, method);
			doPut(key, mapping);
		}
		return mapping;
	}

	class Key {
		Class<?> clazz;

		Method method;

		Key(final Class<?> clazz, final Method method) {
			this.clazz = clazz;
			this.method = method;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (clazz == null ? 0 : clazz.hashCode());
			result = prime * result + (method == null ? 0 : method.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Key other = (Key) obj;
			if (clazz == null) {
				if (other.clazz != null) {
					return false;
				}
			}
			else if (!clazz.equals(other.clazz)) {
				return false;
			}
			if (method == null) {
				if (other.method != null) {
					return false;
				}
			}
			else if (!method.equals(other.method)) {
				return false;
			}
			return true;
		}

		private CachedMappingDiscoverer getOuterType() {
			return CachedMappingDiscoverer.this;
		}
	}

}
