package Apocalypse.type;

/**
 * Rappresenta un oggetto generico nel gioco.
 */
public class GameObject {
    private int idOggetto;
    private String name;
    private String description;
    private int linguaId;
    private Integer idStanza;
    private boolean prendibile;
    private boolean creabile;
    private Integer idContenitore;

    /**
     * Costruttore per creare un nuovo oggetto di gioco.
     *
     * @param idOggetto     ID univoco dell'oggetto
     * @param name          Nome dell'oggetto
     * @param linguaId      ID della lingua dell'oggetto
     * @param idStanza      ID della stanza in cui si trova l'oggetto (può essere null)
     * @param prendibile    Indica se l'oggetto può essere preso dal giocatore
     * @param creabile      Indica se l'oggetto può essere creato nel gioco
     * @param idContenitore ID del contenitore in cui si trova l'oggetto (può essere null)
     */
    public GameObject(int idOggetto, String name, int linguaId, Integer idStanza, boolean prendibile, boolean creabile, Integer idContenitore) {
        this.idOggetto = idOggetto;
        this.name = name;
        this.linguaId = linguaId;
        this.idStanza = idStanza;
        this.prendibile = prendibile;
        this.creabile = creabile;
        this.idContenitore = idContenitore;
    }

    /**
     * Ottiene l'ID dell'oggetto.
     * @return L'ID dell'oggetto
     */
    public int getIdOggetto() {
        return idOggetto;
    }

    /**
     * Imposta l'ID dell'oggetto.
     * @param idOggetto Il nuovo ID dell'oggetto
     */
    public void setIdOggetto(int idOggetto) {
        this.idOggetto = idOggetto;
    }

    /**
     * Ottiene l'ID della lingua dell'oggetto.
     * @return L'ID della lingua
     */
    public int getLinguaId() {
        return linguaId;
    }

    /**
     * Imposta l'ID della lingua dell'oggetto.
     * @param linguaId Il nuovo ID della lingua
     */
    public void setLinguaId(int linguaId) {
        this.linguaId = linguaId;
    }

    /**
     * Ottiene l'ID della stanza in cui si trova l'oggetto.
     * @return L'ID della stanza, o null se l'oggetto non è in una stanza
     */
    public Integer getIdStanza() {
        return idStanza;
    }

    /**
     * Imposta l'ID della stanza in cui si trova l'oggetto.
     * @param idStanza Il nuovo ID della stanza, o null se l'oggetto non è in una stanza
     */
    public void setIdStanza(Integer idStanza) {
        this.idStanza = idStanza;
    }

    /**
     * Ottiene il nome dell'oggetto.
     * @return Il nome dell'oggetto
     */
    public String getName() {
        return name;
    }

    /**
     * Imposta il nome dell'oggetto.
     * @param name Il nuovo nome dell'oggetto
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Ottiene la descrizione dell'oggetto.
     * @return La descrizione dell'oggetto
     */
    public String getDescription() {
        return description;
    }

    /**
     * Imposta la descrizione dell'oggetto.
     * @param description La nuova descrizione dell'oggetto
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Verifica se l'oggetto può essere preso dal giocatore.
     * @return true se l'oggetto è prendibile, false altrimenti
     */
    public boolean isPrendibile() {
        return prendibile;
    }

    /**
     * Imposta se l'oggetto può essere preso dal giocatore.
     * @param prendibile true se l'oggetto deve essere prendibile, false altrimenti
     */
    public void setPrendibile(boolean prendibile) {
        this.prendibile = prendibile;
    }

    /**
     * Verifica se l'oggetto può essere creato nel gioco.
     * @return true se l'oggetto è creabile, false altrimenti
     */
    public boolean isCreabile() {
        return creabile;
    }

    /**
     * Imposta se l'oggetto può essere creato nel gioco.
     * @param creabile true se l'oggetto deve essere creabile, false altrimenti
     */
    public void setCreabile(boolean creabile) {
        this.creabile = creabile;
    }

    /**
     * Ottiene l'ID del contenitore in cui si trova l'oggetto.
     * @return L'ID del contenitore, o null se l'oggetto non è in un contenitore
     */
    public Integer getIdContenitore() {
        return idContenitore;
    }

    /**
     * Imposta l'ID del contenitore in cui si trova l'oggetto.
     * @param idContenitore Il nuovo ID del contenitore, o null se l'oggetto non è in un contenitore
     */
    public void setIdContenitore(Integer idContenitore) {
        this.idContenitore = idContenitore;
    }
}