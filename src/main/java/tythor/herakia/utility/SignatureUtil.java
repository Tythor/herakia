package tythor.herakia.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignatureUtil {
    public static String getSignature(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index + 1];
        String className = stackTraceElement.getClassName();
        String methodName = extractMethodName(stackTraceElement);
        return className + "." + methodName;
    }

    public static String getSimpleSignature(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index + 1];
        String simpleClassName = getSimpleClassName(index + 1);
        String methodName = extractMethodName(stackTraceElement);
        return simpleClassName + "." + methodName;
    }

    public static String getClassName(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index + 1];
        return stackTraceElement.getClassName();
    }

    public static String getSimpleClassName(int index) {
        String className = getClassName(index + 1);
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public static String getMethodName(int index) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[index + 1];
        return extractMethodName(stackTraceElement);
    }

    // -------------------- HELPER METHODS --------------------

    public static String getSignature() {
        return getSignature(2);
    }

    public static String getSimpleSignature() {
        return getSimpleSignature(2);
    }

    public static String getClassName() {
        return getClassName(2);
    }

    public static String getSimpleClassName() {
        return getSimpleClassName(2);
    }

    public static String getMethodName() {
        return getMethodName(2);
    }

    private static String extractMethodName(StackTraceElement stackTraceElement) {
        String methodName = stackTraceElement.getMethodName();
        Pattern pattern = Pattern.compile("lambda\\$(.*?)\\$");
        Matcher matcher = pattern.matcher(methodName);
        return matcher.find() ? matcher.group(1) : methodName;
    }
}
