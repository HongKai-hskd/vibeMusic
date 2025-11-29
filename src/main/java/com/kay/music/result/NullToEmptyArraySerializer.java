package com.kay.music.result;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 自定义序列化器：当字段值为 null 时，序列化为空数组 []
 * 这样可以保证前端始终接收到 data 字段，避免 null 导致的问题
 * 
 * @Author: Kay
 * @date:   2025/11/29
 */
public class NullToEmptyArraySerializer extends JsonSerializer<Object> {
    
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 当值为 null 时，写入空数组
        gen.writeStartArray();
        gen.writeEndArray();
    }
}
