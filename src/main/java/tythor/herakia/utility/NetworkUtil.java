package tythor.herakia.utility;

import lombok.SneakyThrows;

import java.net.InetAddress;

public class NetworkUtil {
    @SneakyThrows
    public static String getHostname() {
        return InetAddress.getLocalHost().getCanonicalHostName();
    }

    public static String getTruncatedHostname() {
        return getHostname().split("\\.")[0];
    }
}
