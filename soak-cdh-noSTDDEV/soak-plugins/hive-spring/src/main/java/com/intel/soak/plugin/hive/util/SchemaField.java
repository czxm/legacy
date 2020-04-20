package com.intel.soak.plugin.hive.util;

import java.sql.Types;

public class SchemaField {
    protected String name;
    protected int length;
    protected int type = Types.VARCHAR;
    protected String typeName =  null;
    protected boolean tableKey = false;
    protected boolean nullable = false;

    public SchemaField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(this.name != name) {
            String oldName = this.name;
            this.name = name;
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        if(this.type != type) {
            int oldType = this.type;
            this.type = type;
        }
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        if(this.typeName != typeName) {
            String oldTypeName = this.typeName;
            this.typeName = typeName;
        }
    }

    public void setTableKey(boolean tableKey) {
        if(this.tableKey != tableKey) {
            boolean oldTableKey = this.tableKey;
            this.tableKey = tableKey;
        }
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        if(this.length != length) {
            int oldLength = this.length;
            this.length = length;
        }
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        if(this.nullable != nullable) {
            boolean oldNullable = this.nullable;
            this.nullable = nullable;
        }
    }

    public void from(SchemaField other) {
        setName(other.getName());
        setLength(other.getLength());
        setNullable(other.isNullable());
    }
}
