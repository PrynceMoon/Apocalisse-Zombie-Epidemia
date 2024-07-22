package apocalypse.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import Apocalypse.type.gameObjectContainer;
import Apocalypse.type.GameObject;
import Apocalypse.type.Missione;

/**
 * Gestisce le operazioni di database per il gioco Apocalypse.
 */
public class DB_Manager {

    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:./database/dbApocalypse";
    private static final String USER = "Menny";
    private static final String PASS = "12345";

    private Connection conn = null;

    /**
     * Costruttore che inizializza la connessione al database.
     */
    public DB_Manager() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera la lista delle lingue disponibili nel gioco.
     *
     * @return Lista di stringhe rappresentanti le lingue disponibili.
     */
    public List<String> getLingueDisponibili() {
        List<String> lingue = new ArrayList<>();
        String query = "SELECT nome_lingua FROM lingue";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                lingue.add(rs.getString("nome_lingua"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lingue;
    }

    /**
     * Recupera l'ID della lingua correntemente impostata.
     *
     * @return ID della lingua corrente.
     */
    public int getLinguaCorrenteId() {
        int linguaCorrenteId = 1; // Default to Italiano
        String query = "SELECT id FROM lingue WHERE lingua_corrente = TRUE";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                linguaCorrenteId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return linguaCorrenteId;
    }

    /**
     * Recupera il nome della lingua dato il suo ID.
     *
     * @param id ID della lingua.
     * @return Nome della lingua.
     */
    public String getLinguaNameById(int id) {
        String linguaName = "italiano"; // Default language name
        String query = "SELECT nome_lingua FROM lingue WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                linguaName = rs.getString("nome_lingua");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return linguaName;
    }

    /**
     * Imposta la lingua corrente del gioco.
     *
     * @param lingua Nome della lingua da impostare.
     */
    public void setLinguaCorrente(String lingua) {
        String updateQuery = "UPDATE lingue SET lingua_corrente = CASE WHEN nome_lingua = ? THEN TRUE ELSE FALSE END";

        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, lingua);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera il testo di un bottone in una specifica lingua.
     *
     * @param nomeBottone Nome del bottone.
     * @param linguaId ID della lingua.
     * @return Testo del bottone nella lingua specificata.
     */
    public String getTestoBottone(String nomeBottone, int linguaId) {
        String testoBottone = nomeBottone; // Valore di default se non trovato
        String query = "SELECT testo FROM bottoni WHERE nome_bottone = ? AND lingua_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nomeBottone);
            pstmt.setInt(2, linguaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                testoBottone = rs.getString("testo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return testoBottone;
    }

    /**
     * Recupera la lista dei comandi disponibili in una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Lista di stringhe rappresentanti i comandi e le loro descrizioni.
     */
    public List<String> getComandi(int linguaId) {
        List<String> comandi = new ArrayList<>();
        String query = "SELECT nome_comando, descrizione FROM comandi WHERE lingua_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String comando = rs.getString("nome_comando") + ": " + rs.getString("descrizione");
                comandi.add(comando);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comandi;
    }

    /**
     * Recupera un messaggio specifico in una determinata lingua.
     *
     * @param chiaveMessaggio Chiave del messaggio.
     * @param linguaId ID della lingua.
     * @return Testo del messaggio nella lingua specificata.
     */
    public String getMessaggio(String chiaveMessaggio, int linguaId) {
        String testo = "Messaggio non trovato";
        String query = "SELECT testo FROM messaggi WHERE chiave_messaggio = ? AND lingua_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, chiaveMessaggio);
            pstmt.setInt(2, linguaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                testo = rs.getString("testo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return testo;
    }

    /**
     * Verifica se un comando esiste nel database.
     *
     * @param nomeComando Nome del comando da verificare.
     * @return true se il comando esiste, false altrimenti.
     */
    public boolean comandoEsiste(String nomeComando) {
        String query = "SELECT COUNT(*) FROM comandi WHERE nome_comando = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nomeComando);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Recupera la descrizione di una stanza in una specifica lingua.
     *
     * @param roomName Nome della stanza.
     * @param linguaId ID della lingua.
     * @return Descrizione della stanza nella lingua specificata.
     */
    public String getRoomDescription(String roomName, int linguaId) {
        String roomDescription = "Descrizione non trovata";
        String query = "SELECT DESCRIZIONE_STANZA FROM stanze WHERE NOME_STANZA = ? AND LINGUA_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomName);
            pstmt.setInt(2, linguaId);
            //System.out.println("Executing query with roomName: " + roomName + ", linguaId: " + linguaId); // Debug statement
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                roomDescription = rs.getString("DESCRIZIONE_STANZA");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roomDescription;
    }

    /**
     * Recupera le traduzioni delle stanze per una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Mappa contenente le informazioni delle stanze tradotte.
     */
    public Map<String, String[]> getRoomTranslations(int linguaId) {
        Map<String, String[]> rooms = new HashMap<>();
        String query = "SELECT ID_STANZA, NOME_STANZA, DESCRIZIONE_STANZA, richiede_tesserino FROM stanze WHERE LINGUA_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.put(rs.getString("NOME_STANZA"), new String[]{
                    rs.getString("NOME_STANZA"),
                    rs.getString("DESCRIZIONE_STANZA"),
                    String.valueOf(rs.getInt("ID_STANZA")),
                    String.valueOf(rs.getBoolean("richiede_tesserino"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /**
     * Recupera le uscite delle stanze per una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Mappa contenente le informazioni sulle uscite delle stanze.
     */
    public Map<String, Map<String, String>> getRoomExits(int linguaId) {
        Map<String, Map<String, String>> exits = new HashMap<>();
        String query = "SELECT s1.NOME_STANZA as from_room, s2.NOME_STANZA as to_room, DIREZIONE "
                + "FROM esci "
                + "JOIN stanze s1 ON esci.STANZA_FROM_ID = s1.ID_STANZA "
                + "JOIN stanze s2 ON esci.STANZA_TO_ID = s2.ID_STANZA "
                + "WHERE esci.LINGUA_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String fromRoom = rs.getString("from_room");
                String toRoom = rs.getString("to_room");
                String direction = rs.getString("DIREZIONE");

                exits.putIfAbsent(fromRoom, new HashMap<>());
                exits.get(fromRoom).put(direction, toRoom);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exits;
    }

    /**
     * Recupera i contenitori di oggetti per una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Mappa contenente i contenitori di oggetti.
     */
    public Map<Integer, gameObjectContainer> getContenitori(int linguaId) {
        Map<Integer, gameObjectContainer> contenitori = new HashMap<>();
        String query = "SELECT ID_CONTENITORE, NOME_CONTENITORE, ID_STANZA FROM contenitori WHERE LINGUA_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int idContenitore = rs.getInt("ID_CONTENITORE");
                String nomeContenitore = rs.getString("NOME_CONTENITORE");
                Integer idStanza = rs.getInt("ID_STANZA");
                if (rs.wasNull()) {
                    idStanza = null;
                }
                gameObjectContainer container = new gameObjectContainer(idContenitore, nomeContenitore, linguaId, idStanza, false, false, null);
                contenitori.put(idContenitore, container);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contenitori;
    }

    /**
     * Recupera gli oggetti del gioco per una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Lista di oggetti del gioco.
     */
    public List<GameObject> getOggetti(int linguaId) {
        List<GameObject> oggetti = new ArrayList<>();
        String query = "SELECT ID_OGGETTO, NOME_OGGETTO, LINGUA_ID, ID_STANZA, PRENDIBILE, CREABILE, ID_CONTENITORE, ESAMINAZIONE FROM OGGETTI WHERE LINGUA_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int idOggetto = rs.getInt("ID_OGGETTO");
                String nomeOggetto = rs.getString("NOME_OGGETTO");
                Integer idStanza = rs.getInt("ID_STANZA");
                if (rs.wasNull()) {
                    idStanza = null;
                }
                boolean prendibile = rs.getBoolean("PRENDIBILE");
                boolean creabile = rs.getBoolean("CREABILE");
                Integer idContenitore = rs.getInt("ID_CONTENITORE");
                if (rs.wasNull()) {
                    idContenitore = null;
                }
                String esaminazione = rs.getString("ESAMINAZIONE");
                GameObject obj = new GameObject(idOggetto, nomeOggetto, linguaId, idStanza, prendibile, creabile, idContenitore);
                obj.setDescription(esaminazione);
                oggetti.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oggetti;
    }

    /**
     * Recupera un dialogo specifico in una determinata lingua.
     *
     * @param chiaveMessaggio Chiave del dialogo.
     * @param linguaId ID della lingua.
     * @return Testo del dialogo nella lingua specificata.
     */
    public String getDialogo(String chiaveMessaggio, int linguaId) {
        String testo = "Dialogo non trovato";
        String query = "SELECT dialogo FROM dialoghi WHERE chiave_messaggio = ? AND lingua_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, chiaveMessaggio);
            pstmt.setInt(2, linguaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                testo = rs.getString("dialogo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return testo;
    }

    /**
     * Recupera le missioni del gioco per una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Lista di missioni del gioco.
     */
    public List<Missione> getMissioni(int linguaId) {
        List<Missione> missioni = new ArrayList<>();
        String query = "SELECT ID, NOME, DESCRIZIONE, CONDIZIONE_COMPLETAMENTO, COMPLETATA FROM missioni WHERE LINGUA_ID = ? ORDER BY ID";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Missione missione = new Missione(
                        rs.getInt("ID"),
                        rs.getString("NOME"),
                        rs.getString("DESCRIZIONE"),
                        rs.getString("CONDIZIONE_COMPLETAMENTO"),
                        rs.getBoolean("COMPLETATA")
                );
                missioni.add(missione);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missioni;
    }

    /**
     * Aggiorna lo stato di completamento di una missione.
     *
     * @param missioneId ID della missione.
     * @param completata Stato di completamento della missione.
     */
    public void aggiornaMissioneCompletata(int missioneId, boolean completata) {
        String query = "UPDATE missioni SET COMPLETATA = ? WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, completata);
            pstmt.setInt(2, missioneId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resetta tutte le missioni per una specifica lingua.
     *
     * @param linguaId ID della lingua.
     */
    public void resetMissioni(int linguaId) {
        String query = "UPDATE missioni SET COMPLETATA = FALSE WHERE LINGUA_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, linguaId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera la trama del gioco in una specifica lingua.
     *
     * @param linguaId ID della lingua.
     * @return Testo della trama nella lingua specificata.
     */
    public String getTrama(int linguaId) {
        String trama = "";
        try {
            String query = "SELECT DIALOGO FROM dialoghi WHERE CHIAVE_MESSAGGIO = 'trama' AND LINGUA_ID = ?";
            System.out.println("Executing query: " + query + " with LINGUA_ID = " + linguaId);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, linguaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                trama = rs.getString("DIALOGO");
                System.out.println("Trama found: " + trama);
            } else {
                System.out.println("No trama found for LINGUA_ID: " + linguaId);
            }
        } catch (SQLException e) {
            System.out.println("Error in getTrama: " + e.getMessage());
            e.printStackTrace();
        }
        return trama;
    }

    /**
     * Chiude la connessione al database.
     */
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
