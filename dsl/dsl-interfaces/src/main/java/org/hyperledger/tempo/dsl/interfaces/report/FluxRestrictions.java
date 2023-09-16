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
 * @author Gregory Vortrman (gregvo) (28/04/2022)
 */
public interface FluxRestrictions {

    ReportQuery then();


    /**
     * Create Record field restriction.
     *
     * @return restriction
     */
    @Nonnull
    FluxColumnRestrictions field(@Nonnull final String fieldName);

    /**
     * Create Record start restriction.
     *
     * @return restriction
     */
    @Nonnull
    public FluxColumnRestrictions start();

    /**
     * Create Record stop restriction.
     *
     * @return restriction
     */
    @Nonnull
    FluxColumnRestrictions stop();

    /**
     * Create Record time restriction.
     *
     * @return restriction
     */
    @Nonnull
    FluxColumnRestrictions time();

    /**
     * Create Record value restriction.
     *
     * @return restriction
     */
    @Nonnull
    FluxColumnRestrictions value();

    /**
     * Create Record tag restriction.
     *
     * @param tagName tag name
     * @return restriction
     */
    @Nonnull
    FluxColumnRestrictions tag(@Nonnull final String tagName);

    /**
     * Create Record column restriction.
     *
     * @param columnName column name
     * @return restriction
     */
    @Nonnull
    FluxColumnRestrictions column(@Nonnull final String columnName);
}
