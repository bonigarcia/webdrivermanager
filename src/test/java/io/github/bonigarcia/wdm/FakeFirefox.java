package io.github.bonigarcia.wdm;

public class FakeFirefox {
    public static final String VERSION = "Mozilla Firefox 63.0.3";
    public static final String HEADLESS_NOTICE = "*** You are running in headless mode.";

    public static void main(String... args) {
        System.err.println(HEADLESS_NOTICE);
        System.out.println(VERSION);
    }

    public static String[] invocation() {
        return new String[]{
                "java",
                "-cp",
                "target/test-classes",
                FakeFirefox.class.getCanonicalName()
        };
    }
}
