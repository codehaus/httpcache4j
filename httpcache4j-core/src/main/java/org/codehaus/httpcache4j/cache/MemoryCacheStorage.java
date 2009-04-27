/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.payload.Payload;


import java.net.URI;
import java.util.*;

/**
 * In Memory implementation of a cache storage.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class MemoryCacheStorage implements CacheStorage {

    protected Map<URI, CacheValue> cache;

    public MemoryCacheStorage() {
        this(1000);
    }

    public MemoryCacheStorage(int capacity) {
        cache = new InvalidateOnRemoveLRUHashMap(capacity);
    }

    public synchronized void put(URI requestURI, Vary vary, CacheItem cacheItem) {
        if (cache.containsKey(requestURI)) {
            CacheValue value = cache.get(requestURI);            
            value.add(vary, cacheItem);
            cache.put(requestURI, value);
        }
        else {
            cache.put(requestURI, new CacheValue(Collections.singletonMap(vary, cacheItem)));
        }
    }

    public synchronized CacheItem get(HTTPRequest request) {
        CacheValue cacheValue = cache.get(request.getRequestURI());
        if (cacheValue != null) {
            for (Map.Entry<Vary, CacheItem> entry : cacheValue) {
                if (entry.getKey().matches(request)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public synchronized void invalidate(URI uri) {
        if (cache.containsKey(uri)) {
            cache.remove(uri);
        }
    }

    public void invalidate(URI requestURI, CacheItem item) {
        if (cache.containsKey(requestURI) && item != null) {
            CacheValue value = cache.get(requestURI);
            invalidate(value, item);
            if (value.isEmpty()) {
                cache.remove(requestURI);
            }
        }
    }

    protected void invalidate(CacheValue value, CacheItem item) {
        if (item == null) {
            for (Map.Entry<Vary, CacheItem> entry : value) {
                Payload payload = entry.getValue().getResponse().getPayload();
                if (payload instanceof CleanablePayload) {
                    ((CleanablePayload) payload).clean();
                }
            }
        }
        else {
            Vary found = null;
            for (Map.Entry<Vary, CacheItem> entry : value) {
                if (entry.getValue().equals(item)) {
                    found = entry.getKey();
                }
            }

            if (found != null) {
                value.remove(found);
                Payload payload = item.getResponse().getPayload();
                if (payload instanceof CleanablePayload) {
                    ((CleanablePayload) payload).clean();
                }
            }
        }
    }

    public synchronized void clear() {
        Set<URI> uris = new HashSet<URI>(cache.keySet());
        for (URI uri : uris) {
            cache.remove(uri);
        }
    }

    public synchronized int size() {
        return cache.size();
    }

    protected class InvalidateOnRemoveLRUHashMap extends LinkedHashMap<URI, CacheValue> {
        private final int capacity;
        public InvalidateOnRemoveLRUHashMap(final int capacity) {
            super(capacity);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<URI, CacheValue> eldest) {
            return size() == capacity;
        }

        @Override
        public CacheValue remove(final Object key) {
            final CacheValue value = super.remove(key);
            invalidate(value, null);
            return value;
        }
    }
}
