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

package org.hyperledger.tempo.dsl.interfaces.report;

import javax.annotation.Nonnull;

/**
 * The column restrictions.
 *
 * @author Gregory Vortman (gregvo) (28/04/2022)
 */
public interface FluxColumnRestrictions {

    /**
     * Is column of record "equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    FluxRestrictions equal(@Nonnull final Object value);

    /**
     * Is column of record "not equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    FluxRestrictions notEqual(@Nonnull final Object value);

    /**
     * Is column of record "less" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    FluxRestrictions less(@Nonnull final Object value);

    /**
     * Is column of record "greater" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    FluxRestrictions greater(@Nonnull final Object value);

    /**
     * Is column of record "less or equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    FluxRestrictions lessOrEqual(@Nonnull final Object value);

    /**
     * Is column of record "greater or equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    FluxRestrictions greaterOrEqual(@Nonnull final Object value);

    /**
     * Is column of record "{@code operator}" than {@code value}?
     *
     * @param value    the value to compare
     * @param operator the restriction operator
     * @return restriction
     */
    @Nonnull
    FluxRestrictions custom(@Nonnull final Object value, @Nonnull final String operator);

    /**
     * Check if an record contains a key or if that key’s value is null.
     *
     * @return restriction
     */
    @Nonnull
    FluxRestrictions exists();

    /**
     * Get back to ReportQuery for post restrictions operations (i.e. groupBy, orederBy, aggragations...).
     *
     * @return reportQuery
     */
    @Nonnull
    ReportQuery then();
}

