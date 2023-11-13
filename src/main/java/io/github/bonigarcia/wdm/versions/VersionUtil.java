package io.github.bonigarcia.wdm.versions;

import java.util.Comparator;

public class VersionUtil implements Comparator<String> {


    @Override
    public int compare(String v1, String v2) {
        String[] v1split = v1.split("\\.");
        String[] v2split = v2.split("\\.");
        int length = Integer.max(v1split.length, v2split.length);
        for (int i = 0; i < length; i++) {
            try {
                int v1Part = i < v1split.length ? Integer.parseInt(v1split[i]) : 0;
                int v2Part = i < v2split.length ? Integer.parseInt(v2split[i]) : 0;
                if (v1Part < v2Part) {
                    return -1;
                }
                if (v1Part > v2Part) {
                    return 1;
                }
            } catch (Exception e) {
                // Log or handle the exception as needed
            }
        }
        return 0;
    }
}
