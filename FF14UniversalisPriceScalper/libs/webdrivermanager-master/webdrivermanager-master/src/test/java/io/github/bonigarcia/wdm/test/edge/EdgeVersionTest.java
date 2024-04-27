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
package io.github.bonigarcia.wdm.test.edge;

import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.edge.EdgeDriver;

import io.github.bonigarcia.wdm.test.base.VersionTestParent;

/**
 * Test asserting Edge driver versions.
 *
 * @author Boni Garcia
 * @since 1.3.0
 */
class EdgeVersionTest extends VersionTestParent {

    @BeforeEach
    void setup() {
        driverClass = EdgeDriver.class;
        os = WIN;
        specificVersions = new String[] { "91.0.864.41", "92.0.902.55" };
    }

}
