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

import org.hyperledger.tempo.dsl.interfaces.streams.IngestStreamCreationContext;
import org.hyperledger.tempo.dsl.interfaces.streams.TrustedStream;
import org.hyperledger.fabric.contract.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrustedStreamCreationContext implements IngestStreamCreationContext {
    private final List<String> params;
    private final Context ctx;

    //    private final Map<String, ChaincodeTrustedStream> knownStreams = new HashMap<>();
    private final Set<TrustedStream> persistingStreams = new HashSet<>();

    public TrustedStreamCreationContext(List<String> params, Context ctx) {
        this.params = params;
        this.ctx = ctx;
    }

    public Context getCtx() {
        return ctx;
    }

    public List<String> getParams() {
        return params;
    }

//    public void registerStream(String streamId, ChaincodeTrustedStream trustedStream) {
//        knownStreams.put(streamId, trustedStream);
//    }
//
//    public ChaincodeTrustedStream getStream(String streamId) {
//        return knownStreams.get(streamId);
//    }

    public Set<TrustedStream> getPersistingStreams() {
        return persistingStreams;
    }

    public void addPersistingStream(TrustedStream persistingStream) {
        this.persistingStreams.add(persistingStream);
    }
}
