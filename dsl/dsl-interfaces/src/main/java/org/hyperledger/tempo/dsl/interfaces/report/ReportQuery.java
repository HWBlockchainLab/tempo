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

import java.time.temporal.ChronoUnit;


/**
 * ReportQuery - Data Scripting Language interface.
 *
 * <h3>The functions:</h3>
 * <ul>
 * <li>{@link FluxRestrictions}</li>
 * </ul>
 *
 * @author Gregory Vortman (gregvo) (22/05/2022)
 */
public interface ReportQuery {

     /**
     * Returns FluxRestrictions object for filter definition with AND operator
     *
     * @return {@link FluxRestrictions}
     */
    FluxRestrictions andFilters();

    /**
     * Returns FluxRestrictions object for filter definition with AND operator.
     *
     * @return {@link FluxRestrictions}
     */
    FluxRestrictions orFilters();

    /**
     * Groups the results by a given time range.
     *
     * @param every – duration of time between windows
     * @param unit – a ChronoUnit determining how to interpret the every
     * @return ReportQuery
     */
    ReportQuery window(final long every, final ChronoUnit unit);

    /**
     * Groups results by a user-specified tag.
     *
     * @param groupBy – Group by these specific tag name
     * @return ReportQuery
     */
    ReportQuery groupBy(final String... groupBy);

    /**
     * Sorts the results by the specified columns. Default sort is ascending.
     * @return ReportQuery
     */
    ReportQuery orderByTime();

    /**
     * Sorts the results by the time column. Default sort is ascending.
     * @return ReportQuery
     */
    ReportQuery sort();

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     * @param columns – columns used to sort
     * @return ReportQuery
     */
    ReportQuery sort(final String... columns);

    /**
     * Renames specified "_value" to a specified column name.
     * @param valueColumnAlias – new name for the "_value" column.
     *
     * @return ReportQuery
     */
    ReportQuery renameValueColumnNameTo(final String valueColumnAlias);

    /**
     * Returns the mean of the values within the results.
     *
     * @return ReportQuery
     */
    ReportQuery mean();

    /**
     * Counts the number of results.
     *
     * @return ReportQuery
     */
    ReportQuery count();

    /**
     * Counts the number of results and rename the result column with a given column name
     * @param countColumnAlias - count result column name to rename to
     *
     * @return ReportQuery
     */
    ReportQuery count(final String countColumnAlias);

    /**
     * Counts the number of results per given column name and rename the count result column with a given column name
     * @param columnToCount - the column to apply count function for
     * @param countColumnAlias - count result column name to rename to
     *
     * @return ReportQuery
     */
    ReportQuery count(final String columnToCount, final String countColumnAlias);

    /**
     * A special application of pivot() that pivots input data on _field and _time columns
     * to align fields within each input table that have the same timestamp. Usually required
     * when filters applied on more than one field (i.e. (field_A_value > 0 and field_B_value > 0)).
     *
     * @return ReportQuery
     */
    ReportQuery fieldsAsColumns();

    /**
     * Sum of the results.
     *
     * @return ReportQuery
     */
    ReportQuery sum();

    /**
     * Percentile is both an aggregate operation and a selector operation depending on selected options.
     * @return ReportQuery
     */
    ReportQuery percentile();

    /**
     * Percentile is both an aggregate operation and a selector operation depending on selected options.
     * @param percentile - value between 0 and 1 indicating the desired percentile
     * @return ReportQuery
     */
    ReportQuery percentile(final Float percentile);

    /**
     * Percentile is both an aggregate operation and a selector operation depending on selected options.
     * @param percentile - parameterized value represented by String pattern $percentile
     * @return ReportQuery
     */
    ReportQuery percentile(final String percentile);

    /**
     * Returns the last result of the query.
     *
     * @return ReportQuery
     */
    ReportQuery last();

    /**
     * Returns the min value within the results.
     *
     * @return ReportQuery
     */
    ReportQuery min();

    /**
     * Restricts the number of rows returned in the results.
     *
     * @return ReportQuery
     */
    ReportQuery max();

    /**
     * Returns the first result of the query.
     *
     * @return ReportQuery
     */
    ReportQuery first();

    /**
     * Returns the mean of the values within the results.
     *
     * @param numberOfResults – The number of results
     * @return ReportQuery
     */
    ReportQuery limit(final int numberOfResults);

    /**
     * Duplicates a specified column in a table and assign a new name to the duplicate column
     *
     * @param column - the column to duplicate
     * @param as - the name that should be assigned to the duplicate column
     * @return ReportQuery
     */
    ReportQuery duplicate(final String column, final String as);

    /**
     * It will return a table containing only columns that are specified.
     * @param columns – The list of columns that should be included in the resulting table.
     * @return ReportQuery
     */
    ReportQuery keep(final String... columns);

    /**
     * Drop will exclude specified columns from a table.
     * @param columns – The list of columns which should be excluded from the resulting table.
     * @return ReportQuery
     */
    ReportQuery drop(final String... columns);

    /**
     * Join two time series together on time and the list of tags.
     *
     * @param name - joined entity name
     * @param other  – other table Flux script
     * @param tag    – tag key to join
     * @param method – the type of join to be performed
     * @return ReportQuery
     */
    ReportQuery join(final String name, final ReportQuery other, final String tag, final String method);

    /**
     * Returns generated Flux script associated with the DSL in its full form (Runnable as it is on InfluxDB)
     * @return generated Flux script
     */
    String fullQuery();
}
