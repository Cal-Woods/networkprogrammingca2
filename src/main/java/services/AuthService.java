package services;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private final Map<String, String> credentials = new HashMap<>();

    public AuthService() {
        credentials.put("admin", "password123");
        credentials.put("user1", "pass1");
    }

    public boolean authenticate(String username, String password) {
        return password.equals(credentials.get(username));
    }
}