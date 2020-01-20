package com.kenvix.utils.android;

import java.util.HashSet;
import java.util.Set;

public class EnvConfig {
    public static final String TargetAppPackage = "com.kenvix.clipboardsync";

    public static final Set<String> ExtendedProcessPackages = new HashSet<String>() {{
        add("com.kenvix.android");
    }};
}
