package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类输入模型（Classification Input）。
 * <p>
 * 统一封装单条记录或表结构输入，便于在不同 API 方法之间复用。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ClassificationInput {

    /** 输入类型：record 或 table。 */
    private InputType type;

    /** 单条记录，key 为字段名，value 为字段值。 */
    private Map<String, Object> record = new LinkedHashMap<>();

    /** 表结构，列名列表。 */
    private List<String> schema = new ArrayList<>();

    /** 表数据，每个元素为一条记录。 */
    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 输入类型枚举。
     */
    public enum InputType {
        /** 单条记录。 */
        RECORD,
        /** 表/批次。 */
        TABLE
    }

    /**
     * 默认构造器。
     */
    public ClassificationInput() {
    }

    /**
     * 从单条记录构造输入。
     *
     * @param record 单条记录
     * @return 分类输入对象
     */
    public static ClassificationInput ofRecord(Map<String, Object> record) {
        ClassificationInput input = new ClassificationInput();
        input.type = InputType.RECORD;
        input.record = record == null ? new LinkedHashMap<>() : record;
        return input;
    }

    /**
     * 从表结构及行数据构造输入。
     *
     * @param schema 列名列表
     * @param rows   行数据
     * @return 分类输入对象
     */
    public static ClassificationInput ofTable(List<String> schema, List<Map<String, Object>> rows) {
        ClassificationInput input = new ClassificationInput();
        input.type = InputType.TABLE;
        input.schema = schema == null ? new ArrayList<>() : schema;
        input.rows = rows == null ? new ArrayList<>() : rows;
        return input;
    }

    public InputType getType() {
        return type;
    }

    public void setType(InputType type) {
        this.type = type;
    }

    public Map<String, Object> getRecord() {
        return record;
    }

    public void setRecord(Map<String, Object> record) {
        this.record = record == null ? new LinkedHashMap<>() : record;
    }

    public List<String> getSchema() {
        return schema;
    }

    public void setSchema(List<String> schema) {
        this.schema = schema == null ? new ArrayList<>() : schema;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }
}
