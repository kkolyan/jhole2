package net.kkolyan.jhole2;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Credentials {
    private String username;
    private String password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
