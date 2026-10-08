package outils;

import exceptions.SaisieInvalideException;
import exceptions.StreamingException;
import modele.FichierVideo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Streamer {
    private final String urlServeur;     // ex. "rtsp://192.168.1.50:8554"
    private volatile Process processus;  // ffmpeg en cours (partagé entre threads)
    private String fluxEnCours;          // nom du chemin diffusé, ex. "film"

    public Streamer(String urlServeur) {
        this.urlServeur = urlServeur;
    }

    /**
     * ffmpeg -re [-stream_loop -1] -i fichier <options du format> <sortie>
     */
    public void diffuserFichier(FichierVideo video, String nomFlux, boolean boucle)
            throws StreamingException, SaisieInvalideException {
        List<String> commande = new ArrayList<>();
        commande.add("ffmpeg.exe");
        commande.add("-re");

        if (boucle) {                 //dans le cas d'une diffusion en boucle
            commande.add("-stream_loop");
            commande.add("-1");
        }
        commande.add("-i");
        commande.add(video.getChemin());
        commande.addAll(video.getOptionsStreaming());
        commande.add("-f");
        commande.add("flv");
        commande.add(urlServeur + "/" + nomFlux);
        //commande.add(urlServeur);
        //System.out.println(commande);

        try {
            ProcessBuilder pb = new ProcessBuilder(commande);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);

            this.processus = pb.start();
            this.fluxEnCours = nomFlux;

            this.processus.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new StreamingException("Erreur lors de la diffusion.");
        } finally {
            this.processus = null;
        }
    }

    /**
     * ffmpeg <entrée caméra selon le système> <encodage direct> <sortie>
     */
    public void diffuserCamera(String nomFlux)
            throws StreamingException, SaisieInvalideException {
        /* TODO */
        List<String> commande = new ArrayList<>();
        commande.add("ffmpeg");

        //Détection du système de la machine distante (Windows/Linux/Mac)
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            commande.add("-f");
            commande.add("dshow");
            commande.add("-rtbufsize");
            commande.add("100M");
            commande.add("-video_size");
            commande.add("1280x720");
            commande.add("-framerate");
            commande.add("30");
            commande.add("-i");
            //Check le nom des périphériques video et audi de la machine distante
            commande.add("video=\"Integrated Camera\":audio=\"Microphone (Realtek(R) Audio)\"");
            commande.add("-c:v");
            commande.add("libx264");
            commande.add("-preset");
            commande.add("ultrafast");
            commande.add("-tune");
            commande.add("zerolatency");
            commande.add("-pix_fmt");
            commande.add("yuv420p");
            commande.add("-g");
            commande.add("30");

        } else if (os.indexOf("mac") >= 0) {

        } else if (os.indexOf("nux") >= 0) {

        }

    }

    /**
     * Arrête proprement ffmpeg : envoie "q" sur son entrée standard,
     * attend 5 s au maximum, sinon destroy().
     */
    public void arreter() {
        // Si un flux est en cours
        if (!estEnCours()) {
            return;
        }
        try {
            processus.getOutputStream().write("q\n".getBytes());
            processus.getOutputStream().flush();
            // Attendre 5s sinon détruire
            if (!processus.waitFor(5, TimeUnit.SECONDS)) {
                processus.destroy();
            }
        } catch (IOException e) {
            processus.destroy();
        } catch (InterruptedException e) {
            processus.destroy();
            Thread.currentThread().interrupt();
        }
    }

    public boolean estEnCours() {
        return processus != null && processus.isAlive();
    }

    /**
     * URL à donner aux spectateurs, ex. rtsp://.../film
     */
    public String getUrlServeur() {
        return urlServeur;
    }

    /**
     * -f rtsp -rtsp_transport tcp rtsp://serveur:8554/nomFlux
     */
    private List<String> optionsSortie(String nomFlux) { /* TODO */

        return List.of();
    }

    /**
     * Entrée caméra : dshow, v4l2 ou avfoundation selon os.name
     */
    private List<String> optionsCamera() { /* TODO */
        return List.of();
    }

    /**
     * Lance ffmpeg et lit sa sortie dans un thread daemon.
     */
    private void lancer(List<String> commande, String nomFlux)
            throws StreamingException { /* TODO */
        ProcessBuilder constructeur = new ProcessBuilder(commande);

        constructeur.redirectErrorStream(true);

        Process lancement;
        try {
            lancement = constructeur.start();
        } catch (IOException e) {
            throw new StreamingException("Impossible de lancer ffmpeg.");
        }
        processus = lancement;
        fluxEnCours = nomFlux;

        Thread lecteurSortie = new Thread(() -> {
            try (BufferedReader sortie = new BufferedReader(new InputStreamReader(lancement.getInputStream()))) {
                while (sortie.readLine() != null) {
                    //
                }
            } catch (IOException e) {
                //
            }
        });
        lecteurSortie.setName("Streamer-" + nomFlux);
        lecteurSortie.setDaemon(true);
        lecteurSortie.start();

        try {
            if (lancement.waitFor(3, TimeUnit.SECONDS)) {
                throw new StreamingException("ffmpeg s'est arrêté (code " + lancement.exitValue()
                        + ") : serveur injoignable");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        }
}