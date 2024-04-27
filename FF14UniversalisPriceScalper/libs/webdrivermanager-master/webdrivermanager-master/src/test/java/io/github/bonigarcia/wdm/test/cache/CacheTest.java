/*
 * (C) Copyright 2016 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.cache;

import static io.github.bonigarcia.wdm.config.Architecture.DEFAULT;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.OperatingSystem.LINUX;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.cache.CacheHandler;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;

/**
 * Test for driver cache.
 *
 * @author Boni Garcia
 * @since 1.4.5
 */
class CacheTest {

    static final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @MethodSource("cacheProvider")
    void testCache(DriverManagerType driverManagerType, String driverName,
            String driverVersion, Architecture arch, OperatingSystem os)
            throws Exception {

        WebDriverManager wdm = WebDriverManager.getInstance(driverManagerType)
                .avoidResolutionCache().forceDownload().operatingSystem(os)
                .driverVersion(driverVersion);
        wdm.setup();

        CacheHandler cacheHandler = new CacheHandler(new Config());
        Optional<String> driverFromCache = cacheHandler.getDriverFromCache(
                driverVersion, driverName, driverManagerType, arch, os.name());

        log.debug("Driver from cache: {}", driverFromCache);
        assertThat(driverFromCache.get()).isNotNull();
    }

    static Stream<Arguments> cacheProvider() {
        return Stream.of(
                Arguments.of(CHROME, "chromedriver", "91.0.4472.101", DEFAULT,
                        LINUX),
                Arguments.of(FIREFOX, "geckodriver", "0.29.0", DEFAULT, LINUX));
    }

}
