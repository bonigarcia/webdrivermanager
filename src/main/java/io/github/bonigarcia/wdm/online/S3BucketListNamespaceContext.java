package io.github.bonigarcia.wdm.online;

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class S3BucketListNamespaceContext implements NamespaceContext {

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
            return new Iterator() {
                boolean more = true;

                public boolean hasNext() {
                    return this.more;
                }

                public Object next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    } else {
                        this.more = false;
                        return S3_PREFIX;
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return Collections.EMPTY_LIST.iterator();
        }
    }

}
