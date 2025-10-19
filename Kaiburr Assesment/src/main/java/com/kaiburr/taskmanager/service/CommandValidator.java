package com.kaiburr.taskmanager.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CommandValidator {

    private static final Pattern ECHO_PATTERN = Pattern.compile("^\n?\s*(?i:echo)\s+.{1,900}$");

    public boolean isSafe(String command) {
        if (command == null) return false;
        String trimmed = command.trim();
        if (trimmed.isEmpty()) return false;
        // Allow only simple echo commands to mitigate RCE risks
        return ECHO_PATTERN.matcher(trimmed).matches();
    }
}


