/**
 * Copyright 2017 VMware, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument;

/**
 * Key/value pair representing a dimension of a meter used to classify and drill into measurements.
 *
 * @author Jon Schneider
 */
public interface Tag extends Comparable<Tag> {
    String getKey();

    String getValue();

    default Cardinality getCardinality() {
        return Cardinality.LOW;
    }

    static Tag of(String key, String value) {
        return Tag.of(key, value, Cardinality.LOW); // TODO: or HIGH?
    }

    static Tag of(String key, String value, Cardinality cardinality) {
        return new ImmutableTag(key, value, cardinality);
    }

    @Override
    default int compareTo(Tag o) {
        return getKey().compareTo(o.getKey());
    }
}
