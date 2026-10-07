package modele;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StockageJson {
    private Path chemin;

    public StockageJson(Path chemin) {
        this.chemin = chemin;
    }

    public Path getChemin() {
        return chemin;
    }

    //creation d'un mapper pour la construction de la structure du json
    ObjectMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public void sauvegarder(List<Video> videotheque) throws JacksonException {
        try {
            //ajout d'un noeud propriétaire et version
            ObjectNode racine = mapper.createObjectNode();
            racine.put("Propriétaire", "CDA");
            racine.put("Version", "1.0");

            //ajout du noeud maison pour la liste des appareils
            ObjectNode home = mapper.createObjectNode();
            home.put("Nom", "Vidéotheque");
            ArrayNode tab = racine.putArray("videos");
            for (Video v : videotheque) {
                tab.add(mapper.valueToTree(v));
            }

            //Créer le dossier du fichier s’il n’existe pas (Files.createDirectories), puis écrire le tableau JSON
            Path jsonRepertory = chemin.getParent();
            if (jsonRepertory != null) {
                try {
                    Files.createDirectories(jsonRepertory);
                } catch (IOException e) {
                    System.out.println("Impossible de créer le répertoire : " + e.getMessage());
                }
            }
            //Écriture du fichier JSON dans le dossier jsonRepertory à la racine du projet
            mapper.writerWithDefaultPrettyPrinter().writeValue(chemin.toFile(), racine);
            System.out.println("Données sauvegardées dans : " + chemin.toAbsolutePath());

        } catch (JacksonException e) {
            System.err.println("Erreur JSON : " + e.getMessage());
        }
    }

    //Opération de désérialisation
    public List<Video> charger() throws IllegalArgumentException {
        ArrayList<Video> newListe = new ArrayList<>();

        //Retourner une liste vide si le fichier n’existe pas encore (premier lancement)
        File f = chemin.toFile();
        if (!f.exists()) {
            return newListe;
        }

        JsonNode lectureRacine = mapper.readTree(f);

        for (JsonNode n : lectureRacine.get("appareils")) {
            String type = n.get("type").asText();
            Video v;

            switch (type) {
                case "Dvd":
                    v = mapper.treeToValue(n, Dvd.class);
                    break;
                case "Video avi":
                    v = mapper.treeToValue(n, VideoAvi.class);
                    break;
                case "Video mp4":
                    v = mapper.treeToValue(n, VideoMp4.class);
                    break;
                default:
                    throw new IllegalArgumentException("Type de vidéo inconnu : " + type);
            }

            newListe.add(v);
        }
        return newListe;

    }



}
