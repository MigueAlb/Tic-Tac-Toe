package espol.com.tresenraya.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class UserRepository {
    private final File file;
    private ArrayList<User> users;

    public UserRepository() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdir();
        }
        this.file = new File(folder, "users.dat");
        this.users = loadUsers();
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public User login(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }

    public User register(String email, String password, String name) {
        if (findByEmail(email) != null) {
            return null;
        }
        User user = new User(email, password, name);
        users.add(user);
        saveUsers();
        return user;
    }

    public void saveUsers() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(users);
        } catch (Exception exception) {
            System.out.println("No se pudieron guardar los usuarios: " + exception.getMessage());
        }
    }

    private ArrayList<User> loadUsers() {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            Object data = input.readObject();
            ArrayList<User> loadedUsers = new ArrayList<>();
            if (data instanceof ArrayList) {
                ArrayList<?> savedUsers = (ArrayList<?>) data;
                for (Object savedUser : savedUsers) {
                    if (savedUser instanceof User) {
                        loadedUsers.add((User) savedUser);
                    }
                }
            }
            return loadedUsers;
        } catch (Exception exception) {
            System.out.println("No se pudieron cargar los usuarios: " + exception.getMessage());
            return new ArrayList<>();
        }
    }
}
