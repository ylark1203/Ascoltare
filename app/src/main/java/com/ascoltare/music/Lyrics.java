package com.ascoltare.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lyrics {
    public static class Entry {
        public final float time;
        public final String text;

        Entry(float time, String text) {
            this.time = time;
            this.text = text;
        }
    }

    private static final Pattern TIMESTAMP = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?\\]");

    public static List<Entry> parse(String lrc) {
        List<Entry> entries = new ArrayList<>();
        if (lrc == null || lrc.trim().isEmpty()) return entries;
        String[] lines = lrc.replace("\r", "").split("\n");
        for (String line : lines) {
            Matcher matcher = TIMESTAMP.matcher(line);
            List<Float> times = new ArrayList<>();
            int end = 0;
            while (matcher.find()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                String fraction = matcher.group(3);
                float milliseconds = 0;
                if (fraction != null) {
                    milliseconds = Integer.parseInt(fraction) / (float) Math.pow(10, fraction.length());
                }
                times.add(minutes * 60 + seconds + milliseconds);
                end = matcher.end();
            }
            String value = line.substring(end).trim();
            if (value.isEmpty()) continue;
            for (float time : times) entries.add(new Entry(time, value));
        }
        Collections.sort(entries, Comparator.comparingDouble(entry -> entry.time));
        return entries;
    }

    public static int indexAt(List<Entry> entries, float seconds) {
        int index = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).time <= seconds) index = i;
            else break;
        }
        return index;
    }
}
