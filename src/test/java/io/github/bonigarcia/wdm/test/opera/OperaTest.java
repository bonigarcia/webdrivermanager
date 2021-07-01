/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.opera;

import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.opera.OperaDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.test.base.BrowserTestParent;

/**
 * Test with Opera browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class OperaTest extends BrowserTestParent {

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.operadriver().clearResolutionCache().setup();
    }

    @BeforeEach
    public void setupTest() {
        Path browserPath = getBrowserPath();
        assumeThat(browserPath).exists();

        driver = new OperaDriver();
    }

    private Path getBrowserPath() {
        Path path;
        if (IS_OS_WINDOWS) {
            path = Paths.get(System.getenv("LOCALAPPDATA"),
                    "/Programs/Opera/launcher.exe");
        } else if (IS_OS_MAC) {
            path = Paths.get("/Applications/Opera.app/Contents/MacOS/Opera");
        } else {
            path = Paths.get("/usr/bin/opera");
        }
        return path;
    }

}
