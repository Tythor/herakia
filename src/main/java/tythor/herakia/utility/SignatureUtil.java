package tythor.herakia.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignatureUtil {
    public static String getSignature(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index];
        String className = stackTraceElement.getClassName();
        String methodName = extractMethodName(stackTraceElement);
        return className + "." + methodName;
    }

    public static String getSimpleSignature(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index];
        String className = stackTraceElement.getClassName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = extractMethodName(stackTraceElement);
        return simpleClassName + "." + methodName;
    }

    public static String getMethodName(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index];
        return extractMethodName(stackTraceElement);
    }

    private static String extractMethodName(StackTraceElement stackTraceElement) {
        String methodName = stackTraceElement.getMethodName();
        Pattern pattern = Pattern.compile("lambda\\$(.*?)\\$");
        Matcher matcher = pattern.matcher(methodName);
        return matcher.find() ? matcher.group(1) : methodName;
    }
}
