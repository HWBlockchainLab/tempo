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


/**
 * The {@code ForeachAction} interface for performing an action on a key-value
 * pair.
 * This is a stateless record-by-record operation, i.e, {@link #apply(Object, Object)} is invoked individually for each
 * record of a stream.
 * If stateful processing is required, consider using KStream#process(...)
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface ForeachAction<K, V> {

    /**
     * Perform an action for each record of a stream.
     *
     * @param key   the key of the record
     * @param value the value of the record
     */
    void apply(final K key, final V value);
}
