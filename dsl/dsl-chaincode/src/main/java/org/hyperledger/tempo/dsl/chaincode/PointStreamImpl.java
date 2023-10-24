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
import org.hyperledger.tempo.dsl.interfaces.PointForeachAction;
import org.hyperledger.tempo.dsl.interfaces.PointMapper;
import org.hyperledger.tempo.dsl.interfaces.PointPredicate;
import org.hyperledger.tempo.dsl.interfaces.streams.GroupedPointStream;
import org.hyperledger.tempo.dsl.interfaces.streams.IngestStreamCreationContext;
import org.hyperledger.tempo.dsl.interfaces.streams.PointStream;
import org.hyperledger.tempo.dsl.interfaces.streams.TrustedStreamReference;

public class PointStreamImpl implements PointStream {

    private final TrustedStreamCreationContext ctx;

    PointStreamImpl() {
        this.ctx = null;
    }

    PointStreamImpl(final IngestStreamCreationContext ctx) {
        this.ctx = (TrustedStreamCreationContext) ctx;
    }

    @Override
    public TrustedStreamReference toTrustedStream(String streamName) {
        return new DummyTrustedStreamReferenceImpl(streamName, ctx);
    }

    @Override
    public PointStream filter(PointPredicate predicate) {
        return this;
    }

    @Override
    public PointStream filterNot(PointPredicate predicate) {
        return this;
    }

    @Override
    public PointStream map(PointMapper<Point, Point> mapper) {
        return this;
    }

    @Override
    public PointStream flatMap(PointMapper<Point,? extends java.lang.Iterable<org.hyperledger.tempo.dsl.dto.Point>> mapper)  {
        return this;
    }

    @Override
    public PointStream peek(PointForeachAction action) {
        return this;
    }

    @Override
    public GroupedPointStream groupBy(PointMapper<? super Point, String> mapper) {
        return new GroupedPointStreamImpl();
    }
}
