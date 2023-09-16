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

package org.hyperledger.tempo.dsl;

import org.hyperledger.tempo.dsl.dto.Point;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PointTest {

    @Test
    public void pointToline(){
        Point myPoint = new Point("blood=pressure");
        myPoint.addTag("patientId", "b");
        myPoint.addField("diastolic", new Long(40));
        myPoint.addField("systolic", new Long(60));
        myPoint.setTime(1650875450206L, TimeUnit.MILLISECONDS);
        String str = myPoint.toLineProtocol();
        assertTrue(str, str.contains("pressure"));
    }
     @Test
    public void validBooleanField() {
        Point myPoint = Point.fromString("blood-pressure,id=a44,patientId=b diastolic=true,systolic=60i 90");
        assertEquals(myPoint.valueOfTag("id"),"a44");
        assertEquals(myPoint.valueOfTag("patientId"),"b");
        assertEquals(myPoint.valueOfField("diastolic"),new Boolean(true));
        assertEquals(myPoint.valueOfField("systolic"),new Long(60));
        assertEquals(myPoint.getTime(),new Long(90));
    }

    @Test
    public void withOutMeasurment() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString(",id=a44,patientId=b diastolic=40i,systolic=60i 90"));
        String expectedMsg ="Measurement missing!";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void invalidMeasurment() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("blood\\ pressure,id=a44,patientId=b diastolic=40i,systolic=60i 90"));
        String expectedMsg ="Invalid measurement";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void missingQuoteMeasurment() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("blood\"pressure,id=a44,patientId=b diastolic=40i,systolic=60i 90"));
        String expectedMsg ="Missing quote at line";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void invalidNegativeIntegerField() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("bloodpressure,id=,patientId=b diastolic=-9323372036854775808i,systolic=60i 90"));
        String expectedMsg ="Unable to parse Integer";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }
    @Test
    public void invalidIntegerField() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("bloodpressure,id=a23,patientId=b diastolic=18446744073709551616i,systolic=60i 90"));
        String expectedMsg ="Unable to parse Integer";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void validDoubleField() {
        Point myPoint = Point.fromString("bloodpressure,id=55,patientId=b diastolic=1844.444444,systolic=60i 90");

        assertEquals(myPoint.valueOfTag("id"),"55");
        assertEquals(myPoint.valueOfTag("patientId"),"b");
        assertEquals(myPoint.valueOfField("diastolic"),new Double(1844.444444));
        assertEquals(myPoint.valueOfField("systolic"),new Long(60));
        assertEquals(myPoint.getTime(),new Long(90));
    }

    @Test
    public void invalidTag() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("bloodpressure,id,patientId=b diastolic=1844.444444,systolic=60i 90"));
        String expectedMsg ="Invalid tag format:";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void invalidWithoutFields() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("bloodpressure,id=10,patientId=b 90"));
        String expectedMsg ="Invalid field format";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void validWithoutTags() {
        Point myPoint = Point.fromString("bloodpressure diastolic=1844.444444,systolic=60i 90");
        assertEquals(myPoint.valueOfField("diastolic"),new Double(1844.444444));
        assertEquals(myPoint.valueOfField("systolic"),new Long(60));
        assertEquals(myPoint.getTime(),new Long(90));
    }

    @Test
    public void invalidWithoutMetric() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("bloodpressure,id=55,patientId=b diastolic=false,systolic=60i"));
        String expectedMsg ="Point without metric is unsupported.";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    public void addingFieldsAndTags() {
        Point myPoint = new Point("bloodpressure").addTag("id","10").addField("systolic",new Long(45)).addField("diastolic",107L);
        assertEquals(myPoint.valueOfField("diastolic"),107L);
        assertEquals(myPoint.valueOfField("systolic"),new Long(45));
        assertEquals(myPoint.valueOfTag("id"),"10");
    }

    @Test
    public void pointTolineMS(){
        Point myPoint = new Point("blood=pressure");
        myPoint.addTag("patientId", "b");
        myPoint.addField("diastolic", new Long(40));
        myPoint.addField("systolic", new Long(60));
        myPoint.setTime(1650875450206L, TimeUnit.MILLISECONDS);
        String str = myPoint.toLineProtocol();
        assertTrue(str, str.contains("1650875450206000000"));

    }

    @Test
    public void pointTolineNS(){
        Point myPoint = new Point("blood=pressure");
        myPoint.addTag("patientId", "b");
        myPoint.addField("diastolic", new Long(40));
        myPoint.addField("systolic", new Long(60));
        myPoint.setTime(1651391233349000000L, TimeUnit.NANOSECONDS);
        String str = myPoint.toLineProtocol();
        assertTrue(str, str.contains("1651391233349000000"));
    }

    @Test
    public void pointTolineMicroseconds(){
        Point myPoint = new Point("blood=pressure");
        myPoint.addTag("patientId", "b");
        myPoint.addField("diastolic", new Long(40));
        myPoint.addField("systolic", new Long(60));
        myPoint.setTime(1651396241888000L, TimeUnit.MICROSECONDS);
        String str = myPoint.toLineProtocol();
        assertTrue(str, str.contains("1651396241888000000"));
    }

    @Test
    public void pointTolineSec() {
        Point myPoint = new Point("bloodpressure");
        myPoint.addTag("patientId", "b");
        myPoint.addField("diastolic", new Long(40));
        myPoint.addField("systolic", new Long(60));
        myPoint.setTime(1651416607L, TimeUnit.SECONDS);
        String str = myPoint.toLineProtocol();
        assertTrue(str, str.contains("1651416607000000000"));
    }
    @Test
    public void pointFromStringWithDataAfterTime() {
        Exception exception=assertThrows(IllegalArgumentException.class, ()-> Point.fromString("traffic_camera,cameraId=53,id=53,speed=69 eventId=12i,plateNumber=\"WY6E6B4CS\" 1656408480634000000 \n sss"));
        String expectedMsg ="Invalid line protocol format. Can't convert point with data";
        assertTrue(exception.getMessage().contains(expectedMsg));
    }

}
