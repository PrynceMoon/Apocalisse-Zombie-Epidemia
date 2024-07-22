package Apocalypse.type;

/**
 * Rappresenta una missione nel gioco.
 */
public class Missione {
    private int id;
    private String nome;
    private String descrizione;
    private String condizioneCompletamento;
    private boolean completata;
    private boolean messaggioMostrato;

    /**
     * Costruttore per creare una nuova missione.
     *
     * @param id                      L'ID univoco della missione
     * @param nome                    Il nome della missione
     * @param descrizione             La descrizione della missione
     * @param condizioneCompletamento La condizione per completare la missione
     * @param completata              Indica se la missione è stata completata
     */
    public Missione(int id, String nome, String descrizione, String condizioneCompletamento, boolean completata) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.condizioneCompletamento = condizioneCompletamento;
        this.completata = completata;
    }

    /**
     * Ottiene l'ID della missione.
     *
     * @return L'ID della missione
     */
    public int getId() {
        return id;
    }

    /**
     * Ottiene il nome della missione.
     *
     * @return Il nome della missione
     */
    public String getNome() {
        return nome;
    }

    /**
     * Ottiene la descrizione della missione.
     *
     * @return La descrizione della missione
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Ottiene la condizione di completamento della missione.
     *
     * @return La condizione di completamento della missione
     */
    public String getCondizioneCompletamento() {
        return condizioneCompletamento;
    }

    /**
     * Verifica se la missione è stata completata.
     *
     * @return true se la missione è completata, false altrimenti
     */
    public boolean isCompletata() {
        return completata;
    }

    /**
     * Imposta lo stato di completamento della missione.
     *
     * @param completata true se la missione è completata, false altrimenti
     */
    public void setCompletata(boolean completata) {
        this.completata = completata;
    }

    /**
     * Verifica se il messaggio di completamento della missione è stato mostrato.
     *
     * @return true se il messaggio è stato mostrato, false altrimenti
     */
    public boolean isMessaggioMostrato() {
        return messaggioMostrato;
    }

    /**
     * Imposta se il messaggio di completamento della missione è stato mostrato.
     *
     * @param messaggioMostrato true se il messaggio è stato mostrato, false altrimenti
     */
    public void setMessaggioMostrato(boolean messaggioMostrato) {
        this.messaggioMostrato = messaggioMostrato;
    }
}