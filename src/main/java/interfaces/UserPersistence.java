package interfaces;

public interface UserPersistence {
    public boolean register();
    public String login(String username, String password);
}
