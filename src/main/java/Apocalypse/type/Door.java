package Apocalypse.type;

/**
 * Rappresenta una porta nel gioco.
 */
public class Door {
    private String name;
    private int Previous_Room;
    private int Next_Room;
    private boolean isLocked;
    private String Description;

    /**
     * Costruttore di default per la classe Door.
     */
    public Door() {}

    /**
     * Imposta il nome della porta.
     * @param name Il nome da assegnare alla porta
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Imposta l'ID della stanza precedente collegata alla porta.
     * @param previous_Room L'ID della stanza precedente
     */
    public void setPrevious_Room(int previous_Room) {
        Previous_Room = previous_Room;
    }

    /**
     * Ottiene l'ID della stanza successiva collegata alla porta.
     * @return L'ID della stanza successiva
     */
    public int getNext_Room() {
        return Next_Room;
    }

    /**
     * Imposta l'ID della stanza successiva collegata alla porta.
     * @param next_Room L'ID della stanza successiva
     */
    public void setNext_Room(int next_Room) {
        Next_Room = next_Room;
    }

    /**
     * Ottiene la descrizione della porta.
     * @return La descrizione della porta
     */
    public String getDescription() {
        return Description;
    }

    /**
     * Imposta la descrizione della porta.
     * @param description La descrizione da assegnare alla porta
     */
    public void setDescription(String description) {
        Description = description;
    }

    /**
     * Verifica se la porta è bloccata.
     * @return true se la porta è bloccata, false altrimenti
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Imposta lo stato di blocco della porta.
     * @param locked true per bloccare la porta, false per sbloccarla
     */
    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}