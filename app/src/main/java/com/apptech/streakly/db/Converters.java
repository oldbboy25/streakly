package com.apptech.streakly.db;

import androidx.room.TypeConverter;

import com.apptech.streakly.model.FrequencyType;

public class Converters {

    @TypeConverter
    public static FrequencyType toFrequencyType(String value) {
        return value == null ? FrequencyType.DAILY : FrequencyType.valueOf(value);
    }

    @TypeConverter
    public static String fromFrequencyType(FrequencyType type) {
        return type == null ? FrequencyType.DAILY.name() : type.name();
    }
}
