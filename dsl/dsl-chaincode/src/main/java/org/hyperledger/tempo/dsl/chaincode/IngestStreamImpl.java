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

package org.hyperledger.tempo.dsl.chaincode;

import org.hyperledger.tempo.dsl.dto.Point;
import org.hyperledger.tempo.dsl.interfaces.*;
import org.hyperledger.tempo.dsl.interfaces.streams.IngestStream;
import org.hyperledger.tempo.dsl.interfaces.streams.IngestStreamCreationContext;
import org.hyperledger.tempo.dsl.interfaces.streams.PointStream;

@SuppressWarnings("unchecked")
public class IngestStreamImpl<K, V> implements IngestStream<K, V> {

    final IngestStreamCreationContext ctx;
    
    public IngestStreamImpl(final IngestStreamCreationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public PointStream toPointStream() {
        return new PointStreamImpl(ctx);
    }

    @Override
    public PointStream toPointStream(ValueMapper<? super V, ? extends Point> mapper) {
        return new PointStreamImpl(ctx);
    }

    @Override
    public IngestStream<K, V> filter(Predicate<? super K, ? super V> predicate) {
        return this;
    }


    @Override
    public IngestStream<K, V> filterNot(Predicate<? super K, ? super V> predicate) {
        return this;
    }


    @Override
    public <KR> IngestStream<KR, V> selectKey(KeyValueMapper<? super K, ? super V, ? extends KR> mapper) {
        return (IngestStream<KR, V>)this;
    }


    @Override
    public <KR, VR> IngestStream<KR, VR> map(final KeyValueMapper<? super K, ? super V, ? extends KeyValue<? extends KR, ? extends VR>> mapper) {
        return (IngestStream<KR, VR>)this;
    }

    @Override
    public <VR> IngestStream<K, VR> mapValues(ValueMapper<? super V, ? extends VR> mapper) {
        return (IngestStream<K, VR>) this;
    }

    @Override
    public <VR> IngestStream<K, VR> mapValues(KeyValueMapper<? super K, ? super V, ? extends VR> mapper) {
        return (IngestStream<K, VR>) this;
    }

    @Override
    public <KR, VR> IngestStream<KR, VR> flatMap(KeyValueMapper<? super K, ? super V, ? extends Iterable<? extends KeyValue<? extends KR, ? extends VR>>> mapper) {
        return (IngestStream<KR, VR>) this;
    }

    @Override
    public <VR> IngestStream<K, VR> flatMapValues(ValueMapper<? super V, ? extends Iterable<? extends VR>> mapper) {
        return (IngestStream<K, VR>) this;
    }

    @Override
    public <VR> IngestStream<K, VR> flatMapValues(ValueMapperWithKey<? super K, ? super V, ? extends Iterable<? extends VR>> mapper) {
        return (IngestStream<K, VR>) this;
    }


    @Override
    public IngestStream<K, V> peek(ForeachAction<? super K, ? super V> action) {
        return this;
    }


}
