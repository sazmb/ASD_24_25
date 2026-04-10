package it.unibs;
import java.io.File;
import java.io.IOException;


import java.util.Scanner;

import javax.swing.SwingUtilities;

public class GrigliaController {
    private static final String OUTPUT_DIR = "benchmark3";

    public GrigliaController(int modalita, java.util.List<Griglia> griglie) {
        if (modalita == 1) // grafica
        {
            SwingUtilities.invokeLater(() -> {

                CreatoreGriglia creator = new CreatoreGriglia();
                creator.setVisible(true);

            });
        } else if (modalita == 0) // batch
        {
            GriglieTester tester = new GriglieTester(griglie);
            Scanner scanner = new Scanner(System.in);

            boolean continua = true;

            while (continua) {
                tester.askParameters();
                tester.runTests();

                System.out.print("\n\n\nVuoi eseguire una nuova analisi (con nuovi parametri)? (s/n): ");
                String risposta = scanner.nextLine().trim().toLowerCase();

                if (!risposta.equals("s")) {
                    continua = false;
                }
            }

            tester.writeCsvReport();
        } else if (modalita == 2) {
            GriglieTester tester = new GriglieTester(griglie);

            // fixed time
            long fixedTime = 60000;

            // list of flag combinations
            String[] flagCombinations = {
                    "-----", "R----", "-C---", "---V-", "--M--", "RC---", "R--V-", "RCM--", "R-MV-"
            };

            for (String flags : flagCombinations) {

                // --- Set fixed time ---
                tester.getConfigAnalizzatore().setLimiteTempoMs(fixedTime);

                // --- Set boolean flags ---
                // Pad to 5 characters just in case
                flags = String.format("%-5s", flags).replace(' ', '-');

                tester.getConfigAnalizzatore().setRiordinaFrontiera(flags.charAt(0) == 'R');
                tester.getConfigAnalizzatore().setCondForte(flags.charAt(1) == 'C' ? 1 : 0);
                tester.getConfigAnalizzatore().setUsaMemoriaSequenze(flags.charAt(2) == 'M' ? 1 : 0);
                tester.getConfigAnalizzatore().setUsaLunghezzaGlob(flags.charAt(3) == 'V' ? 1 : 0);
                tester.setAnalisiSpazio(flags.charAt(4) == 'S' ? 1 : 0);

                // optional: print parameters for verification
                System.out.println("\nRunning test with flags: " + flags);
                System.out.println("- Limite tempo: " + tester.getConfigAnalizzatore().getLimiteTempoMs() + " ms");
                System.out.println("- Riordina Frontiera: " + tester.getConfigAnalizzatore().isRiordinaFrontiera());
                System.out.println("- Condizione Forte: " + tester.getConfigAnalizzatore().getCondForte());
                System.out.println("- Memoria Sequenze: " + tester.getConfigAnalizzatore().getUsaMemoriaSequenze());
                System.out.println(
                        "- Controllo lunghezza globale: " + tester.getConfigAnalizzatore().getUsaLunghezzaGlob());
                System.out.println("- Analisi Spazio: " + (tester.getAnalisiSpazio() == 1 ? "si" : "no"));

                // --- Run the test ---
                tester.runTests();
            }

            // --- After all combinations ---
            tester.writeCsvReport();

        } else if(modalita==3){
            int[][] dimensioni = {
            {500,500},    
        };
            
       generaESalvaGriglieBenchmark(dimensioni);

        }

    }

    public static void generaESalvaGriglieBenchmark(int[][] dimensioniGriglie) {

        File dir = new File(OUTPUT_DIR);
        dir.mkdirs();

        for (int[] dim : dimensioniGriglie) {

            int righe = dim[0];
            int colonne = dim[1];

            // 1. Crea griglia
            Griglia griglia = new Griglia(righe, colonne, TipoPattern.SCACCHI);

            boolean[][] ostacoli = new boolean[righe][colonne];
            Configurazione config = new Configurazione(0);
            config.tipoPattern = TipoPattern.SCACCHI;
            config.righe=righe;
            config.colonne=colonne;

            GeneratoreGriglieOstacoli gen =
                    new GeneratoreGriglieOstacoli(config, ostacoli);
            gen.generaTutto();
            
            griglia.setOstacoli(ostacoli);

            // 2. Origine e destinazione FISSE
            Cella origine = new Cella(0, 1, TipoCella.LIBERA);
            Cella destinazione =(righe%2==0) ? new Cella(righe - 1, 0, TipoCella.LIBERA):new Cella(righe - 1, 1, TipoCella.LIBERA);

            griglia.setOrigineDefault(origine);
            griglia.setDestinazioneDefault(destinazione);

            // 3. Salvataggio automatico (riusa il tuo sistema)
            File file = new File(
                    OUTPUT_DIR + "/griglia_" + righe + "x" + colonne + ".json");

            try {
                GrigliaIO.writeGriglia(griglia, file);
            } catch (IOException e) {
                System.err.println(
                        "Errore nel salvataggio della griglia " +
                        righe + "x" + colonne);
                e.printStackTrace();
            }
        }
    }
}

