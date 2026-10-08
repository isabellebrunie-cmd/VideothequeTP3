package video;

import exceptions.StreamingException;
import modele.FichierVideo;
import outils.Streamer;

public class StreamVideo implements Runnable {
    private final FichierVideo video;
    private final String nomFlux;
    private volatile boolean diffContinue;
    private volatile boolean arretDemande;
    private Thread thread; // gère l'exécution en arrière-plan
    private final Streamer streamer; // référence vers l'objet qui gère le processus ffmpeg

    public StreamVideo(FichierVideo video, String nomFlux, boolean diffContinue, Streamer streamer) {
        this.video = video;
        this.nomFlux = nomFlux;
        this.diffContinue = diffContinue;
        this.streamer = streamer;
    }

    /**
     * Crée le thread (daemon) et le démarre.
     * Ne fait rien si une lecture est déjà en cours.
     * 1 seule lecture à la fois
     */
    public void demarrer() {
        if (thread != null && thread.isAlive()) {
            return;
        }
        arretDemande = false;
        thread = new Thread(this);
        thread.setName("Lecteur-" + video.getTitre());
        thread.setDaemon(true);
        thread.start();
        System.out.println(Thread.currentThread().getName() + " lancement du thread");
    }

    @Override
    public void run() {
        try {
            streamer.diffuserFichier(video, nomFlux, diffContinue);
        } catch (StreamingException e) {
            if (!arretDemande) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            if (!arretDemande) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Arrête la lecture depuis un autre thread : arrêt de la video.
     */
    public void arreter() {
        if (thread != null && thread.isAlive()) {
            arretDemande = true;
            streamer.arreter();
        } else {
            System.out.println("Il n'y a pas de lecture en cours.");
        }
    }

    public boolean estEnCours() {
        //System.out.println("En cours --> " + Thread.currentThread().getName());
        return streamer.estEnCours();
    }
}
