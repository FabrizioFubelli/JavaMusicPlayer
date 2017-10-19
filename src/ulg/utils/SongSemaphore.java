package ulg.utils;

/**
 * Created by: Fabrizio Fubelli
 * Date: 15/01/2017.
 */
public interface SongSemaphore {
    /**
     * Aggiunge nella lista dei processi in attesa, il biglietto che permetterà di sbloccare il primo processo
     * che presenterà tale numero.
     * @param requestTicket il codice che farà sbloccare il processo in stato di wait
     */
    void newRequest(Integer requestTicket);

    /**
     * Il processo attende il suo turno
     * @param requestNumber il numero di richista, equivalente al ticket precedentemente presentato
     */
    void semWait(Integer requestNumber);

    /**
     * Invia un segnale al semaforo, che libererà il primo posto della coda
     */
    void semSignal();

}
