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

public class RawTrustedStreamImpl implements ChaincodeTrustedStream {
    private final List<String> params;
    private final TrustedStreamCreationContext ctx;
    private final String streamId;
    private final TsCCApi apiProvider;
    private final static Logger LOG = Logger.getLogger(RawTrustedStreamImpl.class.getName());

    public RawTrustedStreamImpl(TrustedStreamCreationContext creationCtx, String streamId) {
        this.params = creationCtx.getParams();
        this.ctx = creationCtx;
        this.streamId = streamId;
        this.apiProvider = TsAPIProvider.provide(TsAPIProvider.PROVIDERS.INFLUX, creationCtx.getCtx());
    }

    @Override
    public void save() {
        LOG.info("About to persist lineProtocol " + params.size() + " entries...");
        final String lineProtocol = params.stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator()));
        LOG.info("Incoming lineProtocol entries to save:\n" + lineProtocol);
        apiProvider.putTsState(lineProtocol);
        }

    @Override
    public TrustedStream filter(Predicate<Point> predicate) {
        return new TrustedStreamImpl(params.stream().map(Point::fromString).collect(Collectors.toList()), ctx, streamId).filter(predicate);
    }

    @Override
    public TrustedStream map(Function<Point, Point> mapper) {
        return new TrustedStreamImpl(params.stream().map(Point::fromString).collect(Collectors.toList()), ctx, streamId).map(mapper);
    }

    @Override
    public void toLedger() {
        ctx.addPersistingStream(this);
    }

    public String getStreamId() {
        return streamId;
    }
}
