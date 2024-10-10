package ru.yandex.practicum.filmorate.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void write(JsonWriter jsonWriter, LocalDate date) throws IOException {
        if (date == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(date.format(formatter));
        }
    }

    @Override
    public LocalDate read(JsonReader jsonReader) throws IOException {

        return LocalDate.parse(jsonReader.nextString(), formatter);
    }
}