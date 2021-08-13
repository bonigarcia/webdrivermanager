/*
 * (C) Copyright 2020 Boni Garcia (https://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm.online;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.singletonList;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * Namespace context for S3 buckets.
 *
 * @author andruhon and Boni Garcia
 * @since 4.1.0
 */
public class S3NamespaceContext implements NamespaceContext {

    private static final String S3_BUCKET_LIST_NS = "http://doc.s3.amazonaws.com/2006-03-01";

    private static final String S3_PREFIX = "s3";

    @Override
    public String getNamespaceURI(String prefix) {
        if (S3_PREFIX.equals(prefix)) {
            return S3_BUCKET_LIST_NS;
        }
        throw new IllegalArgumentException("Unsupported prefix");
    }

    @Override
    public String getPrefix(String namespaceURI) {
        if (S3_BUCKET_LIST_NS.equals(namespaceURI)) {
            return S3_PREFIX;
        }
        throw new IllegalArgumentException("Unsupported namespace URI");
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        if (S3_BUCKET_LIST_NS.equals(namespaceURI)) {
            return singletonList(S3_PREFIX).iterator();
        } else {
            return emptyIterator();
        }
    }

}
