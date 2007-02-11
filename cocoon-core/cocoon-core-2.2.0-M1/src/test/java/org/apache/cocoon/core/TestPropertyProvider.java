/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core;

import java.util.Properties;

import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;

public class TestPropertyProvider
    implements PropertyProvider {
    
    private final String configuration;

    public TestPropertyProvider(String configuration) {
        this.configuration = configuration;
    }

    /**
     * @see org.apache.cocoon.configuration.PropertyProvider#getProperties(org.apache.cocoon.configuration.Settings, java.lang.String, java.lang.String)
     */
    public Properties getProperties(Settings settings, String runningMode, String path) {
        final Properties p = new Properties();
        p.setProperty(Settings.KEY_CONFIGURATION, this.configuration);
        p.setProperty(Settings.KEY_WORK_DIRECTORY, "work");
        return p;
    }
}