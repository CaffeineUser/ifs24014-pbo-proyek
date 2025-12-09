package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope // Opsional: atau gunakan ThreadLocal manual di bawah ini
public class AuthContext {
    
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public void setAuthUser(User user) {
        currentUser.set(user);
    }

    public User getAuthUser() {
        return currentUser.get();
    }

    public void clear() {
        currentUser.remove();
    }
    
    // Helper untuk cek role
    public boolean isAdmin() {
        User user = getAuthUser();
        return user != null && user.isAdmin();
    }
}