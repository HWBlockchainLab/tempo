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
import org.hyperledger.tempo.dsl.interfaces.streams.TrustedStream;
import org.hyperledger.tempo.tscc.TsCCApi;
import org.hyperledger.tempo.tscc.api.TsAPIProvider;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TrustedStreamImpl implements ChaincodeTrustedStream {
    private final List<Point> points;
    private final TrustedStreamCreationContext ctx;
    private final String streamId;
    private final TsCCApi apiProvider;

    private long childCount = 0;

    private final static Logger LOG = Logger.getLogger(TrustedStreamImpl.class.getName());

    TrustedStreamImpl(List<Point> points, TrustedStreamCreationContext ctx, String streamId) {
        this.points = points;
        this.ctx = ctx;
        this.streamId = streamId;
        this.apiProvider = TsAPIProvider.provide(TsAPIProvider.PROVIDERS.INFLUX, ctx.getCtx());
    }

    @Override
    public void save() {
        LOG.info("About to persist " + points.size() + " points...");
        final StringBuilder lineProtocolSB = new StringBuilder();
        if (points != null) {
            points.forEach(point -> {
                lineProtocolSB.append(point.toLineProtocol()).append('\n');
            });
        } else
            LOG.info("PointStream is null!!!");

        LOG.info("Incoming lineProtocol entries (converted from Point to line) to save:\n" + lineProtocolSB);
        apiProvider.putTsState(lineProtocolSB.toString());
    }

    @Override
    public TrustedStream filter(Predicate<Point> predicate) {
        final String childStreamId = createChildStreamId();
        return new TrustedStreamImpl(points.stream().filter(predicate).collect(Collectors.toList()), ctx, childStreamId);
    }

    @Override
    public TrustedStream map(Function<Point, Point> mapper) {
        final String childStreamId = createChildStreamId();
        return new TrustedStreamImpl(points.stream().map(mapper).collect(Collectors.toList()), ctx, childStreamId);
    }

    @Override
    public void toLedger() {
        ctx.addPersistingStream(this);
    }

    private String createChildStreamId() {
        childCount++;
        return streamId + "." + childCount;
    }

    public String getStreamId() {
        return streamId;
    }
}
