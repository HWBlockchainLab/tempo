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
import com.influxdb.query.dsl.functions.restriction.ColumnRestriction;

import javax.annotation.Nonnull;

public class FluxColumnRestrictionsImpl implements FluxColumnRestrictions {
    private final ColumnRestriction nativeColumnRestriction;
    private final FluxReportQuery reportQuery;

    protected FluxColumnRestrictionsImpl(final ColumnRestriction columnRestriction, final FluxReportQuery reportQuery) {
        this.nativeColumnRestriction = columnRestriction;
        this.reportQuery = reportQuery;
    }

    @Nonnull
    @Override
    public FluxRestrictions equal(@Nonnull Object value) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.equal(value), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions notEqual(@Nonnull Object value) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.notEqual(value), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions less(@Nonnull Object value) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.less(value), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions greater(@Nonnull Object value) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.greater(value),this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions lessOrEqual(@Nonnull Object value) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.lessOrEqual(value), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions greaterOrEqual(@Nonnull Object value) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.greaterOrEqual(value), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions custom(@Nonnull Object value, @Nonnull String operator) {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.custom(value, operator), this.reportQuery);
    }

    @Nonnull
    @Override
    public FluxRestrictions exists() {
        return new FluxRestrictionsImpl(this.nativeColumnRestriction.exists(), this.reportQuery);
    }

    @Nonnull
    @Override
    public ReportQuery then() {
        return this.reportQuery.applyFilter();
    }
}
