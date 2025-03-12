package tythor.herakia.utility;

import lombok.Getter;
import lombok.SneakyThrows;

import java.net.InetAddress;

public class NetworkUtil {
    @Getter
    private static final String initialHostname = getHostname();

    @SneakyThrows
    public static String getHostname() {
        return InetAddress.getLocalHost().getCanonicalHostName();
    }

    public static String getTruncatedHostname() {
        return getHostname().split("\\.")[0];
    }

    public static String getTruncatedInitialHostname() {
        return getInitialHostname().split("\\.")[0];
    }
}
