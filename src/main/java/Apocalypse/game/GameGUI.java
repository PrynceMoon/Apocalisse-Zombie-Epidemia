package apocalypse.game;

import apocalypse.db.DB_Manager;
import Apocalypse.type.GameObject;
import Apocalypse.type.gameObjectContainer;
import Apocalypse.type.Missione;
import Apocalypse.Home.Home;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import java.util.HashMap;

/**
 * Classe che rappresenta l'interfaccia grafica principale del gioco. Gestisce
 * la visualizzazione della mappa, l'inventario, i dialoghi e le interazioni
 * dell'utente.
 */
public class GameGUI extends JPanel {

    private int linguaCorrenteId;
    private final DB_Manager dbManager;
    private final GameMap gameMap;
    private JTextArea dialoghiTextArea;
    private JTextField inputTextField;
    private String prompt = "> ";
    private static JFrame mainFrame;
    private final List<GameObject> gameObjects;
    private final List<GameObject> inventoryObjects;
    private final JList<String> inventoryList;
    private final DefaultListModel<String> inventoryListModel;
    private volatile boolean isLoadingDialogue = false;
    private final Map<Integer, gameObjectContainer> containers;
    private final JTextArea missioneTextArea;
    private final List<Missione> missioni;
    private int missioneCorrenteIndex;
    private boolean sieroDistribuito = false;

    /**
     * Costruttore della classe GameGUI. Inizializza tutti i componenti
     * dell'interfaccia grafica e carica i dati iniziali del gioco.
     */
    public GameGUI() {
        dbManager = new DB_Manager();
        dbManager.resetMissioni(this.linguaCorrenteId);
        this.linguaCorrenteId = dbManager.getLinguaCorrenteId();
        gameMap = new GameMap(dbManager);
        this.gameObjects = dbManager.getOggetti(this.linguaCorrenteId);
        this.inventoryObjects = new ArrayList<>();
        setLayout(new BorderLayout());
        this.containers = dbManager.getContenitori(this.linguaCorrenteId);
        loadObjectsIntoContainers();
        this.missioni = dbManager.getMissioni(this.linguaCorrenteId);
        this.missioneCorrenteIndex = 0;

        updateFrameTitle(linguaCorrenteId);

        JPanel missionePanel = new JPanel();
        // Modifica il pannello della missione
        missionePanel = new JPanel(new BorderLayout());
        missionePanel.setPreferredSize(new Dimension(800, 50));
        missionePanel.setBorder(BorderFactory.createTitledBorder("Missione"));

        missioneTextArea = new JTextArea();
        missioneTextArea.setEditable(false);
        missioneTextArea.setLineWrap(true);
        missioneTextArea.setWrapStyleWord(true);
        missionePanel.add(new JScrollPane(missioneTextArea), BorderLayout.CENTER);

        JPanel immaginePanel = new ImagePanel("image\\mappa.png");
        immaginePanel.setPreferredSize(new Dimension(800, 350));
        immaginePanel.setBorder(BorderFactory.createTitledBorder(""));

        JPanel dialoghiPanel = new JPanel(new BorderLayout());
        dialoghiPanel.setPreferredSize(new Dimension(800, 350));
        dialoghiPanel.setBorder(BorderFactory.createTitledBorder("Dialoghi e Iterazioni"));

        dialoghiTextArea = new JTextArea();
        dialoghiTextArea.setEditable(false);
        dialoghiTextArea.setLineWrap(true);
        dialoghiTextArea.setWrapStyleWord(true);

        inputTextField = new JTextField(prompt);
        inputTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (inputTextField.getCaretPosition() < prompt.length()) {
                    inputTextField.setCaretPosition(prompt.length());
                }
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && inputTextField.getCaretPosition() == prompt.length()) {
                    e.consume();
                }
            }
        });

        inputTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isLoadingDialogue) {
                    String input = inputTextField.getText().substring(prompt.length()).trim();
                    if (!input.isEmpty()) {
                        input = formatCommand(input);
                        dialoghiTextArea.append(prompt + input + "\n");
                        processCommand(input);
                    }
                    inputTextField.setText(prompt);
                }
            }
        });

        JScrollPane dialoghiScrollPane = new JScrollPane(dialoghiTextArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputTextField, BorderLayout.CENTER);

        dialoghiPanel.add(dialoghiScrollPane, BorderLayout.CENTER);
        dialoghiPanel.add(inputPanel, BorderLayout.SOUTH);

        JPanel guidaPanel = new JPanel();
        guidaPanel.setPreferredSize(new Dimension(400, 350));
        guidaPanel.setBorder(BorderFactory.createTitledBorder("Guida"));

        JTextArea guidaTextArea = new JTextArea();
        guidaTextArea.setEditable(false);

        List<String> comandi = dbManager.getComandi(linguaCorrenteId);
        for (String comando : comandi) {
            guidaTextArea.append("  " + comando + "\n");
        }
        guidaTextArea.setRows(Math.min(comandi.size(), 8));
        guidaTextArea.setColumns(30);
        guidaTextArea.setLineWrap(true);
        guidaTextArea.setWrapStyleWord(true);

        guidaPanel.setLayout(new BorderLayout());
        guidaPanel.add(new JScrollPane(guidaTextArea), BorderLayout.CENTER);

        JPanel zainoPanel = new JPanel();
        zainoPanel.setPreferredSize(new Dimension(400, 350));
        zainoPanel.setBorder(BorderFactory.createTitledBorder("Zaino"));

        inventoryListModel = new DefaultListModel<>();
        inventoryList = new JList<>(inventoryListModel);
        JScrollPane inventoryScrollPane = new JScrollPane(inventoryList);
        zainoPanel.setLayout(new BorderLayout());
        zainoPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(missionePanel, BorderLayout.NORTH);
        leftPanel.add(immaginePanel, BorderLayout.CENTER); // Aggiunta dell'immagine della mappa
        leftPanel.add(dialoghiPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 1));
        rightPanel.add(guidaPanel);
        rightPanel.add(zainoPanel);

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        String trama = dbManager.getTrama(linguaCorrenteId);
        String introduzione = dbManager.getDialogo("introduzione", linguaCorrenteId);

        if (!trama.isEmpty()) {
            displayMessage(trama, () -> {
                try {
                    Thread.sleep(7000); // Pausa di 2 secondi
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                displayMessage(introduzione, null);
            });
        } else {
            System.out.println("Trama non trovata per la lingua ID: " + linguaCorrenteId);
            displayMessage(introduzione, null);
            displayMessage("\n", null);
        }

        aggiornaMissioneVisualizzata();
    }

    /**
     * Visualizza un messaggio nella finestra di dialogo, carattere per
     * carattere.
     *
     * @param message Il messaggio da visualizzare
     * @param onComplete Azione da eseguire al completamento della
     * visualizzazione
     */
    private void displayMessage(String message, Runnable onComplete) {
        isLoadingDialogue = true;
        setInputEnabled(false);

        SwingUtilities.invokeLater(() -> dialoghiTextArea.setText(""));

        Thread displayThread = new Thread(() -> {
            String[] paragraphs = message.split("\n");
            for (int p = 0; p < paragraphs.length; p++) {
                String paragraph = paragraphs[p];
                for (int i = 0; i < paragraph.length(); i++) {
                    final char c = paragraph.charAt(i);
                    SwingUtilities.invokeLater(() -> {
                        dialoghiTextArea.append(String.valueOf(c));
                        dialoghiTextArea.setCaretPosition(dialoghiTextArea.getDocument().getLength());
                    });
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (p < paragraphs.length - 1) {
                    SwingUtilities.invokeLater(() -> dialoghiTextArea.append("\n\n"));
                    try {
                        Thread.sleep(1000); // Pausa di 1 secondo tra i paragrafi
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            SwingUtilities.invokeLater(() -> {
                dialoghiTextArea.append("\n"); // Aggiunge due righe vuote alla fine del messaggio
                isLoadingDialogue = false;
                setInputEnabled(true);
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
        displayThread.start();
    }

    /**
     * Formatta il comando inserito dall'utente.
     *
     * @param command Il comando da formattare
     * @return Il comando formattato
     */
    private String formatCommand(String command) {
        String[] words = command.split(" ");
        StringBuilder formattedCommand = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                formattedCommand.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formattedCommand.toString().trim();
    }

    /**
     * Processa il comando inserito dall'utente ed esegue l'azione
     * corrispondente.
     *
     * @param input Il comando inserito dall'utente
     */
    private void processCommand(String input) {
        String[] commandParts = input.split(" ", 2);
        String command = commandParts[0].toLowerCase();
        String target = commandParts.length > 1 ? commandParts[1].toLowerCase() : "";

        switch (linguaCorrenteId) {
            case 1: // Italiano
                switch (command) {
                    case "guarda":
                        mostraDescrizioneStanza();
                        verificaCompletamentoMissione();
                        break;
                    case "prendi":
                        prendiOggetto(target);
                        verificaCompletamentoMissione();
                        break;
                    case "apri":
                        apriOggetto(target);
                        verificaCompletamentoMissione();
                        break;
                    case "usa":
                        usaOggetto(target);
                        verificaCompletamentoMissione();
                        break;
                    case "nord":
                    case "sud":
                    case "ovest":
                    case "est":
                        if (gameMap.isExitRoom() && command.equalsIgnoreCase("sud")) {
                            tentaFuga();
                        } else {
                            muoviGiocatore(command);
                        }
                        verificaCompletamentoMissione();
                        break;
                    case "unisci":
                        unisciOggetti();
                        verificaCompletamentoMissione();
                        break;
                    case "esamina":
                        esamina(target);
                        verificaCompletamentoMissione();
                        break;
                    case "dai":
                        if (target.equals("siero")) {
                            daiSiero();
                        } else {
                            dialoghiTextArea.append(dbManager.getMessaggio("comando_non_valido", linguaCorrenteId) + "\n");
                        }
                        verificaCompletamentoMissione();
                        break;
                    case "bevi":
                        if (target.equals("siero")) {
                            beviSiero();
                        } else {
                            dialoghiTextArea.append(dbManager.getMessaggio("comando_non_valido", linguaCorrenteId) + "\n");
                        }
                        break;
                    default:
                        dialoghiTextArea.append(dbManager.getMessaggio("unrecognized_command", linguaCorrenteId) + "\n");
                        break;
                }
                break;
            case 2: // Inglese
                switch (command) {
                    case "look":
                        mostraDescrizioneStanza();
                        verificaCompletamentoMissione();
                        break;
                    case "take":
                        prendiOggetto(target);
                        verificaCompletamentoMissione();
                        break;
                    case "open":
                        apriOggetto(target);
                        verificaCompletamentoMissione();
                        break;
                    case "use":
                        usaOggetto(target);
                        verificaCompletamentoMissione();
                        break;
                    case "north":
                    case "south":
                    case "west":
                    case "east":
                        if (gameMap.isExitRoom() && command.equalsIgnoreCase("south")) {
                            tentaFuga();
                        } else {
                            muoviGiocatore(command);
                        }
                        verificaCompletamentoMissione();
                        break;
                    case "combine":
                        unisciOggetti();
                        verificaCompletamentoMissione();
                        break;
                    case "examine":
                        esamina(target);
                        verificaCompletamentoMissione();
                        break;
                    case "give":
                        if (target.equals("serum")) {
                            daiSiero();
                        } else {
                            dialoghiTextArea.append(dbManager.getMessaggio("comando_non_valido", linguaCorrenteId) + "\n");
                        }
                        verificaCompletamentoMissione();
                        break;
                    case "drink":
                        if (target.equals("serum")) {
                            beviSiero();
                        } else {
                            dialoghiTextArea.append(dbManager.getMessaggio("comando_non_valido", linguaCorrenteId) + "\n");
                        }
                        break;
                    default:
                        dialoghiTextArea.append(dbManager.getMessaggio("unrecognized_command", linguaCorrenteId) + "\n");
                        break;
                }
                break;
            default:
                dialoghiTextArea.append(dbManager.getMessaggio("unrecognized_command", linguaCorrenteId) + "\n");
                break;
        }
    }

    /**
     * Tenta la fuga del giocatore dall'edificio.
     */
    private void tentaFuga() {
        boolean hasKeys = inventoryObjects.stream()
                .anyMatch(obj -> obj.getName().equalsIgnoreCase("chiavi") || obj.getName().equalsIgnoreCase("keys"));

        if (!hasKeys) {
            dialoghiTextArea.append(dbManager.getMessaggio("no_keys", linguaCorrenteId) + "\n");
        } else if (missioneCorrenteIndex < missioni.size() - 1) {
            dialoghiTextArea.append(dbManager.getMessaggio("cannot_exit_building", linguaCorrenteId) + "\n");
        } else {
            mostraScenaFinale();
        }
    }

    /**
     * Mostra la scena finale del gioco.
     */
    private void mostraScenaFinale() {
        isLoadingDialogue = true;
        setInputEnabled(false);

        Thread displayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] scenaFinale = {
                    dbManager.getDialogo("scena_finale", linguaCorrenteId),
                    dbManager.getDialogo("scena_finale2", linguaCorrenteId),
                    dbManager.getDialogo("scena_finale3", linguaCorrenteId)
                };

                for (String scena : scenaFinale) {
                    for (int i = 0; i < scena.length(); i++) {
                        final String character = String.valueOf(scena.charAt(i));
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dialoghiTextArea.append(character);
                            }
                        });
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    dialoghiTextArea.append("\n\n");
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mostraPopupFinale();
                    }
                });
            }
        });
        displayThread.start();
    }

    /**
     * Mostra un popup alla fine del gioco.
     */
    private void mostraPopupFinale() {
        JOptionPane optionPane = new JOptionPane(
                dbManager.getMessaggio("game_completed", linguaCorrenteId),
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{dbManager.getMessaggio("home_button", linguaCorrenteId)},
                null);

        JDialog dialog = optionPane.createDialog(this, dbManager.getMessaggio("congratulations", linguaCorrenteId));

        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                    dialog.dispose();
                    tornaAllaHome();
                }
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Ritorna alla schermata principale del gioco.
     */
    private void tornaAllaHome() {
        SwingUtilities.invokeLater(() -> {
            // Chiudi il frame corrente del gioco
            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            currentFrame.dispose();

            // Crea e mostra una nuova istanza della Home
            new Home();
        });
    }

    /**
     * Muove il giocatore nella direzione specificata.
     *
     * @param direction La direzione in cui muovere il giocatore
     */
    private void muoviGiocatore(String direction) {
        try {
            boolean hasKeycard = checkKeycard();
            gameMap.move(direction, hasKeycard);
            dialoghiTextArea.append(dbManager.getMessaggio("moved_to", this.linguaCorrenteId) + " " + gameMap.getCurrentRoom().getName() + ".\n");
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("PinRequired")) {
                String inputPin = JOptionPane.showInputDialog(this, dbManager.getMessaggio("enter_pin", this.linguaCorrenteId));
                if (inputPin != null) {
                    if (gameMap.checkSecurityPin(inputPin)) {
                        gameMap.enterRoomWithPin(direction);
                        dialoghiTextArea.append(dbManager.getMessaggio("correct_pin", this.linguaCorrenteId) + " " + gameMap.getCurrentRoom().getName() + ".\n");
                    } else {
                        dialoghiTextArea.append(dbManager.getMessaggio("incorrect_pin", this.linguaCorrenteId) + "\n");
                    }
                } else {
                    dialoghiTextArea.append(dbManager.getMessaggio("operation_cancelled", this.linguaCorrenteId) + "\n");
                }
            } else {
                dialoghiTextArea.append(e.getMessage() + "\n");
            }
        }
    }

    /**
     * Verifica se il giocatore possiede la tessera magnetica.
     *
     * @return true se il giocatore possiede la tessera, false altrimenti
     */
    private boolean checkKeycard() {
        for (GameObject obj : inventoryObjects) {
            if (("tesserino".equalsIgnoreCase(obj.getName()) || "badge".equalsIgnoreCase(obj.getName()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mostra la descrizione della stanza corrente.
     */
    private void mostraDescrizioneStanza() {
        String currentRoomName = gameMap.getCurrentRoom().getName();
        int linguaCorrenteId = dbManager.getLinguaCorrenteId();
        String roomDescription = dbManager.getRoomDescription(currentRoomName, linguaCorrenteId);
        dialoghiTextArea.append(roomDescription + "\n");
    }

    /**
     * Permette al giocatore di prendere un oggetto dalla stanza corrente.
     *
     * @param nomeOggetto Il nome dell'oggetto da prendere
     */
    private void prendiOggetto(String nomeOggetto) {
        int currentRoomId = gameMap.getCurrentRoom().getId();
        String nomeOggettoFormattato = nomeOggetto.trim().toLowerCase();

        if (inventoryObjects.stream().anyMatch(obj -> obj.getName().trim().toLowerCase().equals(nomeOggettoFormattato))) {
            dialoghiTextArea.append(dbManager.getMessaggio("object_already_taken", linguaCorrenteId) + "\n");
            return;
        }

        gameObjects.stream()
                .filter(obj -> obj.getIdStanza() != null && obj.getIdStanza().equals(currentRoomId))
                .filter(obj -> {
                    String objNameLower = obj.getName().trim().toLowerCase();
                    if (objNameLower.equals("ricetta siero")) {
                        return nomeOggettoFormattato.equals("fogli")
                                || nomeOggettoFormattato.equals("ricetta siero")
                                || nomeOggettoFormattato.equals("ricetta del siero");
                    }
                    return objNameLower.equals(nomeOggettoFormattato);
                })
                .findFirst()
                .ifPresentOrElse(
                        obj -> {
                            if (obj.isPrendibile()) {
                                obj.setIdStanza(null);
                                inventoryObjects.add(obj);
                                gameObjects.remove(obj);
                                updateInventoryDisplay();
                                dialoghiTextArea.append(dbManager.getMessaggio("taken_object", linguaCorrenteId) + " " + obj.getName() + "\n");
                                verificaCompletamentoMissione();
                            } else {
                                dialoghiTextArea.append(dbManager.getMessaggio("object_not_takeable", linguaCorrenteId) + "\n");
                            }
                        },
                        () -> dialoghiTextArea.append(String.format(dbManager.getMessaggio("object_not_found", linguaCorrenteId), nomeOggetto) + "\n")
                );
    }

    /**
     * Permette al giocatore di usare un oggetto.
     *
     * @param nomeOggetto Il nome dell'oggetto da usare
     */
    private void usaOggetto(String nomeOggetto) {
        String nomeOggettoFormattato = nomeOggetto.trim().toLowerCase();

        if (gameMap.getCurrentRoom().getName().equalsIgnoreCase("Stanza dei Server")
                || gameMap.getCurrentRoom().getName().equalsIgnoreCase("Server Room")) {

            if (nomeOggettoFormattato.equals("computer")) {
                dialoghiTextArea.append(dbManager.getMessaggio("vuoi_collegare_usb", this.linguaCorrenteId) + "\n");

                inputTextField.setText(prompt);
                inputTextField.setEnabled(true);
                inputTextField.requestFocus();

                ActionListener defaultListener = inputTextField.getActionListeners()[0];
                inputTextField.removeActionListener(defaultListener);

                ActionListener tempListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String risposta = inputTextField.getText().substring(prompt.length()).trim().toLowerCase();
                        inputTextField.removeActionListener(this);
                        inputTextField.addActionListener(defaultListener);

                        if (risposta.equals("si") || risposta.equals("yes")) {
                            boolean hasPendrive = false;
                            for (GameObject obj : inventoryObjects) {
                                if (obj.getName().equalsIgnoreCase("chiavetta usb") || obj.getName().equalsIgnoreCase("usb key")) {
                                    hasPendrive = true;
                                    String scopertaMessaggio = dbManager.getDialogo("scoperta", linguaCorrenteId);
                                    displayMessage(scopertaMessaggio, () -> {
                                    });
                                    completaMissioneScoperta();
                                    break;
                                }
                            }

                            if (!hasPendrive) {
                                dialoghiTextArea.append(String.format(dbManager.getMessaggio("object_not_in_inventory", linguaCorrenteId), "chiavetta USB") + "\n");
                            }
                        } else {
                            dialoghiTextArea.append(dbManager.getMessaggio("non_collegata_usb", linguaCorrenteId) + "\n");
                        }

                        inputTextField.setText(prompt);
                    }
                };

                inputTextField.addActionListener(tempListener);
                return;
            }
        }
        dialoghiTextArea.append(String.format(dbManager.getMessaggio("oggetto_non_utilizzabile", linguaCorrenteId), nomeOggetto) + "\n");
    }

    /**
     * Permette al giocatore di bere il siero.
     */
    private void beviSiero() {
        boolean sieroPresenteInInventario = inventoryObjects.stream()
                .anyMatch(obj -> obj.getName().equalsIgnoreCase("siero") || obj.getName().equalsIgnoreCase("serum"));

        if (sieroPresenteInInventario) {
            dialoghiTextArea.append(dbManager.getMessaggio("siero_bevuto", linguaCorrenteId) + "\n");
            updateInventoryDisplay();
            verificaCompletamentoMissione();
        } else {
            dialoghiTextArea.append(dbManager.getMessaggio("siero_non_presente", linguaCorrenteId) + "\n");
        }
    }

    /**
     * Completa la missione di scoperta.
     */
    private void completaMissioneScoperta() {
        if (missioneCorrenteIndex == 1) {  // Assicuriamoci che sia la seconda missione
            Missione missioneCorrente = missioni.get(missioneCorrenteIndex);
            missioneCorrente.setCompletata(true);
            dbManager.aggiornaMissioneCompletata(missioneCorrente.getId(), true);
            missioneCorrenteIndex++;
            aggiornaMissioneVisualizzata();
            dialoghiTextArea.append("Missione completata: " + missioneCorrente.getNome() + "\n");
        }
    }

    /**
     * Aggiorna la visualizzazione dell'inventario.
     */
    private void updateInventoryDisplay() {
        inventoryListModel.clear();
        inventoryObjects.stream()
                .map(GameObject::getName)
                .forEach(inventoryListModel::addElement);
    }

    /**
     * Aggiorna il titolo della finestra principale.
     *
     * @param linguaId L'ID della lingua corrente
     */
    private void updateFrameTitle(int linguaId) {
        String titolo = (linguaId == 1) ? "Apocalisse Zombie: Epidemia" : "Zombie Apocalypse: Outbreak";
        mainFrame.setTitle(titolo);
    }

    /**
     * Abilita o disabilita l'input dell'utente.
     *
     * @param enabled true per abilitare l'input, false per disabilitarlo
     */
    private void setInputEnabled(boolean enabled) {
        inputTextField.setEnabled(enabled);
        if (enabled) {
            inputTextField.requestFocus();
        }
    }

    /**
     * Carica gli oggetti nei rispettivi contenitori.
     */
    private void loadObjectsIntoContainers() {
        for (GameObject obj : gameObjects) {
            if (obj.getIdContenitore() != null) {
                gameObjectContainer container = containers.get(obj.getIdContenitore());
                if (container != null) {
                    container.addGameObj(obj);
                }
            }
        }
    }

    /**
     * Permette al giocatore di aprire un oggetto.
     *
     * @param nomeOggetto Il nome dell'oggetto da aprire
     */
    private void apriOggetto(String nomeOggetto) {
        int currentRoomId = gameMap.getCurrentRoom().getId();
        String nomeOggettoFormattato = nomeOggetto.trim().toLowerCase();

        // Controllo per armadietto/locker senza numero
        if ((nomeOggettoFormattato.startsWith("armadietto") && !nomeOggettoFormattato.matches("armadietto \\d+"))
                || (nomeOggettoFormattato.startsWith("cabinet") && !nomeOggettoFormattato.matches("cabinet \\d+"))) {
            chiediNumeroArmadietto();
            return;
        }

        containers.values().stream()
                .filter(container -> container.getIdStanza() == currentRoomId)
                .filter(container -> {
                    String containerName = container.getName().toLowerCase();
                    if (containerName.equals(nomeOggettoFormattato)) {
                        return true;
                    }
                    if ((containerName.startsWith("armadietto") && nomeOggettoFormattato.startsWith("armadietto"))
                            || (containerName.startsWith("cabinet") && nomeOggettoFormattato.startsWith("cabinet"))) {
                        String[] containerParts = containerName.split(" ");
                        String[] inputParts = nomeOggettoFormattato.split(" ");
                        return containerParts.length == 2 && inputParts.length == 2 && containerParts[1].equals(inputParts[1]);
                    }
                    return false;
                })
                .findFirst()
                .ifPresentOrElse(
                        container -> {
                            if (container.isContainerEmpty()) {
                                dialoghiTextArea.append(String.format(dbManager.getMessaggio("contenitore_vuoto", linguaCorrenteId), container.getName()) + "\n");
                            } else {
                                if (container.getName().toLowerCase().contains("cassetto") || container.getName().toLowerCase().contains("drawer")) {
                                    apriCassettoScrivania(container);
                                } else if (container.getName().toLowerCase().contains("armadietto") || container.getName().toLowerCase().contains("cabinet")) {
                                    apriArmadietto(container);
                                }
                            }
                        },
                        () -> dialoghiTextArea.append(dbManager.getMessaggio("niente_da_aprire", linguaCorrenteId) + "\n")
                );
    }

    /**
     * Chiede all'utente di specificare il numero dell'armadietto da aprire.
     */
    private void chiediNumeroArmadietto() {
        dialoghiTextArea.append(dbManager.getMessaggio("chiedi_numero_armadietto", linguaCorrenteId) + "\n");
        inputTextField.setText(prompt);
        inputTextField.setEnabled(true);
        inputTextField.requestFocus();
        ActionListener defaultListener = inputTextField.getActionListeners()[0];
        inputTextField.removeActionListener(defaultListener);

        ActionListener tempListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String risposta = inputTextField.getText().substring(prompt.length()).trim();
                inputTextField.removeActionListener(this);
                inputTextField.addActionListener(defaultListener);

                try {
                    int numeroArmadietto = Integer.parseInt(risposta);
                    if (numeroArmadietto >= 1 && numeroArmadietto <= 5) {
                        // Try both "armadietto" and "cabinet" versions
                        boolean armadiettoAperto = tryOpenContainer("armadietto " + numeroArmadietto);
                        boolean cabinetAperto = tryOpenContainer("cabinet " + numeroArmadietto);

                        if (!armadiettoAperto && !cabinetAperto) {
                            dialoghiTextArea.append(dbManager.getMessaggio("niente_da_aprire", linguaCorrenteId) + "\n");
                        }
                    } else {
                        dialoghiTextArea.append(dbManager.getMessaggio("armadietti_limite", linguaCorrenteId) + "\n");
                    }
                } catch (NumberFormatException ex) {
                    dialoghiTextArea.append(dbManager.getMessaggio("numero_non_valido", linguaCorrenteId) + "\n");
                }

                inputTextField.setText(prompt);
                inputTextField.setEnabled(true);
                inputTextField.requestFocus();
            }
        };
        inputTextField.addActionListener(tempListener);
    }

    private boolean tryOpenContainer(String containerName) {
        int currentRoomId = gameMap.getCurrentRoom().getId();
        return containers.values().stream()
                .filter(container -> container.getIdStanza() == currentRoomId)
                .filter(container -> container.getName().toLowerCase().equals(containerName.toLowerCase()))
                .findFirst()
                .map(container -> {
                    if (container.isContainerEmpty()) {
                        dialoghiTextArea.append(String.format(dbManager.getMessaggio("contenitore_vuoto", linguaCorrenteId), container.getName()) + "\n");
                    } else {
                        apriArmadietto(container);
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Apre il cassetto della scrivania.
     *
     * @param container Il contenitore rappresentante il cassetto
     */
    private void apriCassettoScrivania(gameObjectContainer container) {
        dialoghiTextArea.append(String.format(dbManager.getMessaggio("contenitore_aperto", this.linguaCorrenteId), "il cassetto") + "\n");
        dialoghiTextArea.append(dbManager.getMessaggio("chiavi_trovate", this.linguaCorrenteId) + "\n");
        dialoghiTextArea.append(dbManager.getMessaggio("vuoi_prendere_chiavi", this.linguaCorrenteId) + "\n");

        gestisciInputUtente(container, "chiavi", "keys");
    }

    /**
     * Apre un armadietto.
     *
     * @param container Il contenitore rappresentante l'armadietto
     */
    private void apriArmadietto(gameObjectContainer container) {
        String nomeArmadietto = container.getName();
        dialoghiTextArea.append(String.format(dbManager.getMessaggio("contenitore_aperto", this.linguaCorrenteId), nomeArmadietto) + "\n");

        // Elenca gli oggetti nell'armadietto
        if (!container.getContainerList().isEmpty()) {
            dialoghiTextArea.append(dbManager.getMessaggio("contenuto_armadietto", this.linguaCorrenteId) + "\n");
            for (GameObject obj : container.getContainerList()) {
                dialoghiTextArea.append("- " + obj.getName() + "\n");
            }
            dialoghiTextArea.append(String.format(dbManager.getMessaggio("vuoi_prendere_oggetto", this.linguaCorrenteId), nomeArmadietto) + "\n");
            gestisciInputUtente(container, null, null);
        } else {
            dialoghiTextArea.append(dbManager.getMessaggio("contenitore_vuoto", this.linguaCorrenteId) + "\n");
        }
    }

    /**
     * Gestisce l'input dell'utente per l'interazione con i contenitori.
     *
     * @param container Il contenitore con cui si sta interagendo
     * @param oggetto1 Il primo oggetto da considerare (può essere null)
     * @param oggetto2 Il secondo oggetto da considerare (può essere null)
     */
    private void gestisciInputUtente(final gameObjectContainer container, final String oggetto1, final String oggetto2) {
        inputTextField.setText(prompt);
        inputTextField.setEnabled(true);
        inputTextField.requestFocus();
        final ActionListener defaultListener = inputTextField.getActionListeners()[0];
        inputTextField.removeActionListener(defaultListener);

        ActionListener tempListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String risposta = inputTextField.getText().substring(prompt.length()).trim().toLowerCase();
                inputTextField.removeActionListener(this);
                inputTextField.addActionListener(defaultListener);

                Thread processingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (risposta.equals("si") || risposta.equals("yes")) {
                            boolean oggettoTrovato = false;
                            List<GameObject> toRemove = new ArrayList<>();
                            for (GameObject obj : container.getContainerList()) {
                                if ((oggetto1 != null && obj.getName().equalsIgnoreCase(oggetto1))
                                        || (oggetto2 != null && obj.getName().equalsIgnoreCase(oggetto2))
                                        || (oggetto1 == null && oggetto2 == null)) {
                                    oggettoTrovato = true;
                                    inventoryObjects.add(obj);
                                    toRemove.add(obj);
                                    final String objName = obj.getName();
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialoghiTextArea.append(String.format(dbManager.getMessaggio("oggetto_preso", linguaCorrenteId), objName) + "\n");
                                        }
                                    });
                                }
                            }
                            for (GameObject obj : toRemove) {
                                container.removeContList(obj);
                            }
                            final boolean finalOggettoTrovato = oggettoTrovato;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateInventoryDisplay();
                                    if (!finalOggettoTrovato) {
                                        dialoghiTextArea.append(dbManager.getMessaggio("niente_da_aprire", linguaCorrenteId) + "\n");
                                    }
                                }
                            });
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    dialoghiTextArea.append(String.format(dbManager.getMessaggio("contenitore_chiuso", linguaCorrenteId), container.getName()) + "\n");
                                }
                            });
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                inputTextField.setText(prompt);
                                inputTextField.setEnabled(true);
                                inputTextField.requestFocus();
                            }
                        });
                    }
                });
                processingThread.start();
            }
        };
        inputTextField.addActionListener(tempListener);
    }

    /**
     * Unisce gli oggetti per creare il siero.
     */
    private void unisciOggetti() {
        if (!gameMap.getCurrentRoom().getName().equalsIgnoreCase("Laboratorio")
                && !gameMap.getCurrentRoom().getName().equalsIgnoreCase("Laboratory")) {
            dialoghiTextArea.append(dbManager.getMessaggio("unisci_solo_laboratorio", linguaCorrenteId) + "\n");
            return;
        }

        if (!verificaIngredientiSiero()) {
            dialoghiTextArea.append(dbManager.getMessaggio("mancano_ingredienti", linguaCorrenteId) + "\n");
            return;
        }

        dialoghiTextArea.append(dbManager.getMessaggio("inserisci_ordine_ingredienti", linguaCorrenteId) + "\n");
        gestisciInserimentoIngredienti();
    }

    /**
     * Gestisce l'inserimento degli ingredienti per la creazione del siero.
     */
    private void gestisciInserimentoIngredienti() {
        inputTextField.setText(prompt);
        inputTextField.setEnabled(true);
        inputTextField.requestFocus();

        final ActionListener defaultListener = inputTextField.getActionListeners()[0];
        inputTextField.removeActionListener(defaultListener);

        final int[] currentIngredientIndex = {0};
        final String[] currentIngredienti = INGREDIENTI_SIERO.get(linguaCorrenteId);

        ActionListener ingredientListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String input = inputTextField.getText().substring(prompt.length()).trim().toLowerCase();

                Thread processingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (input.equalsIgnoreCase(currentIngredienti[currentIngredientIndex[0]])) {
                            currentIngredientIndex[0]++;
                            if (currentIngredientIndex[0] == currentIngredienti.length) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        creaSiero();
                                        inputTextField.removeActionListener(inputTextField.getActionListeners()[0]);
                                        inputTextField.addActionListener(defaultListener);
                                    }
                                });
                            } else {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialoghiTextArea.append(dbManager.getMessaggio("inserisci_prossimo_ingrediente", linguaCorrenteId) + "\n");
                                    }
                                });
                            }
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    dialoghiTextArea.append(dbManager.getMessaggio("ordine_sbagliato", linguaCorrenteId) + "\n");
                                    inputTextField.removeActionListener(inputTextField.getActionListeners()[0]);
                                    inputTextField.addActionListener(defaultListener);
                                }
                            });
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                inputTextField.setText(prompt);
                            }
                        });
                    }
                });
                processingThread.start();
            }
        };
        inputTextField.addActionListener(ingredientListener);
    }

    private static final Map<Integer, String[]> INGREDIENTI_SIERO = new HashMap<>();

    static {
        INGREDIENTI_SIERO.put(1, new String[]{"becher", "filtro", "acido citrico", "solfato di magnesio", "idrossido di calcio", "acqua distillata"});
        INGREDIENTI_SIERO.put(2, new String[]{"beaker", "filter", "citric acid", "magnesium sulfate", "calcium hydroxide", "distilled water"});
    }

    /**
     * Crea il siero combinando gli ingredienti necessari.
     */
    private void creaSiero() {
        // Rimuovi gli ingredienti dall'inventario
        for (String ingrediente : INGREDIENTI_SIERO.get(linguaCorrenteId)) {
            rimuoviOggettoDaInventario(ingrediente);
        }

        // Aggiungi il siero all'inventario
        GameObject siero = new GameObject(0, "Siero", linguaCorrenteId, null, true, false, null);
        inventoryObjects.add(siero);
        updateInventoryDisplay();

        dialoghiTextArea.append(dbManager.getMessaggio("siero_creato", linguaCorrenteId) + "\n");

        verificaCompletamentoMissione();
    }

    /**
     * Rimuove un oggetto dall'inventario del giocatore.
     *
     * @param nomeOggetto Il nome dell'oggetto da rimuovere
     */
    private void rimuoviOggettoDaInventario(String nomeOggetto) {
        inventoryObjects.removeIf(obj -> obj.getName().equalsIgnoreCase(nomeOggetto));
    }

    /**
     * Permette al giocatore di esaminare un oggetto nel suo inventario.
     *
     * @param nomeOggetto Il nome dell'oggetto da esaminare
     */
    private void esamina(String nomeOggetto) {
        String nomeOggettoFormattato = nomeOggetto.trim().toLowerCase();
        for (GameObject obj : inventoryObjects) {
            if (obj.getName().trim().toLowerCase().equals(nomeOggettoFormattato)) {
                dialoghiTextArea.append(obj.getDescription() + "\n");
                return;
            }
        }
        dialoghiTextArea.append(dbManager.getMessaggio("object_not_in_inventory", linguaCorrenteId) + "\n");
    }

    /**
     * Permette al giocatore di dare il siero ai sopravvissuti.
     */
    private void daiSiero() {
        if (!gameMap.getCurrentRoom().getName().equalsIgnoreCase("Dormitori")
                && !gameMap.getCurrentRoom().getName().equalsIgnoreCase("Dormitories")) {
            dialoghiTextArea.append(dbManager.getMessaggio("nessuno_presente", linguaCorrenteId) + "\n");
            return;
        }

        boolean sieroPresenteInInventario = inventoryObjects.stream()
                .anyMatch(obj -> obj.getName().equalsIgnoreCase("siero") || obj.getName().equalsIgnoreCase("serum"));

        if (!sieroPresenteInInventario) {
            dialoghiTextArea.append(dbManager.getMessaggio("siero_non_presente", linguaCorrenteId) + "\n");
            return;
        }

        String scena = dbManager.getDialogo("distribuzione_siero", linguaCorrenteId);
        displayMessage(scena, () -> {
        });

        // Rimuovi il siero dall'inventario solo dopo averlo distribuito
        inventoryObjects.removeIf(obj -> obj.getName().equalsIgnoreCase("siero") || obj.getName().equalsIgnoreCase("serum"));
        updateInventoryDisplay();

        // Aggiungi un flag per indicare che il siero è stato distribuito
        sieroDistribuito = true;

        // Completa la missione
        verificaCompletamentoMissione();
    }

    /**
     * Aggiorna la visualizzazione della missione corrente.
     */
    private void aggiornaMissioneVisualizzata() {
        if (missioneCorrenteIndex < missioni.size()) {
            Missione missioneCorrente = missioni.get(missioneCorrenteIndex);
            missioneTextArea.setText(missioneCorrente.getNome() + ": " + missioneCorrente.getDescrizione());
        } else {
            missioneTextArea.setText(linguaCorrenteId == 1 ? "Tutte le missioni completate!" : "All missions completed!");
        }
    }

    /**
     * Verifica se la missione corrente è stata completata e aggiorna lo stato
     * del gioco di conseguenza.
     */
    private void verificaCompletamentoMissione() {
        if (missioneCorrenteIndex < missioni.size()) {
            Missione missioneCorrente = missioni.get(missioneCorrenteIndex);
            boolean completata = false;

            Supplier<Boolean> verificaMissione = () -> false;

            switch (missioneCorrenteIndex) {
                case 0: // Ispeziona l'edificio
                    verificaMissione = () -> inventoryObjects.stream()
                            .anyMatch(obj -> obj.getName().equalsIgnoreCase("chiavetta usb")
                            || obj.getName().equalsIgnoreCase("usb key"));
                    break;
                case 1: // Scopri il contenuto della USB
                    // Questa verifica viene gestita direttamente in usaOggetto()
                    break;
                case 2: // Trova la ricetta del siero
                    verificaMissione = () -> inventoryObjects.stream()
                            .anyMatch(obj -> obj.getName().equalsIgnoreCase("ricetta siero")
                            || obj.getName().equalsIgnoreCase("serum recipe"));
                    break;
                case 3: // Trova gli elementi per creare il siero
                    verificaMissione = this::verificaIngredientiSiero;
                    break;
                case 4: // Crea il siero
                    verificaMissione = () -> inventoryObjects.stream()
                            .anyMatch(obj -> obj.getName().equalsIgnoreCase("siero")
                            || obj.getName().equalsIgnoreCase("serum"));
                    break;
                case 5: // Bevi il siero
                    verificaMissione = () -> dialoghiTextArea.getText()
                            .contains(dbManager.getMessaggio("siero_bevuto", linguaCorrenteId));
                    break;
                case 6: // Dai il siero
                    verificaMissione = () -> sieroDistribuito;
                    break;
                case 7: // Scappa dall'edificio
                    verificaMissione = () -> gameMap.getCurrentRoom().getName()
                            .equalsIgnoreCase("Fuori dall'edificio");
                    break;
            }

            completata = verificaMissione.get();

            if (completata) {
                missioneCorrente.setCompletata(true);
                dbManager.aggiornaMissioneCompletata(missioneCorrente.getId(), true);
                missioneCorrenteIndex++;
                aggiornaMissioneVisualizzata();
                dialoghiTextArea.append(linguaCorrenteId == 1 ? "Missione completata: " : "Mission completed: " + missioneCorrente.getNome() + "\n");
            }
        }
    }

    /**
     * Verifica se tutti gli ingredienti necessari per creare il siero sono
     * presenti nell'inventario.
     *
     * @return true se tutti gli ingredienti sono presenti, false altrimenti
     */
    private boolean verificaIngredientiSiero() {
        Set<String> ingredientiTrovati = new HashSet<>();
        String[] currentIngredienti = INGREDIENTI_SIERO.get(linguaCorrenteId);
        for (GameObject obj : inventoryObjects) {
            for (String ingrediente : currentIngredienti) {
                if (obj.getName().equalsIgnoreCase(ingrediente)) {
                    ingredientiTrovati.add(ingrediente.toLowerCase());
                }
            }
        }
        boolean tuttiTrovati = ingredientiTrovati.size() == currentIngredienti.length;
        return tuttiTrovati;
    }

    /**
     * Crea e mostra l'interfaccia grafica principale del gioco.
     */
    public static void createAndShowGUI() {
        mainFrame = new JFrame("Apocalisse Zombie: Epidemia");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon("image\\icona.png");
        mainFrame.setIconImage(icon.getImage());
        mainFrame.setSize(1280, 720);
        mainFrame.setContentPane(new GameGUI());
        mainFrame.setVisible(true);

    }

    /**
     * Classe interna che rappresenta un pannello con un'immagine di sfondo.
     */
    class ImagePanel extends JPanel {

        private final Image image;

        public ImagePanel(String imagePath) {
            this.image = new ImageIcon(imagePath).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            g.drawImage(image, 0, 0, width, height, this);
        }
    }
}
