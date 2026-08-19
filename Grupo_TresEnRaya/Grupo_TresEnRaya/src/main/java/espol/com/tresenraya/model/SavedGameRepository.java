package espol.com.tresenraya.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persistencia local de partidas a medio jugar, separada de users.dat. */
public final class SavedGameRepository {
    private final File file;
    private final Map<String, List<SavedGame>> gamesByUser;

    public SavedGameRepository() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.file = new File(folder, "saved_games.dat");
        this.gamesByUser = load();
    }

    public void save(SavedGame game) {
        List<SavedGame> games = gamesByUser.computeIfAbsent(
                game.getOwnerEmail().toLowerCase(), key -> new ArrayList<>());

        games.removeIf(existing -> existing.getId().equals(game.getId()));
        games.add(game);
        games.sort(Comparator.comparing(SavedGame::getSavedAt).reversed());
        persist();
    }

    public List<SavedGame> findByUser(String email) {
        return new ArrayList<>(gamesByUser.getOrDefault(
                email.toLowerCase(), List.of()));
    }

    public void delete(String email, String id) {
        List<SavedGame> games = gamesByUser.get(email.toLowerCase());
        if (games == null) {
            return;
        }
        games.removeIf(game -> game.getId().equals(id));
        if (games.isEmpty()) {
            gamesByUser.remove(email.toLowerCase());
        }
        persist();
    }

    private void persist() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(gamesByUser);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No se pudieron guardar las partidas: " + exception.getMessage(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<SavedGame>> load() {
        if (!file.exists()) {
            return new LinkedHashMap<>();
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            Object object = input.readObject();
            if (object instanceof Map<?, ?> map) {
                Map<String, List<SavedGame>> result = new LinkedHashMap<>();
                map.forEach((key, value) -> {
                    if (key instanceof String && value instanceof List<?>) {
                        List<SavedGame> valid = new ArrayList<>();
                        for (Object item : (List<?>) value) {
                            if (item instanceof SavedGame savedGame) {
                                valid.add(savedGame);
                            }
                        }
                        result.put((String) key, valid);
                    }
                });
                return result;
            }
        } catch (Exception exception) {
            System.out.println("No se pudieron cargar las partidas: " + exception.getMessage());
        }
        return new LinkedHashMap<>();
    }
}

