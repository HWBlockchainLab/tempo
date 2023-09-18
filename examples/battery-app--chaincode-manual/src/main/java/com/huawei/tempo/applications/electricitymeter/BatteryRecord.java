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

package org.hyperledger.tempo.applications.batteryrecorder;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.tempo.dsl.annotation.Column;
import org.hyperledger.tempo.dsl.annotation.Measurement;

@Measurement(name = "battery_record")
public class BatteryRecord {
    @JsonProperty("batteryId")
    @Column(name = "batteryId", tag = true)
    private long batteryId;

    @JsonProperty("time")
    @Column(name = "time")
    private long time;

    @JsonProperty("batteryBrand")
    @Column(name = "batteryBrand")
    private String batteryBrand;

    @JsonProperty("batteryType")
    @Column(name = "batteryType")
    private String batteryType;

    @JsonProperty("batteryPower")
    @Column(name = "batteryPower")
    private long batteryPower;

    @JsonProperty("batteryTemperature")
    @Column(name = "batteryTemperature")
    private long batteryTemperature;

    public long getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(long batteryId) {
        this.batteryId = batteryId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getBatteryBrand() {
        return batteryBrand;
    }

    public void setBatteryBrand(String batteryBrand) {
        this.batteryBrand = batteryBrand;
    }

    public String getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }

    public long getBatteryPower() {
        return batteryPower;
    }

    public void setBatteryPower(long batteryPower) {
        this.batteryPower = batteryPower;
    }

    public long getBatteryTemperature() {
        return batteryTemperature;
    }

    public void setBatteryTemperature(long batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }
}
