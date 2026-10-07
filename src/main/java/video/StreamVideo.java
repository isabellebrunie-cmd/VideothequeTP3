package video;

import exceptions.StreamingException;
import modele.FichierVideo;
import outils.Streamer;

public class StreamVideo implements Runnable {
    private final FichierVideo video;
    private final String nomFlux;
    private volatile boolean diffContinue;
    private volatile boolean arretDemande;
    private Thread thread;


    public StreamVideo(FichierVideo video, String nomFlux, boolean diffContinue) {
        this.video = video;
        this.nomFlux = nomFlux;
        this.diffContinue = diffContinue;
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
            Streamer.diffuserFichier(video, nomFlux, diffContinue);
        } catch (StreamingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Arrête la lecture depuis un autre thread : arrêt de la video.
     */
    public void arreter() {
        //TODO
    }

    public boolean estEnCours() {
        //System.out.println("En cours --> " + Thread.currentThread().getName());
        return thread != null && thread.isAlive();
    }
}
