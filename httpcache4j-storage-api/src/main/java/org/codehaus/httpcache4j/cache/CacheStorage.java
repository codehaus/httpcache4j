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

import java.net.URI;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
//TODO: Document.
public interface CacheStorage {

    void put(URI requestURI, Vary vary, CacheItem cacheItem);

    CacheItem get(final HTTPRequest request);

    void invalidate(URI uri);

    void clear();

    int size();

    //TODO: maybe not required any more...
    void invalidate(URI requestURI, CacheItem item);
}