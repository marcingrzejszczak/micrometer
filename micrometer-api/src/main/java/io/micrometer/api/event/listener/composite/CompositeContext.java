/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micrometer.api.event.listener.composite;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import io.micrometer.api.event.listener.RecordingListener;

/**
 * Context holder in case of using listeners that have different context types.
 * It basically holds a listener -> context mapping so that you can query
 * context of the listener by passing a listener instance.
 *
 * @author Jonatan Ivanov
 * @since 6.0.0
 */
public class CompositeContext {

    private final Map<RecordingListener<?>, Object> contexts = new IdentityHashMap<>();

    CompositeContext(RecordingListener<?>... listeners) {
        this(Arrays.asList(listeners));
    }

    CompositeContext(List<? extends RecordingListener<?>> listeners) {
        // Could be a .stream().collect(toMap(...)) but toMap fails on null values:
        // https://bugs.openjdk.java.net/browse/JDK-8148463
        addContexts(listeners);
    }

    private void addContexts(List<? extends RecordingListener<?>> listeners) {
        for (RecordingListener<?> listener : listeners) {
            if (listener instanceof CompositeRecordingListener) {
                addContexts(((CompositeRecordingListener) listener).getListeners());
            } else {
                this.contexts.put(listener, listener.createContext());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T byListener(RecordingListener<T> listener) {
        return (T) this.contexts.get(listener);
    }

}
