/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kahadb.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Implementation of a Marshaller for a Integer
 * 
 * @version $Revision$
 */
public class IntegerMarshaller implements Marshaller<Integer> {
    
    public static final IntegerMarshaller INSTANCE = new IntegerMarshaller();
    
    public void writePayload(Integer object, DataOutput dataOut) throws IOException {
        dataOut.writeInt(object);
    }

    public Integer readPayload(DataInput dataIn) throws IOException {
        return dataIn.readInt();
    }

    public int getFixedSize() {
        return 4;
    }

    
    /** 
     * @return the source object since integers are immutable. 
     */
    public Integer deepCopy(Integer source) {
        return source;
    }

    public boolean isDeepCopySupported() {
        return true;
    }
}
