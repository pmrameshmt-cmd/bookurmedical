package com.bookurmedical.appsecurity;

import org.json.JSONObject;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class JSONObjectHttpMessageConverter implements HttpMessageConverter<JSONObject> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return JSONObject.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return JSONObject.class.isAssignableFrom(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_JSON);
    }

    @Override
    public JSONObject read(Class<? extends JSONObject> clazz, HttpInputMessage inputMessage) throws IOException {
        return new JSONObject(new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    @Override
    public void write(JSONObject jsonObject, MediaType contentType, HttpOutputMessage outputMessage) throws IOException {
        outputMessage.getBody().write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
    }
}

