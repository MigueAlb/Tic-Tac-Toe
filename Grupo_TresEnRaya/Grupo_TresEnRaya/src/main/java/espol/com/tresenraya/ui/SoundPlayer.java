package espol.com.tresenraya.ui;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundPlayer {

    private static boolean enabled = true;

    // Sonido de seleccion
    public static void playClick() {
        play("/sounds/click.wav");
    }

    // Sonido de confirmacion
    public static void playSuccess() {
        play("/sounds/success.wav");
    }

    // Sonido de error
    public static void playError() {
        play("/sounds/error.wav");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    private static void play(String path) {
        if (!enabled) {
            return;
        }

        try {
            URL soundFile = SoundPlayer.class.getResource(path);
            AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception exception) {
            System.out.println("No se pudo reproducir el sonido");
        }
    }
}
