package io.github.bonigarcia.wdm;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ShellTest {
    @Test
    public void runAndWait_headlessFirefox_doesNotIncludeHeadlessNotice() {
        String version = Shell.runAndWait(FakeFirefox.invocation());

        assertThat(version, not(containsString(FakeFirefox.HEADLESS_NOTICE)));
        assertThat(version, equalTo(FakeFirefox.VERSION));
    }

    @Test
    public void getVersionFromPosixOutput_firefoxOutput_returnsMajorVersion() {
        String version = Shell.getVersionFromPosixOutput(
                "Mozilla Firefox 63.0.3",
                "Mozilla Firefox");

        assertThat(version, equalTo("63"));
    }

    @Test
    public void getVersionFromPosixOutput_chromeOutput_returnsMajorVersion() {
        String version = Shell.getVersionFromPosixOutput(
                "Google Chrome 70.0.3538.110",
                "Google Chrome");

        assertThat(version, equalTo("70"));
    }
}