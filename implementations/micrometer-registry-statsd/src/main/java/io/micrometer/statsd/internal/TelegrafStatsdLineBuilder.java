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
package io.micrometer.statsd.internal;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.api.instrument.Statistic;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.api.lang.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class TelegrafStatsdLineBuilder extends FlavorStatsdLineBuilder {
    private final Object conventionTagsLock = new Object();
    @SuppressWarnings({"NullableProblems", "unused"})
    private volatile NamingConvention namingConvention;
    @SuppressWarnings("NullableProblems")
    private volatile String name;
    @Nullable
    private volatile String conventionTags;
    @SuppressWarnings("NullableProblems")
    private volatile String tagsNoStat;
    private final ConcurrentMap<Statistic, String> tags = new ConcurrentHashMap<>();

    public TelegrafStatsdLineBuilder(Meter.Id id, MeterRegistry.Config config) {
        super(id, config);
    }

    @Override
    String line(String amount, @Nullable Statistic stat, String type) {
        updateIfNamingConventionChanged();
        return name + tagsByStatistic(stat) + ":" + amount + "|" + type;
    }

    private void updateIfNamingConventionChanged() {
        NamingConvention next = config.namingConvention();
        if (this.namingConvention != next) {
            synchronized (conventionTagsLock) {
                if (this.namingConvention == next) {
                    return;
                }
                this.tags.clear();
                this.conventionTags = id.getTagsAsIterable().iterator().hasNext() ?
                        id.getConventionTags(next).stream()
                                .map(t -> telegrafEscape(t.getKey()) + "=" + telegrafEscape(t.getValue()))
                                .collect(Collectors.joining(","))
                        : null;
            }
            this.name = telegrafEscape(next.name(id.getName(), id.getType(), id.getBaseUnit()));
            this.tagsNoStat = tags(null, conventionTags, "=", ",");
            this.namingConvention = next;
        }
    }

    private String tagsByStatistic(@Nullable Statistic stat) {
        if (stat == null) {
            return tagsNoStat;
        }
        String tags = this.tags.get(stat);
        if (tags != null) {
            return tags;
        }
        synchronized (conventionTagsLock) {
            return this.tags.computeIfAbsent(stat, (key) -> tags(stat, conventionTags, "=", ","));
        }
    }

    /**
     * Backslash escape '=' works fine.
     * <p>
     * Trying to escape spaces and commas causes the rest of the name to be dropped by telegraf.
     * Trying to escape colons doesn't work. All of these must be replaced.
     */
    // backslash escape =
    // trying to escape spaces and comma drops everything after that
    private String telegrafEscape(String value) {
        return value.replaceAll("=", "\\=")
                .replaceAll("[\\s,:]", "_");
    }
}
