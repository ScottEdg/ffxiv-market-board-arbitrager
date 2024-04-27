/*
 * (C) Copyright 2019 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.versions;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test latest version of edgedriver.
 *
 * @author Boni Garcia
 * @since 3.6.0
 */
@Disabled
class LatestAndBetaTest {

    final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @ValueSource(classes = { ChromeDriver.class, EdgeDriver.class })
    void testLatestAndBetaedgedriver(Class<? extends WebDriver> driverClass) {
        WebDriverManager wdm = WebDriverManager.getInstance(driverClass)
                .avoidResolutionCache().avoidBrowserDetection().win();
        wdm.setup();
        String edgedriverStable = wdm.getDownloadedDriverVersion();
        log.debug("edgedriver LATEST version: {}", edgedriverStable);

        wdm.useBetaVersions().setup();
        String edgedriverBeta = wdm.getDownloadedDriverVersion();
        log.debug("edgedriver BETA version: {}", edgedriverBeta);

        assertThat(edgedriverStable).isNotEqualTo(edgedriverBeta);
    }

}
