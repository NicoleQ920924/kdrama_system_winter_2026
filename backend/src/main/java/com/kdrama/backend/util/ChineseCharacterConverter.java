package com.kdrama.backend.util;

// Chinese Character Converter Library from GitHub
import com.github.houbb.opencc4j.util.ZhConverterUtil;

public class ChineseCharacterConverter {
    public static String toTraditional(String text) {
        return ZhConverterUtil.toTraditional(text);
    }

    public static String toSimplified(String text) {
        return ZhConverterUtil.toSimple(text);
    }
}