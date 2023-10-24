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

package org.hyperledger.tempo.dsl.interfaces;

import org.hyperledger.tempo.dsl.dto.Point;

/**
 * The  interface for aggregating values of the given key.
 * This is a generalization of PointReducer and allows to have different types for input value and aggregation
 * result.
 *  is used in combination with PointInitializer that provides an initial aggregation value.
 * <p>
 * can be used to implement aggregation functions like count.

 */
public interface PointAggregator {

    /**
     * Compute a new aggregate from the key and value of a record and the current aggregate of the same key.
     *
     * @param value     the value of the record
     * @param aggregate the current aggregate value
     * @return the new aggregate value
     */
    Point apply(final Point value, final Point aggregate);

}
