package Apocalypse.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un contenitore di oggetti di gioco.
 * Estende la classe GameObject.
 */
public class gameObjectContainer extends GameObject {
    
    /** Lista che contiene gli oggetti nel contenitore */
    private final List<GameObject> containerList = new ArrayList<>();

    /**
     * Costruttore completo per la classe gameObjectContainer.
     * 
     * @param idOggetto     ID univoco dell'oggetto
     * @param name          Nome dell'oggetto
     * @param linguaId      ID della lingua
     * @param idStanza      ID della stanza in cui si trova l'oggetto
     * @param prendibile    Indica se l'oggetto può essere preso
     * @param creabile      Indica se l'oggetto può essere creato
     * @param idContenitore ID del contenitore in cui si trova l'oggetto
     */
    public gameObjectContainer(int idOggetto, String name, int linguaId, Integer idStanza, boolean prendibile, boolean creabile, Integer idContenitore) {
        super(idOggetto, name, linguaId, idStanza, prendibile, creabile, idContenitore);
    }

    /**
     * Restituisce la lista degli oggetti nel contenitore.
     * 
     * @return Lista degli oggetti nel contenitore
     */
    public List<GameObject> getContainerList() {
        return containerList;
    }

    /**
     * Aggiunge tutti gli oggetti della lista fornita al contenitore.
     * 
     * @param ls Lista di oggetti da aggiungere al contenitore
     */
    public void addAllGameObjList(List<GameObject> ls) {
        containerList.addAll(ls);
    }

    /**
     * Rimuove un oggetto specifico dal contenitore.
     * 
     * @param gameObj Oggetto da rimuovere dal contenitore
     */
    public void removeContList(GameObject gameObj) {
        containerList.remove(gameObj);
    }

    /**
     * Aggiunge un singolo oggetto al contenitore.
     * 
     * @param gameObj Oggetto da aggiungere al contenitore
     */
    public void addGameObj(GameObject gameObj) {
        containerList.add(gameObj);
    }

    /**
     * Restituisce il numero di oggetti nel contenitore.
     * 
     * @return Numero di oggetti nel contenitore
     */
    public int getContainerSize() {
        return containerList.size();
    }

    /**
     * Verifica se il contenitore è vuoto.
     * 
     * @return true se il contenitore è vuoto, false altrimenti
     */
    public boolean isContainerEmpty() {
        return containerList.isEmpty();
    }

    /**
     * Rimuove tutti gli oggetti dal contenitore.
     */
    public void clearContainer() {
        containerList.clear();
    }
}