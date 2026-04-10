package it.unibs;

import jdk.jfr.*;

import java.nio.file.Path;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class GriglieTester {
    private final java.util.List<Griglia> griglie;
    private List<RisultatoCammino> risultati;
    private long limiteTempo = 3000; // ms
    private int analisiSpazio = 0; // 0 = no, 1 = si
    private AgenteCamminoMinimo agente;
    private ConfigurazioneAnalizzatore configAnalizzatore;
    private final AtomicBoolean stopRichiesto;
    StringBuilder sbCSV;
 
    public GriglieTester(java.util.List<Griglia> griglie) {
        this.griglie = griglie;
        this.risultati = new java.util.ArrayList<>();
        this.agente = new AgenteCamminoMinimo();
        this.configAnalizzatore = new ConfigurazioneAnalizzatore();
        stopRichiesto = new AtomicBoolean(false);
        this.sbCSV = new StringBuilder();

    }

    // Metodo principale per eseguire i test su tutte le griglie
    // prepara l'ambiente e richiama eseguiCammino per ogni griglia
    public void runTests() {
        risultati.clear();
        System.out.print("\nEsecuzione del test su " + griglie.size() + " griglie...");
        int contatore = 0;
        RisultatoCammino risultato;
        for(int i = 0; i < griglie.size(); i++) {
        	Griglia g = griglie.get(i);
            risultato = (analisiSpazio == 1) ? testGrigliaconSpazio(g, contatore) : testGriglia(g, i+1);
            RisultatoCammino risultatoCopia = new RisultatoCammino(risultato);

            risultati.add(risultatoCopia);
            contatore++;

        }
        buildCsvBuffer(risultati);
    }

    // Metodo eseguiCammino normale (con analisi spazio base)
    private RisultatoCammino testGriglia(Griglia g, int i) {
        System.out.print("\nTest della griglia " + i + " (" + g.getRighe() + "x" + g.getColonne() + ") con " + g.getTipologia());
        return eseguiCammino(g, limiteTempo);
    }

    // Metodo eseguiCammino con analisi spazio (JFR) (produce file .jfr (file che
    // mostra andamento memoria)
    // per ogni griglia testata, bisogna aprirli con jvisualvm o jmc)
    private RisultatoCammino testGrigliaconSpazio(Griglia g, int contatore) {
        System.out.println("Griglia di prova con modalità analizzatore spaziale");
        long limiteTempo = 3000; // ms
        Recording recording = new Recording();
        recording.enable("jdk.ObjectAllocationInNewTLAB"); // captures object allocations
        recording.enable("jdk.ObjectAllocationOutsideTLAB");
        recording.setToDisk(true);
        recording.setName("eseguiCamminoRecording");
        recording.start();

        // Call your method
        RisultatoCammino result = eseguiCammino(g, limiteTempo);

        // Stop recording
        recording.stop();

        // Create a unique file name using StringBuilder
        StringBuilder buffer = new StringBuilder();
        buffer.append("eseguiCammino_").append(contatore).append(".jfr");

        // Save the recording to a file

        Path recordingFile = Path.of(buffer.toString());
        try {
            recording.dump(recordingFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            recording.close();
        }

        System.out.println("Registrazione JFR salvata su: " + recordingFile);

        return result;
    }

    // Metodo eseguiCammino con analisi tempo e spazio(base-> prendo il picco di
    // memoria heap usata durante l'esecuzione)
    private RisultatoCammino eseguiCammino(Griglia g, long limiteTempoMs) {

        return agente.eseguiCam(g, configAnalizzatore, () -> stopRichiesto.get());
    }

    // Builds the CSV content (can be called multiple times)
    private StringBuilder buildCsvBuffer(List<RisultatoCammino> risultati) {

        for (RisultatoCammino r : risultati) {

            String landmarks = r.getSeq().stream()
                    .map(Cella::toString)
                    .collect(Collectors.joining("|"));

            sbCSV.append(String.format(
                    "%s,%d,%d,%s,%.3f,%d,%d,%.3f,%d,%d,%d,%d,%d,\"%s\"%n",
                    r.getModalita(),
                    r.getGriglia().getRighe(),
                    r.getGriglia().getColonne(),
                    r.getGriglia().getTipologia().toString(),
                    r.getDlib(),
                    r.getRaggiungibile(),
                    r.getConvergenza(),
                    r.getLunghezza(),
                    r.getTempoCalcolo(),
                    r.getIstr17Contatore(),
                    r.getContatoreFrontiera(),
                    r.getContatoreSalvataggi(),
                    r.getCrescitaMemoriaBytes(),
                    landmarks));

        }

        return sbCSV;
    }

    // Writes the final CSV report to disk
    public void writeCsvReport() {

        String fileName = "report.csv";

        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

            // CSV HEADER (written once)
            pw.println(
                    "modalita,righe,colonne,tipologia,d_lib,dest_ragg,convergenza,lunghezza,tempo_ms,istr16,cont_frontiera,nodi_salvati,memo_usata,landmarks");

            pw.print(sbCSV.toString());

            System.out.println("Rapporto CSV generato: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askParameters() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(
                    "\nInserisci limite di tempo per ogni test (ms) [Default: "
                            + configAnalizzatore.getLimiteTempoMs() + "]: ");

            String input = scanner.nextLine().trim();
            if (input.isEmpty())
                break;

            try {
                configAnalizzatore.setLimiteTempoMs(Long.parseLong(input));
                break;
            } catch (Exception e) {
                System.out.println("Valore non valido. Inserisci un numero > 0.");
            }
        }

        // --- Boolean options in one string ---
        System.out.println("\nConfigura le modalità usando UNA stringa di 4 caratteri.");
        System.out.println("Ogni posizione corrisponde a una modalità (ordine OBBLIGATORIO):");
        System.out.println();
        System.out.println("  1 2 3 4");
        System.out.println("  R C M V");
        System.out.println("  | | | |");
        System.out.println("  | | | |");
        System.out.println("  | | | +----> Controllo della lunghezza globale");
        System.out.println("  | | +------> Memorizzazione delle sequenze");
        System.out.println("  | +--------> Condizione forte");
        System.out.println("  +----------> Riordinamento della frontiera");
        System.out.println();
        System.out.println("Usa:");
        System.out.println("  - la LETTERA per attivare la modalità");
        System.out.println("  - '-' per disattivarla");
        System.out.println();
        System.out.println("Esempio: R-MV  (R,M,V attivi; C disattiva)");
        System.out.print("Stringa opzioni: ");
        String flags = scanner.nextLine().trim().toUpperCase();

        // Pad string to 4 chars in case user types less
        flags = String.format("%-4s", flags).replace(' ', '-');

        // Extract booleans
        configAnalizzatore.setRiordinaFrontiera(flags.charAt(0) == 'R');
        configAnalizzatore.setCondForte(flags.charAt(1) == 'C' ? 1 : 0);
        configAnalizzatore.setUsaMemoriaSequenze(flags.charAt(2) == 'M' ? 1 : 0);
        configAnalizzatore.setUsaLunghezzaGlob(flags.charAt(3) == 'V' ? 1 : 0);
        // analisiSpazio = (flags.charAt(4) == 'S') ? 1 : 0;

        System.out.println("\nParametri impostati:");
        System.out.println("- Limite tempo: " + configAnalizzatore.getLimiteTempoMs() + " ms");
        System.out.println(
                "\n- Riordinamento della frontiera: " + (configAnalizzatore.isRiordinaFrontiera() ? "sì" : "no"));
        System.out.println("- Condizione forte: " + (configAnalizzatore.getCondForte() == 1 ? "sì" : "no"));
        System.out.println(
                "- Memorizzazione delle sequenze: " + (configAnalizzatore.getUsaMemoriaSequenze() == 1 ? "sì" : "no"));
        System.out.println("- Controllo della lunghezza globale: "
                + (configAnalizzatore.getUsaLunghezzaGlob() == 1 ? "sì" : "no"));
        // System.out.println("- Analisi Spazio: " + (analisiSpazio == 1 ? "si" :
        // "no"));
    }

    public ConfigurazioneAnalizzatore getConfigAnalizzatore() {
        return configAnalizzatore;
    }

    public int getAnalisiSpazio() {
        return analisiSpazio;
    }

    public void setAnalisiSpazio(int analisiSpazio) {
        this.analisiSpazio = analisiSpazio;
    }
}
