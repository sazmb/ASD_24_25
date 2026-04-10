package it.unibs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class FinestraGriglia extends JFrame {
    private static final long serialVersionUID = 1L;

    // Dati della griglia
    private Griglia griglia;

    // Componenti interfaccia utente
    private JButton pulsanteAvvio;
    private JButton pulsanteStop;
    private JLabel etichettaInfo;
    private PannelloGriglia pannelloGriglia;
    private JButton pulsanteSalva;

    // Worker per calcolo percorsi
    private LavoratorePercorso lavoratore;
    private final AtomicBoolean stopRichiesto;
    private ConfigurazioneAnalizzatore cfgAnalizzatore;
    private AgenteCamminoMinimo agenteCamMIn;

    public FinestraGriglia(Griglia griglia) {

        // TEMPORANEO: View e Controller insieme
        this.griglia = griglia;

        // auto-seleziona origine e destinazione se presenti
        // (se si è usata opzione di caricamentro da file)
//        System.out.println("Verifica origine/destinazione preimpostate...");

        int numeroRighe = griglia.getRighe();
        int numeroColonne = griglia.getColonne();

        cfgAnalizzatore = new ConfigurazioneAnalizzatore();
        agenteCamMIn = new AgenteCamminoMinimo();
        stopRichiesto = new AtomicBoolean(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        pannelloGriglia = new PannelloGriglia(griglia);
//        System.out.println("griglia disegnata" + griglia);
        add(pannelloGriglia, BorderLayout.CENTER);

        // Pannello legenda laterale
        JPanel pannelloLegenda = new JPanel();
        pannelloLegenda.setLayout(new BoxLayout(pannelloLegenda, BoxLayout.Y_AXIS));
        pannelloLegenda.setBorder(BorderFactory.createTitledBorder("Legenda"));

        // Spaziatura interna
        pannelloLegenda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Legenda"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Voci legenda
        pannelloLegenda.add(creaVoceLegenda(Color.WHITE, "Frontiera", true));
        pannelloLegenda.add(creaVoceLegenda(Color.WHITE, "Cella libera", false));
        pannelloLegenda.add(creaVoceLegenda(Color.CYAN, "Landmark", false));
        pannelloLegenda.add(creaVoceLegenda(Color.BLUE.darker(), "Ostacolo", false));
        pannelloLegenda.add(creaVoceLegenda(Color.GREEN.darker(), "Contesto", false));
        pannelloLegenda.add(creaVoceLegenda(Color.YELLOW, "Asse", false));
        pannelloLegenda.add(creaVoceLegenda(Color.ORANGE, "Complemento", false));
        pannelloLegenda.add(creaVoceLegenda(Color.RED, "Origine (O)", false));
        pannelloLegenda.add(creaVoceLegenda(Color.RED, "Destinazione (D)", false));

        add(pannelloLegenda, BorderLayout.EAST);

        // Pannello controlli inferiori
        JPanel pannelloControlli = new JPanel(new FlowLayout());
        pulsanteAvvio = new JButton("Avvia valutazione");
        pulsanteStop = new JButton("Termina valutazione");
        pulsanteSalva = new JButton("Salva JSON");
        pulsanteStop.setEnabled(false);
        // pulsanteSalva.setEnabled(false);

        pannelloControlli.add(pulsanteAvvio);
        pannelloControlli.add(pulsanteStop);
        pannelloControlli.add(pulsanteSalva);

        add(pannelloControlli, BorderLayout.SOUTH);

        // Etichetta superiore
        etichettaInfo = new JLabel("Clicca per selezionare Origine (O) e Destinazione (D)");
        etichettaInfo.setFont(new Font("Arial", Font.BOLD, 18));
        etichettaInfo.setForeground(Color.BLACK);
        etichettaInfo.setOpaque(true);
        etichettaInfo.setBackground(Color.LIGHT_GRAY);
        etichettaInfo.setHorizontalAlignment(SwingConstants.CENTER);
        add(etichettaInfo, BorderLayout.NORTH);

        setSize(900, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        // Listener per click del mouse sulla griglia
        pannelloGriglia.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                // Conversione pixel → cella usando il metodo del pannello
                Point p = pannelloGriglia.cellaDaCoordinatePixel(e.getX(), e.getY());
                if (p == null)
                    return; // click fuori dalla griglia

                int riga = p.y;
                int colonna = p.x;

                // se clicco una cella ostocolo o fuori dai margini
                if (!griglia.isNeiMargini(riga, colonna)
                        || griglia.getCella(riga, colonna).getTipo() == TipoCella.OSTACOLO)
                    return;

                // se ancora l'origine non è selezionata
                if (griglia.getOrigineDefault() == null) {
                    griglia.setOrigineDefault(new Cella(riga, colonna));
                    etichettaInfo.setText(
                            "Origine selezionata: " + griglia.getOrigineDefault() + ". Ora scegli Destinazione");
                    agenteCamMIn.calcolaCCF(griglia);

                }
                // se l'origine è selezionata ma non la destinazione
                else if (griglia.getDestinazioneDefault() == null) {
                    griglia.setDestinazioneDefault(new Cella(riga, colonna));
                    etichettaInfo.setText("Destinazione selezionata: " + griglia.getDestinazioneDefault());

                }
                // se entrambe sono selezionate resetto e scelgo nuova origine
                else {
                    // Reset: nuova origine
                    griglia.setOrigineDefault(new Cella(riga, colonna));
                    griglia.setDestinazioneDefault(null);
                    etichettaInfo.setText(
                            "Origine aggiornata: " + griglia.getOrigineDefault() + ". Ora scegli Destinazione");
                    agenteCamMIn.calcolaCCF(griglia);
                }

                // Aggiorna il pannello e ridisegna
                pannelloGriglia.aggiornaDati();
            }
        });

        pulsanteAvvio.addActionListener(e -> {
            // Controllo: origine e destinazione devono essere selezionate
            if (griglia.getOrigineDefault() == null || griglia.getDestinazioneDefault() == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Seleziona prima Origine e Destinazione",
                        "Errore di input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Pannello di dialogo per impostazioni avanzate
            JPanel pannelloDialogo = new JPanel(new BorderLayout(10, 10));
            pannelloDialogo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel pannelloInput = new JPanel(new GridLayout(6, 2, 10, 10));

            // --- Limite di tempo ---
            pannelloInput.add(new JLabel("Limite di tempo (ms):"));
            JTextField campoTempo = new JTextField(String.valueOf(cfgAnalizzatore.getLimiteTempoMs()));
            pannelloInput.add(campoTempo);

            // --- Riordina Frontiera ---
            pannelloInput.add(new JLabel("Riordina Frontiera:"));
            JCheckBox checkRiordina = new JCheckBox();
            checkRiordina.setSelected(cfgAnalizzatore.isRiordinaFrontiera());
            pannelloInput.add(checkRiordina);

            // --- Condizione Forte ---
            pannelloInput.add(new JLabel("Condizione Forte:"));
            JCheckBox checkCondForte = new JCheckBox();
            checkCondForte.setSelected(cfgAnalizzatore.getCondForte() == 1);
            pannelloInput.add(checkCondForte);

            // --- Memoria Sequenze ---
            pannelloInput.add(new JLabel("Usa Memoria Sequenze:"));
            JCheckBox checkMemSequenze = new JCheckBox();
            checkMemSequenze.setSelected(cfgAnalizzatore.getUsaMemoriaSequenze() == 1);
            pannelloInput.add(checkMemSequenze);

            // --- Versione Ottimizzata ---
            pannelloInput.add(new JLabel("Versione Ottimizzata:"));
            JCheckBox checkVersioneOttimizzata = new JCheckBox();
            checkVersioneOttimizzata.setSelected(cfgAnalizzatore.getUsaLunghezzaGlob() == 1);
            pannelloInput.add(checkVersioneOttimizzata);

            pannelloDialogo.add(pannelloInput, BorderLayout.CENTER);

            // Mostra il dialogo
            int risultato = JOptionPane.showConfirmDialog(
                    this,
                    pannelloDialogo,
                    "Impostazioni Algoritmo",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (risultato == JOptionPane.OK_OPTION) {
                // --- Gestione limite di tempo ---
                try {
                    cfgAnalizzatore.setLimiteTempoMs(Long.parseLong(campoTempo.getText()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Limite di tempo non valido, uso valore predefinito: " + cfgAnalizzatore.getLimiteTempoMs()
                                    + " ms.");
                }

                // --- Booleani aggiornati dai checkbox ---
                cfgAnalizzatore.setRiordinaFrontiera(checkRiordina.isSelected());
                cfgAnalizzatore.setCondForte(checkCondForte.isSelected() ? 1 : 0);
                cfgAnalizzatore.setUsaMemoriaSequenze(checkMemSequenze.isSelected() ? 1 : 0);
                cfgAnalizzatore.setUsaLunghezzaGlob(checkVersioneOttimizzata.isSelected() ? 1 : 0);

                // Reset del flag di stop
                stopRichiesto.set(false);

                // Avvio del worker per il calcolo del percorso
                lavoratore = new LavoratorePercorso(agenteCamMIn, griglia, cfgAnalizzatore);
                pulsanteAvvio.setEnabled(false);
                pulsanteStop.setEnabled(true);
                etichettaInfo.setText("Algoritmo in esecuzione…");
                lavoratore.execute();
            }
        });

        pulsanteStop.addActionListener(e -> {

            if (lavoratore != null) {

                // Imposta il flag di stop e interrompe il worker
                stopRichiesto.set(true);
                // lavoratore.cancel(true);

                // Aggiorna l’etichetta informativa
                etichettaInfo.setText("Interruzione in corso…");
            }
        });

        pulsanteSalva.addActionListener(e -> salvaGrigliaJson());

        if (griglia.getOrigineDefault() != null && this.griglia.getDestinazioneDefault() != null) {
            agenteCamMIn.calcolaCCF(griglia);
            pannelloGriglia.aggiornaDati();
        }

    }

    // Metodo di utilità per creare una voce della legenda
    private JPanel creaVoceLegenda(Color colore, String testo, boolean conCerchio) {
        JLabel box;
        if (conCerchio) {
            // JLabel personalizzato che disegna un cerchio
            box = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int pad = 2;
                    g.setColor(Color.BLACK);
                    g.drawOval(pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad);
                }
            };
        } else {
            box = new JLabel();
        }

        box.setOpaque(true);
        box.setBackground(colore);
        box.setPreferredSize(new Dimension(20, 20));
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel voce = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voce.add(box);
        voce.add(new JLabel(testo));
        return voce;
    }

    // Worker Swing per l’esecuzione del calcolo del cammino in background (=>
    // l'utente può fermare il processo senza problemi)
    private class LavoratorePercorso
            extends SwingWorker<RisultatoCammino, Void> {

        private final AgenteCamminoMinimo agente;
        private final Griglia griglia;
        private final ConfigurazioneAnalizzatore config;

        public LavoratorePercorso(
                AgenteCamminoMinimo agente,
                Griglia griglia,
                ConfigurazioneAnalizzatore config) {

            this.agente = agente;
            this.griglia = griglia;
            this.config = config;

        }

        @Override
        protected RisultatoCammino doInBackground() {
            return agente.eseguiCam(
                    griglia,
                    config,
                    () -> stopRichiesto.get());
        }

        @Override
        protected void done() {
            pulsanteAvvio.setEnabled(true);
            pulsanteStop.setEnabled(false);

            // questo try serve a intercettare errori relativi allo swing worker,
            // l'analizzatore anche se non termina
            // non restituisce un errore bensi il miglior risultato trovato finora, il flag
            // convergenza indica se il metodo ha
            // terminato correttamente l'esecuzione o è stato interrotto anticipatamente
            try {
                RisultatoCammino risultato = get();

                List<Cella> cammino = risultato.getSeq() != null ? risultato.getSeq() : new ArrayList<>();

                pannelloGriglia.aggiornaCammino(agenteCamMIn.ricostruisciCammino(cammino, griglia), 0);
                // pannelloGriglia.aggiornaCammino(cammino, 1);
                if (risultato.getConvergenza() == 1) {
                    etichettaInfo.setText("Esecuzione completata");
                    mostraRiepilogo(risultato, 2);
                } else if ((risultato.getTempoCalcolo()) >= cfgAnalizzatore.limiteTempoMs) {
                    etichettaInfo.setText("Esecuzione interrotta");
                    mostraRiepilogo(risultato, 1);
                } else {
                    etichettaInfo.setText("Esecuzione interrotta");
                    mostraRiepilogo(risultato, 0);
                }

            } catch (Exception e) {

                etichettaInfo.setText("Esecuzione interrotta");
                mostraRiepilogo(null, 0);
                e.printStackTrace();
            }
        }
    }

    // Mostra un dialogo riepilogativo al termine o all’interruzione dell’algoritmo
    private void mostraRiepilogo(RisultatoCammino risultato, int stato) {
        JDialog dialogo = new JDialog(this, "Finestra riepilogo", true);
        dialogo.setSize(400, 400);
        dialogo.setLayout(new BorderLayout());

        JTextArea areaTesto = new JTextArea();
        areaTesto.setEditable(false);

        if (stato == 0) {

            areaTesto.setText("ESITO:\nProcesso INTERROTTO dall'utente\n"); 

        } else if (stato == 1) {

            areaTesto.setText("ESITO:\nProcesso INTERROTTO per limiti di tempo\n"); 

        } else if (stato == 2) {
            areaTesto.setText("ESITO:\nProcesso COMPLETATO\n");
        }
        if (risultato != null) {
            areaTesto.append(risultato.stampa());
        }

        dialogo.add(new JScrollPane(areaTesto), BorderLayout.CENTER);

        // Pulsante SALVA
        JButton pulsanteSalva = new JButton("Salva in .txt");
        pulsanteSalva.addActionListener(ev -> {
            JFileChooser selettoreFile = new JFileChooser();
            if (selettoreFile.showSaveDialog(dialogo) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter out = new PrintWriter(selettoreFile.getSelectedFile())) {
                    out.print(areaTesto.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogo, "Errore durante il salvataggio: " + ex.getMessage());
                }
            }
        });

        JPanel pannelloInferiore = new JPanel();
        pannelloInferiore.add(pulsanteSalva);

        dialogo.add(pannelloInferiore, BorderLayout.SOUTH);

        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    // Metodo che salva la griglia in un file JSON
    private void salvaGrigliaJson() {
        if (griglia.getOrigineDefault() == null || griglia.getDestinazioneDefault() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleziona prima Origine e Destinazione",
                    "Errore di input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Converto la matrice in un oggetto Griglia
        Griglia g = griglia;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salva griglia come JSON");

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            // Aggiunge estensione .json se mancante
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }

            try {
                GrigliaIO.writeGriglia(g, file);
                JOptionPane.showMessageDialog(this,
                        "Griglia salvata con successo in:\n" + file.getAbsolutePath(),
                        "Salvataggio completato",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Errore durante il salvataggio del file.",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}