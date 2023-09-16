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

import org.hyperledger.tempo.dsl.interfaces.PointAggregator;
import org.hyperledger.tempo.dsl.interfaces.PointInitializer;
import org.hyperledger.tempo.dsl.interfaces.PointReducer;
import org.hyperledger.tempo.dsl.interfaces.streams.GroupedPointStream;
import org.hyperledger.tempo.dsl.interfaces.streams.PointStream;

import java.time.Duration;


public class GroupedPointStreamImpl implements GroupedPointStream {

    private final PointStream dummyPointStream;

    public GroupedPointStreamImpl() {
        this.dummyPointStream = new PointStreamImpl();
    }


    @Override
    public PointStream reduce(PointReducer reducer) {
        return this.dummyPointStream;
    }

    @Override
    public PointStream aggregate(PointInitializer reducer, PointAggregator aggregator) {
        return this.dummyPointStream;
    }

    @Override
    public PointStream windowedAggregate(Duration duration, PointInitializer initializer, PointAggregator aggregator) {
        return this.dummyPointStream;
    }

    @Override
    public PointStream windowedReduce(Duration duration, PointReducer reducer) {
        return this.dummyPointStream;
    }
}
