package com.github.fengzhizi319.privacy.sdk.api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * 脱敏 API（Data Masking API）。
 * <p>
 * 支持基于字段名自动识别敏感字段类型并进行掩码，同时提供 HMAC-SHA256 哈希、截断等辅助能力。
 * 可识别的字段类型包括：手机号（mobile）、身份证（id_card）、姓名（name）、银行卡（bank_card），其余按默认规则处理。
 * </p>
 *
 * <p><b>实现注意点：</b></p>
 * <ul>
 *   <li>掩码规则仅做格式上的遮挡，不可逆；需要不可逆场景请使用 {@link #hashValue(String, String)}。</li>
 *   <li>字段类型识别基于字段名关键字（不区分大小写），实际生产中建议结合元数据或标签更准确。</li>
 * </ul>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class MaskingApi {

    /** 默认盐值，用于无显式盐时的 HMAC 哈希；生产环境建议显式传入独立盐。 */
    private final String defaultSalt;

    /**
     * 使用默认盐值 "default-salt" 构造 API。
     */
    public MaskingApi() {
        this("default-salt");
    }

    /**
     * 使用指定盐值构造 API。
     *
     * @param defaultSalt 默认盐值，用于 {@link #hashValue(String, String)} 中 salt 为 {@code null} 时（当前实现要求显式传入 salt）
     */
    public MaskingApi(String defaultSalt) {
        this.defaultSalt = defaultSalt;
    }

    /**
     * 对单条字段值进行自动掩码。
     * <p>
     * 根据 {@code fieldName} 推断字段类型，并调用对应的掩码规则；{@code context} 参数当前保留用于未来按上下文选择策略。
     * </p>
     *
     * @param fieldName 字段名称，例如 "mobile"、"id_card"、"patient_name"
     * @param value     原始字段值
     * @param context   业务上下文标识（当前未参与决策，仅保留扩展）
     * @return 掩码后的字符串；若值格式不符或为空，可能原样返回
     */
    public String maskValue(String fieldName, String value, String context) {
        return switch (guessFieldType(fieldName)) {
            case "mobile" -> maskMobile(value);
            case "id_card" -> maskIdCard(value);
            case "name" -> maskName(value);
            case "bank_card" -> maskBankCard(value);
            default -> maskDefault(value, 3, 3);
        };
    }

    /**
     * 对值进行 HMAC-SHA256 哈希并截取前 16 位 Base64 字符。
     * <p>
     * 相同输入与盐将产生相同输出，可用于去标识化后的关联键生成；不同盐结果不同，可防止彩虹表攻击。
     * </p>
     *
     * @param value 待哈希的原始字符串
     * @param salt  哈希盐值
     * @return 16 位 Base64 哈希字符串
     * @throws RuntimeException 当 HMAC 算法不可用或初始化失败时抛出
     */
    public String hashValue(String value, String salt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] hash = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("Hash failed", e);
        }
    }

    /**
     * 将字符串截断为只保留指定前缀长度，其余替换为 "***"。
     *
     * @param value     原始字符串
     * @param keepPrefix 保留的前缀字符数
     * @return 截断后的字符串；若原始值长度小于等于保留长度，则原样返回
     */
    public String truncate(String value, int keepPrefix) {
        if (value == null || value.length() <= keepPrefix) {
            return value;
        }
        return value.substring(0, keepPrefix) + "***";
    }

    /**
     * 手机号掩码：保留前 3 位与后 4 位，中间替换为 4 个 *。
     *
     * @param value 原始手机号
     * @return 掩码后的手机号；长度非 11 位时原样返回
     */
    private String maskMobile(String value) {
        if (value == null || value.length() != 11) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(7);
    }

    /**
     * 身份证号掩码：保留前 6 位与后 4 位，中间替换为 8 个 *。
     *
     * @param value 原始身份证号
     * @return 掩码后的身份证号；长度非 18 位时原样返回
     */
    private String maskIdCard(String value) {
        if (value == null || value.length() != 18) {
            return value;
        }
        return value.substring(0, 6) + "********" + value.substring(14);
    }

    /**
     * 姓名掩码：保留首尾汉字，中间替换为 *。
     *
     * @param value 原始姓名
     * @return 掩码后的姓名；空值或单字时原样返回
     */
    private String maskName(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + "**" + value.charAt(value.length() - 1);
    }

    /**
     * 银行卡号掩码：保留前 4 位与后 4 位，中间按空格分组隐藏。
     *
     * @param value 原始银行卡号
     * @return 掩码后的卡号；长度小于 8 时原样返回
     */
    private String maskBankCard(String value) {
        if (value == null || value.length() < 8) {
            return value;
        }
        return value.substring(0, 4) + " **** **** " + value.substring(value.length() - 4);
    }

    /**
     * 默认掩码规则：保留前后指定长度，中间替换为 *。
     *
     * @param value  原始字符串
     * @param prefix 保留的前缀长度
     * @param suffix 保留的后缀长度
     * @return 掩码后的字符串；若长度不足则原样返回
     */
    private String maskDefault(String value, int prefix, int suffix) {
        if (value == null || value.length() <= prefix + suffix) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(value, 0, prefix);
        for (int i = prefix; i < value.length() - suffix; i++) {
            sb.append("*");
        }
        sb.append(value.substring(value.length() - suffix));
        return sb.toString();
    }

    /**
     * 根据字段名推断敏感字段类型。
     * <p>
     * 规则基于关键字匹配（不区分大小写），命中顺序为：mobile/phone、id_card/idcard/身份证、name/姓名、bank/card_no。
     * </p>
     *
     * @param fieldName 字段名
     * @return 类型标识字符串，如 "mobile"、"id_card"、"name"、"bank_card"、"default"
     */
    private String guessFieldType(String fieldName) {
        String lower = fieldName.toLowerCase();
        if (lower.contains("mobile") || lower.contains("phone")) {
            return "mobile";
        }
        if (lower.contains("id_card") || lower.contains("idcard") || lower.contains("身份证")) {
            return "id_card";
        }
        if (lower.contains("name") || lower.contains("姓名")) {
            return "name";
        }
        if (lower.contains("bank") || lower.contains("card_no")) {
            return "bank_card";
        }
        return "default";
    }

    /**
     * 对 map 中的每个字符串值按字段名进行脱敏，非字符串值保持原样。
     *
     * @param record  原始记录
     * @param context 业务上下文标识
     * @return 脱敏后的新记录映射；不会修改原 record 映射
     */
    public Map<String, Object> maskRecord(Map<String, Object> record, String context) {
        if (record == null) {
            return null;
        }
        Map<String, Object> out = new java.util.LinkedHashMap<>(record.size());
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof String val) {
                out.put(entry.getKey(), maskValue(entry.getKey(), val, context));
            } else {
                out.put(entry.getKey(), v);
            }
        }
        return out;
    }
}
