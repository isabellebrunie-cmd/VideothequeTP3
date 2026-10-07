import application.Controller;
import exceptions.*;
import outils.Streamer;

import java.util.InputMismatchException;

public class Main {
    private static final Streamer streamer = new Streamer("rtsp://192.168.1.50:8554");

    public static void main(String[] args) {
        Controller c = new Controller();

        //Login par mot de passe déjà enregistré
        c.login();

        //Affichage du menu
        c.peuplerVideotheque("mp4");
        int choix = -1;

        while (true) {
            try {
                c.afficherMenu();
                System.out.print("Choix :");
                choix = Controller.scan.nextInt();
                Controller.scan.nextLine();
                switch (choix) {
                    case 1:
                        c.ajouterVideo();
                        break;
                    case 2:
                        c.listerVideos();
                        break;
                    case 3:
                        c.rechercherVideo();
                        break;
                    case 4:
                        c.supprimerVideo();
                        break;
                    case 5:
                        c.lectureVideo();
                        break;
                    case 6:
                        c.convertirVideo();
                        break;
                    case 7:
                        c.diffuserVideo();
                        break;
                    case 8:
                        //Diffuser la caméra du laptop
                        //break;
                    case 9:
                        //Arrêter la diffusion
                        //break;
                    case 0:
                        System.out.println("Au revoir !");
                        Controller.scan.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("\u001B[31mChoix invalide, veuillez réessayer.\u001B[0m");
                }
            } catch (ConversionImpossibleException | LectureImpossibleException | VideoDejaExistanteException |
                     SaisieInvalideException | VideoIntrouvableException | VideothequeVideException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException ime) {
                Controller.scan.nextLine();
                System.out.println("\u001B[31mSaisie non valide\u001B[0m");
            }
        }
    }

}
