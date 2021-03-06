/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.runtimefields.fielddata;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.IndexFieldDataCache;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.index.fielddata.SortedNumericDoubleValues;
import org.elasticsearch.index.fielddata.plain.LeafDoubleFieldData;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.indices.breaker.CircuitBreakerService;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.xpack.runtimefields.mapper.DoubleFieldScript;

import java.io.IOException;

public final class ScriptDoubleFieldData extends IndexNumericFieldData {

    public static class Builder implements IndexFieldData.Builder {
        private final String name;
        private final DoubleFieldScript.LeafFactory leafFactory;

        public Builder(String name, DoubleFieldScript.LeafFactory leafFactory) {
            this.name = name;
            this.leafFactory = leafFactory;
        }

        @Override
        public ScriptDoubleFieldData build(IndexFieldDataCache cache, CircuitBreakerService breakerService, MapperService mapperService) {
            return new ScriptDoubleFieldData(name, leafFactory);
        }
    }

    private final String fieldName;
    DoubleFieldScript.LeafFactory leafFactory;

    private ScriptDoubleFieldData(String fieldName, DoubleFieldScript.LeafFactory leafFactory) {
        this.fieldName = fieldName;
        this.leafFactory = leafFactory;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public ValuesSourceType getValuesSourceType() {
        return CoreValuesSourceType.NUMERIC;
    }

    @Override
    public ScriptDoubleLeafFieldData load(LeafReaderContext context) {
        try {
            return loadDirect(context);
        } catch (Exception e) {
            throw ExceptionsHelper.convertToElastic(e);
        }
    }

    @Override
    public ScriptDoubleLeafFieldData loadDirect(LeafReaderContext context) throws IOException {
        return new ScriptDoubleLeafFieldData(new ScriptDoubleDocValues(leafFactory.newInstance(context)));
    }

    @Override
    public NumericType getNumericType() {
        return NumericType.DOUBLE;
    }

    @Override
    protected boolean sortRequiresCustomComparator() {
        return true;
    }

    public static class ScriptDoubleLeafFieldData extends LeafDoubleFieldData {
        private final ScriptDoubleDocValues scriptDoubleDocValues;

        ScriptDoubleLeafFieldData(ScriptDoubleDocValues scriptDoubleDocValues) {
            super(0);
            this.scriptDoubleDocValues = scriptDoubleDocValues;
        }

        @Override
        public SortedNumericDoubleValues getDoubleValues() {
            return scriptDoubleDocValues;
        }

        @Override
        public void close() {}
    }
}
