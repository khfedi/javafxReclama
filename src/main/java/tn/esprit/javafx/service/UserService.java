package tn.esprit.javafx.service;

import tn.esprit.javafx.database.UserDAO;
import tn.esprit.javafx.model.User;

import java.util.List;

public class UserService {
    
    private static final int STATIC_USER_ID = 1; // Static user ID as per requirements
    
    public User getCurrentUser() {
        // Using static user ID = 1 as per requirements
        return UserDAO.getUserById(STATIC_USER_ID);
    }
    
    public User getUserById(int id) {
        return UserDAO.getUserById(id);
    }
    
    public User getUserByEmail(String email) {
        return UserDAO.getUserByEmail(email);
    }
    
    public List<User> getAllUsers() {
        return UserDAO.getAllUsers();
    }
    
    public boolean addUser(String username, String email, String password) {
        User user = new User(0, username, email, password);
        return UserDAO.addUser(user);
    }
    
    public boolean updateUser(int id, String username, String email, String password) {
        User user = UserDAO.getUserById(id);
        
        if (user != null) {
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            
            return UserDAO.updateUser(user);
        }
        
        return false;
    }
    
    public boolean deleteUser(int id) {
        return UserDAO.deleteUser(id);
    }
}
