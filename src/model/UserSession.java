package model;

/**
 * Utility class to hold the session state of the currently logged-in user.
 * We can keep track of user ID, role, etc.
 */
public class UserSession {
    // Current user's identifier (admin_id, faculty_id, or roll_no)
    private static String loggedInUserId;
    // Current user's name
    private static String loggedInUserName;
    // Role of current user: "ADMIN", "FACULTY", "STUDENT"
    private static String role;

    public static String getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(String loggedInUserId) {
        UserSession.loggedInUserId = loggedInUserId;
    }

    public static String getLoggedInUserName() {
        return loggedInUserName;
    }

    public static void setLoggedInUserName(String loggedInUserName) {
        UserSession.loggedInUserName = loggedInUserName;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        UserSession.role = role;
    }
    
    public static void clear() {
        loggedInUserId = null;
        loggedInUserName = null;
        role = null;
    }
}
