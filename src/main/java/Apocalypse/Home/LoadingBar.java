package Apocalypse.Home;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import apocalypse.db.DB_Manager;
import apocalypse.game.GameGUI;

/**
 * La classe LoadingBar rappresenta una finestra di dialogo che mostra una barra
 * di caricamento prima di avviare il gioco principale. Simula un processo di
 * caricamento e gestisce la transizione alla schermata di gioco.
 */
public class LoadingBar extends JDialog {

    private JProgressBar progressBar;
    private JLabel messageLabel;
    private Timer timer;
    private int linguaCorrenteId;
    private DB_Manager dbManager;

    /**
     * Costruisce una nuova istanza di LoadingBar.
     *
     * @param parent Il frame genitore per questa finestra di dialogo.
     * @param modal Determina se la finestra di dialogo deve essere modale.
     */
    public LoadingBar(Frame parent, boolean modal) {
        super(parent, modal);
        dbManager = new DB_Manager();
        linguaCorrenteId = dbManager.getLinguaCorrenteId();
        initComponents();
    }

    /**
     * Inizializza i componenti dell'interfaccia utente per la barra di
     * caricamento. Configura la barra di progresso, l'etichetta del messaggio e
     * il timer per simulare il caricamento.
     */
    private void initComponents() {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false); // Progresso determinato per un caricamento con durata specifica
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);

        messageLabel = new JLabel();
        aggiornaTestoMessaggio("caricamento_prep"); // Aggiorna il messaggio iniziale
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        getContentPane().add(panel);
        pack();

        setResizable(false); // Rendi non ridimensionabile la finestra
        setLocationRelativeTo(null); // Centra il dialogo rispetto al genitore

        // Timer per simulare il caricamento
        int durationInSeconds = 6; // Durata del caricamento in secondi
        int delay = 1000; // Ritardo in millisecondi tra le azioni del timer
        timer = new Timer(delay, new ActionListener() {
            int count = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                count++;
                progressBar.setValue((int) (100.0 * count / durationInSeconds));

                if (count >= durationInSeconds) {
                    stopLoading();
                    startGame();
                }
            }
        });
    }

    /**
     * Avvia il processo di caricamento simulato. Resetta la barra di progresso,
     * avvia il timer e rende visibile la finestra di dialogo.
     */
    public void startLoading() {
        progressBar.setValue(0); // Resetta il valore della progress bar
        timer.start();
        setVisible(true);
    }

    /**
     * Arresta il processo di caricamento simulato. Ferma il timer e nasconde la
     * finestra di dialogo.
     */
    public void stopLoading() {
        timer.stop();
        setVisible(false);
    }

    /**
     * Avvia il gioco principale dopo che il caricamento è completato. Chiude la
     * finestra di dialogo e inizializza l'interfaccia utente del gioco.
     */
    private void startGame() {
        // Chiude la finestra di dialogo e avvia il gioco dopo il caricamento
        dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Esempio: avvia la GUI del gioco
                GameGUI.createAndShowGUI();
            }
        });
    }

    /**
     * Aggiorna il testo del messaggio visualizzato durante il caricamento.
     *
     * @param chiaveMessaggio La chiave per recuperare il messaggio localizzato
     * dal database.
     */
    private void aggiornaTestoMessaggio(String chiaveMessaggio) {
        String testo = dbManager.getMessaggio(chiaveMessaggio, linguaCorrenteId);
        messageLabel.setText(testo);
    }
}
