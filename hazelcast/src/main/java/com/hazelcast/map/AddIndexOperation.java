/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.SerializationServiceImpl;
import com.hazelcast.query.impl.Index;
import com.hazelcast.query.impl.IndexService;
import com.hazelcast.query.impl.QueryEntry;
import com.hazelcast.spi.PartitionLevelOperation;
import com.hazelcast.spi.impl.AbstractNamedOperation;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

public class AddIndexOperation extends AbstractNamedOperation implements PartitionLevelOperation {

    String attributeName;
    boolean ordered;

    public AddIndexOperation(String name, String attributeName, boolean ordered) {
        super(name);
        this.attributeName = attributeName;
        this.ordered = ordered;
    }

    public AddIndexOperation() {
    }

    @Override
    public void run() throws Exception {
        MapService mapService = getService();
        MapContainer mapContainer = mapService.getMapInfo(name);
        RecordStore rs = mapService.getPartitionContainer(getPartitionId()).getRecordStore(name);
        ConcurrentMap<Data, Record> records = rs.getRecords();
        IndexService indexService = mapContainer.getIndexService();
        SerializationServiceImpl ss = (SerializationServiceImpl) getNodeEngine().getSerializationService();
        Index index = indexService.addOrGetIndex(attributeName, ordered);
        for (Record record : records.values()) {
            Data key = record.getKey();
            index.saveEntryIndex(new QueryEntry(ss, key, key, record.getValue()));
        }
    }

    @Override
    public Object getResponse() {
        return Boolean.TRUE;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(attributeName);
        out.writeBoolean(ordered);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        attributeName = in.readUTF();
        ordered = in.readBoolean();
    }
}
