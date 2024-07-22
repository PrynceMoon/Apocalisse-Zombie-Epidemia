package apocalypse.game;

import apocalypse.db.DB_Manager;
import Apocalypse.type.Room;
import java.util.HashMap;
import java.util.Map;

/**
 * Rappresenta la mappa del gioco e gestisce la navigazione tra le stanze.
 */
public class GameMap {

    private Map<String, Room> rooms;
    private Room currentRoom;
    private DB_Manager dbManager;
    private int currentLanguageId;

    /**
     * Costruttore della mappa di gioco.
     *
     * @param dbManager Gestore del database per accedere alle informazioni
     * sulle stanze
     */
    public GameMap(DB_Manager dbManager) {
        this.dbManager = dbManager;
        this.currentLanguageId = dbManager.getLinguaCorrenteId();
        this.rooms = new HashMap<>();
        initializeRooms();
        initializeExits();

        String defaultRoomName = currentLanguageId == 1 ? "Dormitori" : "Dormitories";
        if (rooms.containsKey(defaultRoomName)) {
            currentRoom = rooms.get(defaultRoomName);
        } else {
            System.err.println("Errore: La stanza di default '" + defaultRoomName + "' non è stata trovata.");
        }
    }

    /**
     * Inizializza le stanze della mappa.
     */
    private void initializeRooms() {
        Map<String, String[]> roomTranslations = dbManager.getRoomTranslations(currentLanguageId);
        for (Map.Entry<String, String[]> entry : roomTranslations.entrySet()) {
            int roomId = Integer.parseInt(entry.getValue()[2]);
            boolean requiresKeycard = Boolean.parseBoolean(entry.getValue()[3]);
            Room room = new Room(entry.getValue()[0], entry.getValue()[1], roomId);
            room.setRequiresKeycard(requiresKeycard);
            rooms.put(entry.getKey(), room);
        }

        Room securityRoom = currentLanguageId == 1 ? rooms.get("Stanza della Sicurezza") : rooms.get("Security Room");
        if (securityRoom != null) {
            securityRoom.setSecurityPin("090706");
        }
    }

    /**
     * Inizializza le uscite delle stanze.
     */
    private void initializeExits() {
        Map<String, Map<String, String>> roomExits = dbManager.getRoomExits(currentLanguageId);
        for (Map.Entry<String, Map<String, String>> entry : roomExits.entrySet()) {
            String fromRoom = entry.getKey();
            Map<String, String> exits = entry.getValue();
            Room room = rooms.get(fromRoom);
            if (room != null) {
                for (Map.Entry<String, String> exitEntry : exits.entrySet()) {
                    String direction = exitEntry.getKey();
                    String toRoom = exitEntry.getValue();
                    room.setExit(direction, rooms.get(toRoom));
                }
            }
        }
    }

    /**
     * Restituisce la stanza corrente.
     *
     * @return La stanza corrente
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Verifica se la stanza corrente è l'uscita.
     *
     * @return true se la stanza corrente è l'uscita, false altrimenti
     */
    public boolean isExitRoom() {
        return currentRoom.getName().equalsIgnoreCase("Atrio") || currentRoom.getName().equalsIgnoreCase("Atrium");
    }

    /**
     * Tenta di muoversi verso l'uscita.
     *
     * @param hasKeys true se il giocatore ha le chiavi, false altrimenti
     * @return true se il movimento verso l'uscita è riuscito, false altrimenti
     */
    public boolean moveToExit(boolean hasKeys) {
        return isExitRoom() && hasKeys;
    }

    /**
     * Muove il giocatore in una direzione specificata.
     *
     * @param direction Direzione in cui muoversi
     * @param hasKeycard true se il giocatore ha la keycard, false altrimenti
     * @throws IllegalStateException se il movimento non è possibile
     */
    public void move(String direction, boolean hasKeycard) throws IllegalStateException {
        String translatedDirection = translateDirection(direction.toLowerCase());

        if (currentRoom == null) {
            throw new IllegalStateException(dbManager.getMessaggio("error_current_room_null", currentLanguageId));
        }

        Room nextRoom = currentRoom.getExit(translatedDirection);
        if (nextRoom != null) {
            if (nextRoom.isRequiresKeycard() && !hasKeycard) {
                throw new IllegalStateException(dbManager.getMessaggio("no_keycard", currentLanguageId));
            }
            if (nextRoom.hasSecurityPin()) {
                throw new IllegalStateException("PinRequired");
            } else {
                // Controllo specifico per l'uscita dall'edificio
                if (isExitRoom() && direction.equalsIgnoreCase("sud")) {
                    throw new IllegalStateException("CannotExitBuilding");
                }
                currentRoom = nextRoom;
            }
        } else {
            throw new IllegalStateException(dbManager.getMessaggio("no_exit", currentLanguageId));
        }
    }

    /**
     * Verifica il PIN di sicurezza per una stanza.
     *
     * @param inputPin PIN inserito dal giocatore
     * @return true se il PIN è corretto, false altrimenti
     */
    public boolean checkSecurityPin(String inputPin) {
        for (Room room : rooms.values()) {
            if (room.hasSecurityPin() && room.checkSecurityPin(inputPin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Entra in una stanza protetta da PIN.
     *
     * @param direction Direzione della stanza protetta da PIN
     */
    public void enterRoomWithPin(String direction) {
        String translatedDirection = translateDirection(direction.toLowerCase());
        Room nextRoom = currentRoom.getExit(translatedDirection);
        if (nextRoom != null && nextRoom.hasSecurityPin()) {
            currentRoom = nextRoom;
        }
    }

    /**
     * Traduce la direzione in base alla lingua corrente.
     *
     * @param direction Direzione da tradurre
     * @return Direzione tradotta
     */
    private String translateDirection(String direction) {
        if (currentLanguageId == 1) {
            switch (direction) {
                case "north":
                    return "nord";
                case "south":
                    return "sud";
                case "east":
                    return "est";
                case "west":
                    return "ovest";
                default:
                    return direction;
            }
        } else {
            switch (direction) {
                case "nord":
                    return "north";
                case "sud":
                    return "south";
                case "est":
                    return "east";
                case "ovest":
                    return "west";
                default:
                    return direction;
            }
        }
    }
}
