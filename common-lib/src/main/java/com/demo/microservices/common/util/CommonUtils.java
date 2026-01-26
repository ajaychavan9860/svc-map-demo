package com.demo.microservices.common.util;

public class CommonUtils {
    
    public static String formatMessage(String message) {
        return "[COMMON] " + message;
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
