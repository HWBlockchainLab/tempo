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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.tempo.dsl.interfaces;


import org.hyperledger.tempo.dsl.dto.Point;

/**
 * The {@code Reducer} interface for combining two values of the same type into a new value.
 * In contrast to {@link Aggregator} the result type must be the same as the input type.
 * <p>
 * The provided values can be either original values from input {@link KeyValue} pair records or be a previously
 * computed result from {@link PointReducer#apply(Object, Object)}.
 * <p>
 * {@code Reducer} can be used to implement aggregation functions like sum, min, or max.
 *
 * @param <V> value type
 * @see KGroupedStream#reduce(PointReducer)
 * @see KGroupedStream#reduce(PointReducer, Materialized)
 * @see TimeWindowedKStream#reduce(PointReducer)
 * @see TimeWindowedKStream#reduce(PointReducer, Materialized)
 * @see SessionWindowedKStream#reduce(PointReducer)
 * @see SessionWindowedKStream#reduce(PointReducer, Materialized)
 * @see Aggregator
 */
public interface PointReducer {

    /**
     * Aggregate the two given values into a single one.
     *
     * @param value1 the first value for the aggregation
     * @param value2 the second value for the aggregation
     * @return the aggregated value
     */
    Point apply(final Point value1, final Point value2);
}
