package tythor.herakia.utility;

import java.math.BigDecimal;

public class DigitUtil {
    public static String truncateDouble(double d, int minimumDigits) {
        String bdString = BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        if (bdString.length() >= minimumDigits && !bdString.substring(0, minimumDigits).contains(".")) return new BigDecimal(bdString).toBigInteger().toString();

        String format = String.format("%." + minimumDigits + "f", d);
        int diff = format.length() - (minimumDigits + 1);
        return diff == 0 ? format : format.substring(0, format.length() - diff);
    }
}
