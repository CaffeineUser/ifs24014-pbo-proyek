package org.delcom.app.entities;

public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER", "Customer"),
    STAFF("ROLE_STAFF", "Staff"),
    ADMIN("ROLE_ADMIN", "Administrator");
    
    private final String authority;
    private final String displayName;
    
    UserRole(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }
    
    public String getAuthority() {
        return authority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    // Helper method to get enum from string
    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(text) || 
                role.getAuthority().equalsIgnoreCase(text)) {
                return role;
            }
        }
        return CUSTOMER; // default
    }
}