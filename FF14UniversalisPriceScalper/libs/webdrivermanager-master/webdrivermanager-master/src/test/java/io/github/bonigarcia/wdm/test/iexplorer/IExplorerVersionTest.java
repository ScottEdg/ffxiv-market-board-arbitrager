/*
 * (C) Copyright 2015 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.iexplorer;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with Internet Explorer browser.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
class IExplorerVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriverManager wdm = WebDriverManager.iedriver().win().arch32();

    @Test
    void testIExplorerLatest() {
        wdm.setup();
        assertIEDriver();
    }

    @Test
    void testIExplorerVersion() {
        wdm.driverVersion("2.53.1").setup();
        assertIEDriver();
    }

    private void assertIEDriver() {
        File driver = new File(wdm.getDownloadedDriverPath());
        log.debug("Path for IEDriverServer {}", driver);
        assertThat(driver).exists();
    }

}
