/*
 * (C) Copyright 2021 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.docker;

import java.time.Duration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

class DockerChromeVersionsTest {

    @ParameterizedTest
    @ValueSource(strings = { "", "91", "91.0", "latest", "latest-1",
            "latest-2" })
    void test(String browserVersion) {
        WebDriverManager wdm = WebDriverManager.chromedriver()
                .clearResolutionCache().browserInDocker()
                .browserVersion(browserVersion);
        WebDriver driver = wdm.create();

        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        Wait<WebDriver> wait = new WebDriverWait(driver,
                Duration.ofSeconds(30));
        wait.until(d -> d.getTitle().contains("Selenium WebDriver"));

        wdm.quit();
    }

}
