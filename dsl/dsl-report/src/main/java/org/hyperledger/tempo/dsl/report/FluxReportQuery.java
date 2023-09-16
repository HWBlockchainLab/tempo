// Copyright 2023 Huawei Cloud Computing Technology Co., Ltd.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and;
// limitations under the License.

package org.hyperledger.tempo.dsl.report;


import org.hyperledger.tempo.dsl.interfaces.report.FluxRestrictions;
import org.hyperledger.tempo.dsl.interfaces.report.ReportQuery;
import org.hyperledger.tempo.dsl.interfaces.streams.IngestStreamCreationContext;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class FluxReportQuery implements ReportQuery {

    private final static String RANGE_FLUX_GENERATED_CLAUSE = "|> range()";
    private final static String RANGE_PARAMETERIZED_CLAUSE = "|> range(start: $start, stop: $stop)";
    private final static String PERCENTILE_FLUX_GENERATED_CLAUSE = "|> percentile(percentile:-Infinity)";
    private final static String PERCENTILE_PARAMETERIZED_CLAUSE = "|> percentile(percentile: $percentile)";

    private final String name;
    private final String[] measurements;
    private final ReportQueryCreationContext initCtx;
    private Flux flux;
    private final List<Restrictions> restrictionsArr = new ArrayList<>();
    private boolean logicalOrRestrictions = false;


    private FluxReportQuery(String name, @Nonnull final Flux flux, ReportQueryCreationContext initCtx) {
        this.initCtx = initCtx;
        this.measurements = null;
        this.flux = flux;
        this.name = name;
    }

    public FluxReportQuery(final String name, final String[] measurements, final IngestStreamCreationContext initCtx) {
        this.name = name;
        this.measurements = measurements;
        this.initCtx = (ReportQueryCreationContext) initCtx;
        this.flux = Flux.from("dummyDbName").range();

        Restrictions[] measurementRestrictions = new Restrictions[measurements.length];

        for (int i = 0; i < measurementRestrictions.length; i++) {
            measurementRestrictions[i] = Restrictions.measurement().equal(this.measurements[i]);
        }
        this.addRestrictions(Restrictions.or(measurementRestrictions));

        if (null != this.initCtx) {
            this.initCtx.addReportQuery(this);
        }
    }

    private Restrictions[] getRestrictions() {
        return this.restrictionsArr.toArray(new Restrictions[restrictionsArr.size()]);
    }

    private void clearRestrictions() {
        this.restrictionsArr.clear();
    }

    void addRestrictions(final Restrictions restrictions) {
        this.restrictionsArr.add(restrictions);
    }

    @Override
    public FluxRestrictions andFilters() {
        return new FluxRestrictionsImpl(this);
    }

    @Override
    public FluxRestrictions orFilters() {
        this.logicalOrRestrictions = true;
        return new FluxRestrictionsImpl(this);
    }

    ReportQuery applyFilter() {
        if (this.restrictionsArr.size() > 0) {
            Restrictions r = this.logicalOrRestrictions ? Restrictions.or(this.getRestrictions()) : Restrictions.and(this.getRestrictions());
            this.flux = this.flux.filter(r);
            this.clearRestrictions();
        }
        this.logicalOrRestrictions = false;
        return this;
    }

    @Override
    public ReportQuery window(final long every, final ChronoUnit unit) {
        this.flux = this.flux.window(every, unit);
        return this;
    }

    @Override
    public ReportQuery groupBy(final String... groupBy) {
        this.flux = this.flux.groupBy(groupBy);
        return this;
    }

    @Override
    public ReportQuery orderByTime() {
        this.flux = this.flux.sort(Collections.singleton("_time"));
        return this;
    }

    public ReportQuery sort() {
        this.flux = this.flux.sort();
        return this;
    }

    @Override
    public ReportQuery sort(final String... columns) {
        this.flux = this.flux.sort(columns);
        return this;
    }

    @Override
    public ReportQuery duplicate(final String column, final String as) {
        this.flux = this.flux.duplicate(column, as);
        return this;
    }

    @Override
    public ReportQuery keep(final String... columns) {
        this.flux = this.flux.keep(columns);
        return this;
    }

    @Override
    public ReportQuery drop(final String... columns) {
        this.flux = this.flux.drop(columns);
        return this;
    }

    @Override
    public ReportQuery renameValueColumnNameTo(final String valueColumnAlias) {
        Map<String, String> map = new HashMap<String, String>() {{
            put("_value", valueColumnAlias);
        }};
        this.flux = this.flux.rename(map);
        return this;
    }

    @Override
    public ReportQuery mean() {
        this.flux = this.flux.mean();
        return this;
    }

    @Override
    public ReportQuery count() {
        this.flux = this.flux.count();
        return this;
    }

    @Override
    public ReportQuery count(final String countColumnAlias) {
        Map<String, String> map = new HashMap<String, String>() {{
            put("_value", countColumnAlias);
        }};
        this.flux = this.flux.count().rename(map);
        return this;
    }

    @Override
    public ReportQuery count(final String columnToCount, final String countColumnAlias) {
        return this
                .andFilters()
                .field(columnToCount)
                .then()
                .duplicate("_value", columnToCount)
                .groupBy(columnToCount)
                .count()
                .renameValueColumnNameTo(countColumnAlias)
                .keep(columnToCount, countColumnAlias);
    }

    @Override
    public ReportQuery fieldsAsColumns() {
        this.flux = this.flux.pivot(Collections.singleton("_time"), Collections.singleton("_field"), "_value");
        return this;
    }

    public ReportQuery sum() {
        this.flux = this.flux.sum();
        return this;
    }

    @Override
    public ReportQuery percentile() {
        this.flux = this.flux.quantile();
        return this;
    }

    @Override
    public ReportQuery percentile(final Float percentile) {
        this.flux = this.flux.quantile(percentile);
        return this;
    }

    @Override
    public ReportQuery percentile(final String percentile) {
        // Use NEGATIVE_INFINITY just for generating placeholder for replacement by $percentile
        // once flux query is fully built and replacement can be done.
        this.flux = this.flux.quantile(Float.NEGATIVE_INFINITY);
        return this;
    }

    @Override
    public ReportQuery last() {
        this.flux = this.flux.last();
        return this;
    }

    @Override
    public ReportQuery min() {
        this.flux = this.flux.min();
        return this;
    }

    @Override
    public ReportQuery max() {
        this.flux = this.flux.max();
        return this;
    }

    @Override
    public ReportQuery first() {
        this.flux = this.flux.first();
        return this;
    }

    @Override
    public ReportQuery limit(final int numberOfResults) {
        this.flux = this.flux.limit(numberOfResults);
        return this;
    }

    @Override
    @Nonnull
    public ReportQuery join(final String name, final ReportQuery other, final String tag, final String method) {
        FluxReportQuery otherImpl = (FluxReportQuery) other;
        Flux joinedFlux = Flux.join(this.getName(), this.flux, otherImpl.getName(), otherImpl.flux, tag, method);

        return new FluxReportQuery(name, joinedFlux, initCtx);
    }

    private String removeFromClause() {
        String fluxStr = this.fullQuery();
        return fluxStr.substring(fluxStr.indexOf("\n") + 1);
    }

    private String replaceParameterizedPatterns() {
        String fluxStr = this.removeFromClause();
        fluxStr = this.replaceRangeClause(fluxStr);
        fluxStr = this.replacePercentileClause(fluxStr);
        return fluxStr;
    }

    private String replacePercentileClause(final String fluxStr) {
        return fluxStr.replace(PERCENTILE_FLUX_GENERATED_CLAUSE, PERCENTILE_PARAMETERIZED_CLAUSE);
    }

    private String replaceRangeClause(final String fluxStr) {
        return fluxStr.replace(RANGE_FLUX_GENERATED_CLAUSE, RANGE_PARAMETERIZED_CLAUSE);
    }

    @Override
    public String fullQuery() {
        this.applyFilter();
        return this.flux.toString();
    }

    @Override
    public String toString() {
        return replaceParameterizedPatterns();
    }

    public String getName() {
        return name;
    }
}
