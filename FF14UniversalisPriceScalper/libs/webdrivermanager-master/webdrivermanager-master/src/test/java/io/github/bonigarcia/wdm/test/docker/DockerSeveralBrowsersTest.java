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

import static java.lang.invoke.MethodHandles.lookup;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

class DockerSeveralBrowsersTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriver driver1, driver2;

    WebDriverManager wdm = WebDriverManager.chromedriver().browserInDocker()
            .dockerLang("ES").dockerTimezone("Europe/Madrid").enableVnc();

    @BeforeEach
    void setupTest() {
        driver1 = wdm.create();
        driver2 = wdm.create();
    }

    @AfterEach
    void teardown() {
        wdm.quit(driver1);
        wdm.quit(driver2);
    }

    @Test
    void test() throws Exception {
        exercise(driver1, "https://bonigarcia.dev/selenium-webdriver-java/",
                "Selenium WebDriver");
        exercise(driver2,
                "https://bonigarcia.dev/selenium-webdriver-java/slow-calculator.html",
                "Selenium WebDriver");

        // Pause for manual inspection
        Thread.sleep(Duration.ofSeconds(10).toMillis());
    }

    void exercise(WebDriver driver, String sutUrl, String expectedTitleContains)
            throws Exception {
        driver.get(sutUrl);
        Wait<WebDriver> wait = new WebDriverWait(driver,
                Duration.ofSeconds(30));
        wait.until(d -> d.getTitle().contains(expectedTitleContains));

        URL dockerSessionUrl = wdm.getDockerNoVncUrl(driver);
        log.debug("The noVNC URL is {}", dockerSessionUrl);
        HttpURLConnection huc = (HttpURLConnection) dockerSessionUrl
                .openConnection();
        assertThat(huc.getResponseCode()).isEqualTo(HTTP_OK);
    }

}
