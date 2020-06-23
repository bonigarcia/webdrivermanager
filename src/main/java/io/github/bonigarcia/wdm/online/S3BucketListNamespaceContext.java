package io.github.bonigarcia.wdm.online;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class S3BucketListNamespaceContext implements NamespaceContext {

    private static final String S3_BUCKET_LIST_NS = "http://doc.s3.amazonaws.com/2006-03-01";

    @Override
    public String getNamespaceURI(String prefix) {
        return S3_BUCKET_LIST_NS;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

}
