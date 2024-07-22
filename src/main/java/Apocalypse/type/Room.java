package Apocalypse.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rappresenta una stanza nel gioco.
 */
public class Room {
    private String name;
    private String description;
    private int id;
    private boolean requiresKeycard;
    private String securityPin;
    private Map<String, Room> exits;
    private List<GameObject> objects;

    /**
     * Costruttore per creare una nuova stanza.
     *
     * @param name        Il nome della stanza
     * @param description La descrizione della stanza
     * @param id          L'ID univoco della stanza
     */
    public Room(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.requiresKeycard = false;
        this.exits = new HashMap<>();
        this.objects = new ArrayList<>();
    }

    /**
     * Ottiene il nome della stanza.
     *
     * @return Il nome della stanza
     */
    public String getName() {
        return name;
    }

    /**
     * Ottiene la descrizione della stanza.
     *
     * @return La descrizione della stanza
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ottiene l'ID della stanza.
     *
     * @return L'ID della stanza
     */
    public int getId() {
        return id;
    }

    /**
     * Verifica se la stanza richiede una keycard per l'accesso.
     *
     * @return true se la stanza richiede una keycard, false altrimenti
     */
    public boolean isRequiresKeycard() {
        return requiresKeycard;
    }

    /**
     * Imposta se la stanza richiede una keycard per l'accesso.
     *
     * @param requiresKeycard true se la stanza richiede una keycard, false altrimenti
     */
    public void setRequiresKeycard(boolean requiresKeycard) {
        this.requiresKeycard = requiresKeycard;
    }

    /**
     * Imposta il PIN di sicurezza per la stanza.
     *
     * @param securityPin Il PIN di sicurezza da impostare
     */
    public void setSecurityPin(String securityPin) {
        this.securityPin = securityPin;
    }

    /**
     * Verifica se la stanza ha un PIN di sicurezza.
     *
     * @return true se la stanza ha un PIN di sicurezza, false altrimenti
     */
    public boolean hasSecurityPin() {
        return securityPin != null;
    }

    /**
     * Verifica se il PIN di sicurezza inserito è corretto.
     *
     * @param inputPin Il PIN inserito da verificare
     * @return true se il PIN è corretto, false altrimenti
     */
    public boolean checkSecurityPin(String inputPin) {
        return securityPin != null && securityPin.equals(inputPin);
    }

    /**
     * Imposta un'uscita dalla stanza.
     *
     * @param direction La direzione dell'uscita
     * @param room      La stanza collegata all'uscita
     */
    public void setExit(String direction, Room room) {
        exits.put(direction, room);
    }

    /**
     * Ottiene la stanza collegata a una specifica direzione.
     *
     * @param direction La direzione dell'uscita
     * @return La stanza collegata all'uscita nella direzione specificata, o null se non esiste
     */
    public Room getExit(String direction) {
        return exits.get(direction);
    }

    /**
     * Aggiunge un oggetto alla stanza.
     *
     * @param obj L'oggetto da aggiungere alla stanza
     */
    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    /**
     * Ottiene la lista degli oggetti presenti nella stanza.
     *
     * @return La lista degli oggetti nella stanza
     */
    public List<GameObject> getObjects() {
        return objects;
    }
}