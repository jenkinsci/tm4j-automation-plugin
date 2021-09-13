package com.adaptavist.tm4j.jenkins.cucumber;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

// this parser was based on https://github.com/cucumber/cucumber-json-schema/blob/main/schema.json
public class CucumberReportParser {

    private final JsonReader reader;
    private final JsonWriter writer;

    public CucumberReportParser(JsonReader reader, JsonWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void parseFeatures() throws IOException {
        beginArray();
        while (reader.hasNext()) {
            parseFeature();
        }
        endArray();
    }

    private void parseFeature() throws IOException {
        beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();
            if ("name".equals(tag)) {
                writer.name("name").value(reader.nextString());
            } else if ("keyword".equals(tag)) {
                writer.name("keyword").value(reader.nextString());
            } else if ("uri".equals(tag)) {
                writer.name("uri").value(reader.nextString());
            } else if ("line".equals(tag)) {
                writer.name("line").value(reader.nextInt());
            } else if ("description".equals(tag)) {
                writer.name("description").value(reader.nextString());
            } else if ("id".equals(tag)) {
                writer.name("id").value(reader.nextString());
            } else if ("tags".equals(tag) && reader.peek() != JsonToken.NULL) {
                parseTags();
            } else if ("elements".equals(tag) && reader.peek() != JsonToken.NULL) {
                parseElements();
            } else {
                reader.skipValue();
            }
        }
        endObject();
    }

    private void parseTags() throws IOException {
        writer.name("tags");
        beginArray();
        while (reader.hasNext()) {
            parseTag();
        }
        endArray();
    }

    private void parseTag() throws IOException {
        beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();
            if ("line".equals(tag)) {
                writer.name("line").value(reader.nextInt());
            } else if ("name".equals(tag)) {
                writer.name("name").value(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        endObject();
    }

    private void parseElements() throws IOException {
        writer.name("elements");
        beginArray();
        while (reader.hasNext()) {
            parseElement();
        }
        endArray();
    }

    private void parseElement() throws IOException {
        beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();
            if ("line".equals(tag)) {
                writer.name("line").value(reader.nextLong());
            } else if ("name".equals(tag)) {
                writer.name("name").value(reader.nextString());
            } else if ("description".equals(tag)) {
                writer.name("description").value(reader.nextString());
            } else if ("id".equals(tag)) {
                writer.name("id").value(reader.nextString());
            } else if ("type".equals(tag)) {
                writer.name("type").value(reader.nextString());
            } else if ("keyword".equals(tag)) {
                writer.name("keyword").value(reader.nextString());
            } else if ("steps".equals(tag) && reader.peek() != JsonToken.NULL) {
                parseSteps();
            } else if ("tags".equals(tag) && reader.peek() != JsonToken.NULL) {
                parseTags();
            } else {
                reader.skipValue();
            }
        }
        endObject();
    }

    private void parseSteps() throws IOException {
        writer.name("steps");
        beginArray();
        while (reader.hasNext()) {
            parseStep();
        }
        endArray();
    }

    private void parseStep() throws IOException {
        beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();
            if ("line".equals(tag)) {
                writer.name("line").value(reader.nextLong());
            } else if ("name".equals(tag)) {
                writer.name("name").value(reader.nextString());
            } else if ("keyword".equals(tag)) {
                writer.name("keyword").value(reader.nextString());
            } else if ("hidden".equals(tag)) {
                writer.name("hidden").value(reader.nextBoolean());
            } else if ("result".equals(tag)) {
                writer.name("result");
                parseResult();
            } else {
                reader.skipValue();
            }
        }
        endObject();
    }

    private void parseResult() throws IOException {
        beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();
            if ("status".equals(tag)) {
                writer.name("status").value(reader.nextString());
            } else if ("duration".equals(tag)) {
                writer.name("duration").value(reader.nextLong());
            } else if ("error_message".equals(tag)) {
                writer.name("error_message").value(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        endObject();
    }

    private void beginObject() throws IOException {
        reader.beginObject();
        writer.beginObject();
    }

    private void endObject() throws IOException {
        reader.endObject();
        writer.endObject();
    }

    private void beginArray() throws IOException {
        reader.beginArray();
        writer.beginArray();
    }

    private void endArray() throws IOException {
        writer.endArray();
        reader.endArray();
    }
}
