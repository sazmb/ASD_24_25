package it.unibs;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;

public class CreatoreGriglia extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField campoRighe, campoColonne, campoSeed;
    private JButton pulsanteAnteprima, pulsanteConferma, pulsanteCarica;
    private JComboBox<String> boxDensita;
    private JPanel pannelloGriglia;
    private PannelloGrigliaManuale pannelloManuale;
    private Griglia grigliaBackup;

    private int righe, colonne;
    private long seed = 0;

    public CreatoreGriglia() {

        setTitle("Creazione Griglia");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Pannello form
        JPanel pannelloForm = new JPanel(new GridBagLayout());
        pannelloForm.setBorder(new TitledBorder("Parametri griglia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Numero righe
        gbc.gridx = 0;
        gbc.gridy = 0;
        righe = colonne = 10;
        pannelloForm.add(new JLabel("Numero di righe:"), gbc);
        campoRighe = new JTextField("10", 10);
        gbc.gridx = 1;
        pannelloForm.add(campoRighe, gbc);

        // Numero colonne
        gbc.gridx = 0;
        gbc.gridy = 1;
        pannelloForm.add(new JLabel("Numero di colonne:"), gbc);
        campoColonne = new JTextField("10", 10);
        gbc.gridx = 1;
        pannelloForm.add(campoColonne, gbc);

        // Densità ostacoli
        gbc.gridx = 0;
        gbc.gridy = 2;
        pannelloForm.add(new JLabel("Densità ostacoli:"), gbc);
        boxDensita = new JComboBox<>(new String[] { "Bassa", "Media", "Alta", "Spirale", "Scacchi", "Righe",
                "Unico Ostacolo", "Personalizzata" });
        gbc.gridx = 1;
        pannelloForm.add(boxDensita, gbc);

        // Seed
        gbc.gridx = 0;
        gbc.gridy = 3;
        pannelloForm.add(new JLabel("Seme (0 = casuale):"), gbc);
        campoSeed = new JTextField("0", 10);
        gbc.gridx = 1;
        pannelloForm.add(campoSeed, gbc);

        // Pulsanti
        JPanel pannelloPulsanti = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pulsanteAnteprima = new JButton("Aggiorna anteprima");
        pulsanteConferma = new JButton("Conferma");
        // pulsanteSalva = new JButton("Salva JSON");
        pulsanteCarica = new JButton("Carica JSON");

        pannelloPulsanti.add(pulsanteAnteprima);
        pannelloPulsanti.add(pulsanteConferma);
        // pannelloPulsanti.add(pulsanteSalva);
        pannelloPulsanti.add(pulsanteCarica);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        pannelloForm.add(pannelloPulsanti, gbc);

        add(pannelloForm, BorderLayout.WEST);

        pannelloGriglia = new JPanel();
        pannelloGriglia.setBorder(new TitledBorder("Anteprima"));
        // pannelloGriglia.setPreferredSize(new Dimension(400, 400)); // quadrato
        add(pannelloGriglia, BorderLayout.CENTER);

        // Azioni
        pulsanteAnteprima.addActionListener(e -> {
            mostraAnteprima();
            pulsanteConferma.setEnabled(true);
            // pulsanteSalva.setEnabled(true);
            pulsanteCarica.setEnabled(true);
        });

        pulsanteConferma.addActionListener(e -> {
            if (leggiParametri())
                creaGriglia();
        });

        // pulsanteSalva.addActionListener(e -> salvaGrigliaJson());
        pulsanteCarica.addActionListener(e -> caricaGrigliaJson());

        disattivaConferma();
        mostraAnteprima();
    }

    // Se l'utente modifica un input, il pulsante di conferma si disattiva finché
    // non aggiorna l'anteprima
    public void disattivaConferma() {
        // --- LISTENER PER I CAMPI ---
        DocumentListener listenerCampi = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                pulsanteConferma.setEnabled(false);
                pulsanteCarica.setEnabled(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                pulsanteConferma.setEnabled(false);
                pulsanteCarica.setEnabled(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                pulsanteConferma.setEnabled(false);
                pulsanteCarica.setEnabled(false);
            }
        };

        // Aggiungo il listener ai JTextField
        campoRighe.getDocument().addDocumentListener(listenerCampi);
        campoColonne.getDocument().addDocumentListener(listenerCampi);
        campoSeed.getDocument().addDocumentListener(listenerCampi);

        // Se hai anche una JComboBox per la densità:
        boxDensita.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pulsanteConferma.setEnabled(false);

            }
        });
    }

    // Metodo che recupera i dati inseriti dall'utente
    private boolean leggiParametri() {
        try {
            righe = Integer.parseInt(campoRighe.getText().trim());
            colonne = Integer.parseInt(campoColonne.getText().trim());
            seed = Long.parseLong(campoSeed.getText().trim());
//            System.out.println("Parametri letti: righe=" + righe + ", colonne=" + colonne + ", seme=" + seed);
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Inserisci valori numerici validi per righe, colonne e seme", "Errore di input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Metodo che chiama il generatore degli ostacoli
    private boolean[][] generaOstacoli(TipoPattern tipo, double densita) {
        if (righe <= 0 || colonne <= 0) {
            JOptionPane.showMessageDialog(this, "La matrice deve avere un numero positivo di righe e e di colonne",
                    "Errore input",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        } else if (righe > 100 || colonne > 100) {
            JOptionPane.showMessageDialog(this, "La matrice non può avere più di 100 righe e 100 colonne",
                    "Errore input",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        boolean[][] ostacoli = new boolean[righe][colonne];
        Configurazione cfg = new Configurazione(seed);
        cfg.righe = righe;
        cfg.colonne = colonne;
        cfg.density = densita;
        cfg.tipoPattern = tipo;

        GeneratoreGriglieOstacoli generatore = new GeneratoreGriglieOstacoli(cfg, ostacoli);
        generatore.generaTutto();
        return ostacoli;
        // return generatore.getMat_ostacoli();
    }

    // Metodo che si occupa della griglia visualizzabile in anteprima
    private void mostraAnteprima() {
        String scelta = (String) boxDensita.getSelectedItem();
        leggiParametri();

        pannelloGriglia.removeAll();

        if ("Personalizzata".equals(scelta)) {
            boolean[][] vuota = new boolean[righe][colonne];

            // Salvo la griglia vuota
            aggiornaGrigliaBackup(new Griglia(righe, colonne, TipoPattern.PERSONALIZZATA));
            grigliaBackup.setOstacoli(vuota);

            // Creo subito pannello manuale
            pannelloManuale = new PannelloGrigliaManuale(righe, colonne);
            pannelloManuale.setOstacoli(vuota);

            pannelloGriglia.setLayout(new BorderLayout());
            pannelloGriglia.add(pannelloManuale, BorderLayout.CENTER);

        } else {
            double densita = 0.1;
            TipoPattern tipo = TipoPattern.DENSITA_BASSA;

            if ("Media".equals(scelta)) {
                tipo = TipoPattern.DENSITA_MEDIA;
                densita = 0.25;
            }
            if ("Alta".equals(scelta)) {
                tipo = TipoPattern.DENSITA_ALTA;
                densita = 0.45;
            }
            if ("Spirale".equals(scelta))
                tipo = TipoPattern.SPIRALE;
            if ("Scacchi".equals(scelta))
                tipo = TipoPattern.SCACCHI;
            if ("Righe".equals(scelta))
                tipo = TipoPattern.RIGHE;
            if ("Unico Ostacolo".equals(scelta))
                tipo = TipoPattern.UNICO_OSTACOLO;

            Griglia g = new Griglia(righe, colonne, tipo);
            boolean[][] ostacoli = generaOstacoli(tipo, densita);
            g.setOstacoli(ostacoli);
            if (ostacoli == null)
                return;

            // Salvo la griglia generata
            aggiornaGrigliaBackup(g);

            PannelloGrigliaBase anteprima = new PannelloGrigliaBase(ostacoli);
            pannelloGriglia.setLayout(new BorderLayout());
            pannelloGriglia.add(anteprima, BorderLayout.CENTER);
        }

        pannelloGriglia.revalidate();
        pannelloGriglia.repaint();
    }

    // Metodo che chiama il generatore di griglie
    private void creaGriglia2() {
        if (grigliaBackup != null) {
            new FinestraGriglia(grigliaBackup);
        } else {
            // if (ostacoli != null) {
            new FinestraGriglia(grigliaBackup);
            // }
        }
    }

    // METODO PROVA CHE CONTROLLA SE LA MATRICE E' FATTA SOLO DA OSTACOLI
    private void creaGriglia() {

        // piccolo aggiustamento per griglie personalizzate(passaggio da griglia di
        // booleani a griglia di Celle fatto manualmente)
        if (grigliaBackup.getTipologia() == TipoPattern.PERSONALIZZATA) {
            grigliaBackup.setOstacoli(grigliaBackup.getOstacoli());
        }

        if (!checkGrigliaSoloOstacoli())
//            System.out.println("Griglia creata: " + grigliaBackup);
        new FinestraGriglia(grigliaBackup);
    }

    private boolean checkGrigliaSoloOstacoli() {
        boolean[][] ostacoli;
        if (grigliaBackup != null) {
            ostacoli = grigliaBackup.getOstacoli();
        } else {
            ostacoli = null;
        }

        // Controllo: griglia composta solo da ostacoli
        boolean soloOstacoli = true;
        for (int i = 0; i < ostacoli.length; i++) {
            for (int j = 0; j < ostacoli[i].length; j++) {
                if (!ostacoli[i][j]) { // trovato almeno una cella libera
                    soloOstacoli = false;
                    break;
                }
            }
            if (!soloOstacoli)
                break;
        }

        if (soloOstacoli) {
            JOptionPane.showMessageDialog(this,
                    "La griglia generata contiene solo ostacoli.\n"
                            + "Non è possibile creare una griglia giocabile.",
                    "Errore griglia",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        } else {
            return false;
        }
    }

    // Metodo che si occupa delle griglie con ostacoli inseriti a mano dall'utente
    public void personalizzaOstacoli(boolean[][] ostacoli) {
        pannelloGriglia.removeAll();
        pannelloGriglia.setLayout(new BorderLayout());

        JPanel wrapperCentro = new JPanel(new GridBagLayout());
        pannelloManuale = new PannelloGrigliaManuale(righe, colonne);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;

        wrapperCentro.add(pannelloManuale, gbc);
        pannelloGriglia.add(wrapperCentro, BorderLayout.CENTER);
        pannelloGriglia.revalidate();
        pannelloGriglia.repaint();
    }

    private void caricaGrigliaJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Carica griglia da JSON");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            // Reads a list OR a single Griglia (your IO class handles both)
            java.util.List<Griglia> lista = GrigliaIO.readGriglie(file);

            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Il file non contiene griglie valide.",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Take first one (or ask user if multiple)
            Griglia g = lista.get(0);

            // --- VALIDATION: size must be <= 100 ---
            if (g.getRighe() > 100 || g.getColonne() > 100) {
                JOptionPane.showMessageDialog(this,
                        "La griglia caricata supera le dimensioni massime (100x100).",
                        "Errore dimensioni",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Store it as current backup
            aggiornaGrigliaBackup(g);

            // Update input fields
            campoRighe.setText(String.valueOf(g.getRighe()));
            campoColonne.setText(String.valueOf(g.getColonne()));
            campoSeed.setText("0");
            boxDensita.setSelectedItem("Bassa");

            // Show preview
            pannelloGriglia.removeAll();
            pannelloGriglia.setLayout(new BorderLayout());

            PannelloGrigliaBase anteprima = new PannelloGrigliaBase(g.getOstacoli());
            pannelloGriglia.add(anteprima, BorderLayout.CENTER);

            pannelloGriglia.revalidate();
            pannelloGriglia.repaint();

            pulsanteConferma.setEnabled(true);
            // pulsanteSalva.setEnabled(true);

            System.out.println("Griglia caricata con successo " + grigliaBackup);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Errore durante il caricamento del file JSON.",
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aggiornaGrigliaBackup(Griglia g) {
        grigliaBackup = g;
//        System.out.println("Griglia di backup aggiornata: " + grigliaBackup);
    }
}