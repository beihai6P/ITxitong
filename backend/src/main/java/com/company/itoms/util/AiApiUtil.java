package com.company.itoms.util;

import cn.hutool.crypto.digest.DigestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AiApiUtil {

    private static final String APP_SECRET = "ai_secret_key_12345";

    public static Map<String, String> generateSignHeader() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        
        String sign = DigestUtil.md5Hex(timestamp + nonce + APP_SECRET);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Timestamp", String.valueOf(timestamp));
        headers.put("X-Nonce", nonce);
        headers.put("X-Sign", sign);
        
        return headers;
    }
}
