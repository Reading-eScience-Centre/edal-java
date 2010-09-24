/*
 * Copyright (c) 2010 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.Map;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.RecordType;

/**
 * A simple implementation of a {@link Record}.
 * @author Jon
 */
public final class SimpleRecord implements Record {

    private final Map<String, Object> values;
    private final RecordType recordType;

    public SimpleRecord(Map<String, Object> values, RecordType recordType) {
        if (values == null || recordType == null) {
            throw new NullPointerException();
        }
        this.values = values;
        this.recordType = recordType;
    }

    @Override
    public Object getValue(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RecordType getRecordType() {
        return this.recordType;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + this.values.hashCode();
        hashCode = 31 * hashCode + this.recordType.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SimpleRecord)) return false;
        SimpleRecord other = (SimpleRecord)obj;

        return this.values.equals(other.values) && this.recordType.equals(other.recordType);
    }



}
