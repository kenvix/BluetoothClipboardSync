package com.kenvix.android;

public final class ApplicationProperties {
    private ApplicationProperties() {
    }

    public static final String ForumUrl = "https://x.kenvix.com:6151/";
    public static final String BaseServerUrl = "https://x.kenvix.com:5555/";
    public static final String BluetoothSyncUUID = "e2ae9f67-a140-487c-bb8e-54a9370e940e";

    public static final String BluetoothSyncChannelID = "com.kenvix.SyncService";
    public static final String BluetoothSyncChannelIDEmergency = "com.kenvix.SyncServiceE";

    public static final long OkHttpClientTimeout = 10;
    public static final long OkHttpClientCacheSize = 1000000000L;

    public static String getServerApiUrl(String path) {
        return BaseServerUrl + path;
    }
}