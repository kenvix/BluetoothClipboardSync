package com.kenvix.clipboardsync;

public final class ApplicationProperties {
    private ApplicationProperties() {
    }

    public static final String ForumUrl = "https://x.kenvix.com:6151/";
    public static final String BaseServerUrl = "https://x.kenvix.com:5555/";
    public static final String RecognizerPath = "Recognizer/Food";
    public static final long OkHttpClientTimeout = 10;
    public static final long OkHttpClientCacheSize = 1000000000L;

    public static String getServerApiUrl(String path) {
        return BaseServerUrl + path;
    }
}