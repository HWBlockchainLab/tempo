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

package org.hyperledger.tempo.dsl.report;

import org.hyperledger.tempo.dsl.interfaces.report.ReportQuery;
import org.hyperledger.tempo.dsl.interfaces.streams.IngestStreamCreationContext;

public class ReportQueryBuilder {
    public static ReportQuery build(String name, IngestStreamCreationContext ctx, final String... measurements) {
        return new FluxReportQuery(name, measurements, ctx);
    }
}