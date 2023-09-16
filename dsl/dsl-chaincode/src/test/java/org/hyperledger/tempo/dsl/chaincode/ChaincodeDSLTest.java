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

import org.hyperledger.tempo.dsl.TrustedStreamBuilder;
import org.hyperledger.tempo.dsl.interfaces.streams.TrustedStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChaincodeDSLTest {
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSimpleTrustStreamName() {
        final TrustedStreamCreationContext ctx = new TrustedStreamCreationContext(Collections.emptyList(), null);
        final TrustedStream trustedStream = TrustedStreamBuilder.build(ctx, "TEST_INGEST_STREAM");

        final String streamId = ((RawTrustedStreamImpl) trustedStream).getStreamId();
        assertEquals(streamId, "TEST_INGEST_STREAM");
    }

    @Test
    void testTrustStreamName2Legs() {
        final TrustedStreamCreationContext ctx = new TrustedStreamCreationContext(Collections.emptyList(), null);
        final TrustedStream trustedStream = TrustedStreamBuilder.build(ctx, "TEST_INGEST_STREAM").filter(x -> true);

        final String streamId = ((TrustedStreamImpl) trustedStream).getStreamId();
        assertEquals("TEST_INGEST_STREAM.1", streamId);
    }

    @Test
    void testTrustStreamName3Legs() {
        final TrustedStreamCreationContext ctx = new TrustedStreamCreationContext(Collections.emptyList(), null);
        final TrustedStream trustedStream = TrustedStreamBuilder.build(ctx, "TEST_INGEST_STREAM").filter(x -> true).map(x -> x);

        final String streamId = ((TrustedStreamImpl) trustedStream).getStreamId();
        assertEquals("TEST_INGEST_STREAM.1.1", streamId);
    }

    @Test
    void testTrustStreamNameWithBranches() {
        final TrustedStreamCreationContext ctx = new TrustedStreamCreationContext(Collections.emptyList(), null);
        final TrustedStream testStream = TrustedStreamBuilder.build(ctx, "TEST_INGEST_STREAM");
        final TrustedStream trustedStream1 = testStream.filter(x -> false);
        final TrustedStream trustedStream2 = trustedStream1
                .filter(x -> true)
                .map(x -> x);
        final TrustedStream trustedStream3 = trustedStream1
                .filter(x -> true)
                .map(x -> x);

        final String streamId = ((TrustedStreamImpl) trustedStream1).getStreamId();
        final String streamId2 = ((TrustedStreamImpl) trustedStream2).getStreamId();
        final String streamId3 = ((TrustedStreamImpl) trustedStream3).getStreamId();

        assertEquals("TEST_INGEST_STREAM.1", streamId);
        assertEquals("TEST_INGEST_STREAM.1.1.1", streamId2);
        assertEquals("TEST_INGEST_STREAM.1.2.1", streamId3);
    }

    @Test
    void testTraversePersistingBranches() {
        final TrustedStreamCreationContext ctx = new TrustedStreamCreationContext(Collections.emptyList(), null);
        final TrustedStream trustedStream = TrustedStreamBuilder.build(ctx, "TEST_INGEST_STREAM");
        final TrustedStream trustedStream1 = trustedStream.filter(x -> false);
        final TrustedStream trustedStream2 = trustedStream1
                .filter(x -> true)
                .map(x -> x);
        final TrustedStream trustedStream3 = trustedStream1
                .filter(x -> true)
                .map(x -> x);

        trustedStream.toLedger();
        trustedStream3.toLedger();

        Set<TrustedStream> persistingStreams = ctx.getPersistingStreams();
        assertEquals(2, persistingStreams.size());
        assertTrue(persistingStreams.contains(trustedStream));
        assertTrue(persistingStreams.contains(trustedStream3));
    }
}
