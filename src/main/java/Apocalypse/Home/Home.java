package Apocalypse.Home;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import apocalypse.db.DB_Manager;

/**
 * La classe Home rappresenta la schermata principale del gioco Apocalisse
 * Zombie. Fornisce opzioni per iniziare una nuova partita, cambiare la lingua e
 * uscire dal gioco. La classe estende JFrame per creare la finestra principale
 * dell'applicazione.
 */
public class Home extends JFrame {

    private DB_Manager dbManager;
    private int linguaCorrenteId;
    private String linguaCorrente;
    private final JButton linguaButton;
    private final JButton nuovoButton;
    private final JButton esciButton;
    private JLabel titoloLabel;
    private Font titoloFontItaliano;
    private Font titoloFontInglese;

    /**
     * Costruisce una nuova istanza di Home, inizializzando la finestra
     * principale del gioco. Questo costruttore imposta i componenti
     * dell'interfaccia utente, carica le risorse necessarie e configura i
     * listener degli eventi per i pulsanti.
     */
    public Home() {
        dbManager = new DB_Manager();
        linguaCorrenteId = dbManager.getLinguaCorrenteId();
        linguaCorrente = dbManager.getLinguaNameById(linguaCorrenteId).toLowerCase();

        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        ImageIcon icon = new ImageIcon("image\\icona.png");
        setIconImage(icon.getImage());

        ImageIcon sfondoIcon = new ImageIcon("image\\sfondo.jpg");

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(sfondoIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);

        Font customFont = new Font("Berlin Sans FB", Font.BOLD, 21);

        titoloFontItaliano = new Font("Berlin Sans FB", Font.BOLD, 48);
        titoloFontInglese = new Font("Berlin Sans FB", Font.BOLD, 40);

        titoloLabel = new JLabel();
        titoloLabel.setForeground(Color.WHITE);
        titoloLabel.setBounds(100, 10, 800, 100);
        titoloLabel.setHorizontalAlignment(SwingConstants.LEFT);

        nuovoButton = createStyledButton("", customFont);
        esciButton = createStyledButton("", customFont);
        linguaButton = createStyledButton("", customFont);

        int buttonWidth = 250;
        int buttonHeight = 40;
        int buttonSpacing = 20;
        int leftMargin = 100;
        int topMargin = 120;

        nuovoButton.setBounds(leftMargin, topMargin, buttonWidth, buttonHeight);
        linguaButton.setBounds(leftMargin, topMargin + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);
        esciButton.setBounds(leftMargin, topMargin + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);

        panel.add(titoloLabel);
        panel.add(nuovoButton);
        panel.add(esciButton);
        panel.add(linguaButton);

        esciButton.addActionListener(e -> {
            dbManager.closeConnection();
            dispose();
            System.exit(0);
        });

        linguaButton.addActionListener(e -> cambiaLingua());

        nuovoButton.addActionListener(e -> {
            dispose();
            LoadingBar loadingDialog = new LoadingBar(Home.this, true);
            loadingDialog.startLoading();
        });

        add(panel);

        updateLanguageButtonText();
        updateTitles();

        setVisible(true);
    }

    /**
     * Crea un JButton stilizzato con una pittura personalizzata.
     *
     * @param text Il testo da visualizzare sul pulsante.
     * @param customFont Il font da utilizzare per il testo del pulsante.
     * @return Un JButton con uno stile personalizzato.
     */
    private JButton createStyledButton(String text, Font customFont) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getModel().isPressed() ? Color.BLACK : Color.WHITE);
                g.setFont(customFont);
                FontMetrics metrics = g.getFontMetrics(customFont);
                int x = 10;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g.drawString(getText(), x, y);
            }
        };
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }

    /**
     * Cambia la lingua corrente dell'applicazione. Questo metodo scorre tra le
     * lingue disponibili e aggiorna l'interfaccia utente di conseguenza.
     */
    private void cambiaLingua() {
        List<String> lingue = dbManager.getLingueDisponibili();
        if (lingue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna lingua disponibile nel database.");
            return;
        }

        int indiceCorrente = -1;
        for (int i = 0; i < lingue.size(); i++) {
            if (lingue.get(i).equalsIgnoreCase(linguaCorrente)) {
                indiceCorrente = i;
                break;
            }
        }

        if (indiceCorrente == -1) {
            return;
        }

        int nuovoIndice = (indiceCorrente + 1) % lingue.size();
        String nuovaLingua = lingue.get(nuovoIndice);

        dbManager.setLinguaCorrente(nuovaLingua);
        linguaCorrente = nuovaLingua.toLowerCase();
        linguaCorrenteId = dbManager.getLinguaCorrenteId();
        updateLanguageButtonText();
        updateTitles();
    }

    /**
     * Aggiorna il testo del pulsante della lingua in base alla lingua corrente.
     * Questo metodo viene chiamato ogni volta che la lingua viene cambiata.
     */
    private void updateLanguageButtonText() {
        SwingUtilities.invokeLater(() -> {
            String testoNuovoButton = dbManager.getTestoBottone("inizia_nuova_partita", linguaCorrenteId);
            String testoLinguaButton = dbManager.getTestoBottone("lingua", linguaCorrenteId);
            String testoEsciButton = dbManager.getTestoBottone("esci", linguaCorrenteId);

            nuovoButton.setText(testoNuovoButton != null ? testoNuovoButton : "");
            linguaButton.setText(testoLinguaButton + " " + capitalizeFirstLetter(linguaCorrente));
            esciButton.setText(testoEsciButton != null ? testoEsciButton : "");
        });
    }

    /**
     * Aggiorna i titoli dell'applicazione in base alla lingua corrente. Questo
     * include il titolo della finestra e l'etichetta del titolo principale.
     */
    private void updateTitles() {
        String titolo = (linguaCorrenteId == 1) ? "Apocalisse Zombie: Epidemia" : "Zombie Apocalypse: Outbreak";
        titoloLabel.setFont(linguaCorrenteId == 1 ? titoloFontItaliano : titoloFontInglese);
        setTitle(titolo);
        titoloLabel.setText(titolo);
    }

    /**
     * Rende maiuscola la prima lettera di una stringa data.
     *
     * @param input La stringa di input da capitalizzare.
     * @return La stringa con la prima lettera maiuscola.
     */
    private String capitalizeFirstLetter(String input) {
        return input.isEmpty() ? input : Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * Il metodo main che avvia l'applicazione creando un'istanza di Home.
     *
     * @param args Gli argomenti della riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Home::new);
    }
}
