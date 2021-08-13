/*
 * (C) Copyright 2018 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.config;

/**
 * Configuration key class.
 *
 * @author Boni Garcia
 * @since 2.2.0
 */
public class ConfigKey<T> {

    String name;
    Class<T> type;
    T value;
    T defaultValue;

    public ConfigKey(Class<T> type) {
        this.type = type;
    }

    public ConfigKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public ConfigKey(String name, Class<T> type, T value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.defaultValue = value;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public void reset() {
        value = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        this.value = (T) value;
    }

}
