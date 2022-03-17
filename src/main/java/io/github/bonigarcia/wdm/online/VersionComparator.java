package io.github.bonigarcia.wdm.online;

import io.github.bonigarcia.wdm.config.Config;

import java.util.Comparator;

import static java.lang.Integer.signum;
import static java.lang.Integer.valueOf;

public class VersionComparator extends Configuration implements Comparator<String> {

    public VersionComparator(Config config) {
        this.config = config;
    }

    @Override
    public int compare(String str1, String str2) {
        String versionRegex = config.getBrowserVersionDetectionRegex();
        String[] vals1 = str1.replaceAll(versionRegex, "").split("\\.");
        String[] vals2 = str2.replaceAll(versionRegex, "").split("\\.");

        if (vals1[0].equals("")) {
            vals1[0] = "0";
        }
        if (vals2[0].equals("")) {
            vals2[0] = "0";
        }

        int i = 0;
        while (i < vals1.length && i < vals2.length
                && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            return signum(valueOf(vals1[i]).compareTo(valueOf(vals2[i])));
        } else {
            return signum(vals1.length - vals2.length);
        }
    }
}
