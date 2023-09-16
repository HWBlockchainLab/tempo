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

import org.hyperledger.tempo.dsl.interfaces.report.FluxColumnRestrictions;
import org.hyperledger.tempo.dsl.interfaces.report.FluxRestrictions;
import org.hyperledger.tempo.dsl.interfaces.report.ReportQuery;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

import javax.annotation.Nonnull;

public class FluxRestrictionsImpl implements FluxRestrictions {

    private final FluxReportQuery reportQuery;

    FluxRestrictionsImpl(final FluxReportQuery reportQuery) {
        this.reportQuery = reportQuery;
    }

    FluxRestrictionsImpl(final Restrictions restrictions, final FluxReportQuery reportQuery) {
        reportQuery.addRestrictions(restrictions);
        this.reportQuery = reportQuery;
    }

    @Override
    public ReportQuery then() {
        return this.reportQuery.applyFilter();
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions start() {
        return new FluxColumnRestrictionsImpl(Restrictions.start(), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions stop() {
        return new FluxColumnRestrictionsImpl(Restrictions.stop(), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions time() {
        return new FluxColumnRestrictionsImpl(Restrictions.time(), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions value() {
        return new FluxColumnRestrictionsImpl(Restrictions.value(), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions field(@Nonnull final String fieldName) {
        return new FluxColumnRestrictionsImpl(Restrictions.field(), this.reportQuery).equal(fieldName).value();
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions tag(@Nonnull final String tagName) {
        return new FluxColumnRestrictionsImpl(Restrictions.tag(tagName), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxColumnRestrictions column(@Nonnull final String columnName) {
        return new FluxColumnRestrictionsImpl(Restrictions.column(columnName), this.reportQuery);
    }
}
