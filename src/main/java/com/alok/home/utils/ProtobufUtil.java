package com.alok.home.utils;

import com.alok.home.response.GetInvestmentsResponse;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

public class ProtobufUtil {

    @SuppressWarnings("unchecked")
    public static <T extends Message> T fromJson(String json, Class<T> clazz) throws IOException {
        //Message.Builder builder = Struct.newBuilder();
        Message.Builder builder = null;
        try {
            builder = (Message.Builder) clazz.getMethod("newBuilder").invoke(null);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return (T) builder.build();
    }
}
