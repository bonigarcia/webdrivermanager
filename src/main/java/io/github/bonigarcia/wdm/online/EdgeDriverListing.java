package io.github.bonigarcia.wdm.online;

public class EdgeDriverListing {

    public EdgeDriverItem[] items;

    public class EdgeDriverItem {
        public boolean isDirectory;
        public String name;
        public long contentLength;
        public String lastModified;
    }

}
