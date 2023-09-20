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
 * The {@code ValueMapperWithKey} interface for mapping a value to a new value of arbitrary type.
 * This is a stateless record-by-record operation, i.e, {@link #apply(Object, Object)} is invoked individually for each
 * record of a stream ValueTransformer for stateful value transformation).
 * If {@code ValueMapperWithKey} is applied to a KeyValue key-value pair record the
 * record's key is preserved.
 * Note that the key is read-only and should not be modified, as this can lead to corrupt partitioning.
 * If a record's key and value should be modified {@link KeyValueMapper} can be used.
 *
 * @param <K>  key type
 * @param <V>  value type
 * @param <VR> mapped value type
 *
 */

public interface ValueMapperWithKey<K, V, VR> {

    /**
     * Map the given [key and ]value to a new value.
     *
     * @param readOnlyKey the read-only key
     * @param value       the value to be mapped
     * @return the new value
     */
    VR apply(final K readOnlyKey, final V value);
}
