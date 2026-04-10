package it.unibs;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.function.Supplier;

public class AgenteCamminoMinimo {

    public AnalizCam analizCam;
    // private ConfigurazioneAlgoritmo configurazione; //non usata per ora

    public AgenteCamminoMinimo() {
        Griglia gDummy = new Griglia(1, 1, TipoPattern.PERSONALIZZATA);
        this.analizCam = new AnalizCam(gDummy);

    }

    public void calcolaCCF(Griglia g) {

        analizCam.calcolaCCF(g.getCelle(), g.getOrigineDefault());
    }

    /**
     * Esegue il calcolo del cammino minimo tra origine e destinazione sulla griglia
     * fornita,
     * rispettando un limite di tempo opzionale (in ms). Calcola anche tempo e
     * memoria utilizzata.
     */
    public RisultatoCammino eseguiCam(
            Griglia griglia,
            ConfigurazioneAnalizzatore config,
            Supplier<Boolean> stopCheck) {

        // System.out.println("valore del supplier: " + stopCheck.get());

        // System.out.println("Avvio calcolo cammino minimo con AnalizCam...");
        // System.out.println(config);

        analizCam.setConfig(config);
        analizCam.resetContatori(griglia.getRighe(), griglia.getColonne());

        if (config.getUsaMemoriaSequenze() == 1) {
            analizCam.inizializzaMemoriaSequenze(
                    griglia.getRighe(),
                    griglia.getColonne());
        }

        /* ================= MEMORY BASELINE ================= */

        HeapMonitor.prepare();
        long memoriaBaseline = HeapMonitor.heapUsed();

        /* ================= TIME START ================= */

        long inizioMs = System.currentTimeMillis();

        Supplier<Boolean> combinedStopCheck = () -> stopCheck.get() ||
                (config.getLimiteTempoMs() > 0 &&
                        (System.currentTimeMillis() - inizioMs) >= config.getLimiteTempoMs());

        RisultatoCammino risultato;

        try {
            risultato = analizCam.camminoMinOttimizzato(
                    griglia.getOrigineDefault(),
                    griglia.getDestinazioneDefault(),
                    griglia.getCelle(),
                    0,
                    combinedStopCheck);
        } catch (StopException e) {
            System.out.print(" --> limite di tempo superato");
            risultato = e.partialResult;
            risultato.setConvergenza(0);
        }

        /* ================= TIME END ================= */

        long fineMs = System.currentTimeMillis();

        /* ================= MEMORY MEASURE ================= */

        long memoriaPeak = HeapMonitor.heapPeak();
        long memoriaFinale = HeapMonitor.heapUsed();

        long crescitaPeakBytes = memoriaPeak - memoriaBaseline;
        long crescitaFinaleBytes = memoriaFinale - memoriaBaseline;

        /* ================= RESULT ================= */

        risultato.setGriglia(griglia);
        risultato.setOrigine(griglia.getOrigineDefault());
        risultato.setDestinazione(griglia.getDestinazioneDefault());
        // se ancora non è stato trovato un cammino valido ma faccio uno stop
        // anticipato(convergenza 0) la destinazione è considerata
        // raggiungibile
        if (risultato.getLunghezza() == Double.POSITIVE_INFINITY && risultato.getConvergenza() == 1) {
            risultato.setRaggiungibile(0);
        }
        
        if(risultato.getSeq().isEmpty()) risultato.setRaggiungibile(0);

        risultato.setTempo(fineMs - inizioMs);

        // memoria in KB
        risultato.setCrescitaMemoriaBytes(crescitaPeakBytes / 1024);

        // opzionale: utile per debug / analisi leak
        risultato.setCrescitaMemoriaBytes(crescitaFinaleBytes / 1024);

        risultato.setContatoreFrontiera(analizCam.getContatoreFrontiera());
        risultato.setIstr17Contatore(analizCam.getIstr16Contatore());
        risultato.setContatoreSalvataggi(analizCam.getContatorePercorsiSalvati());
        risultato.setDlib(AnalizCam.dlib(
                griglia.getOrigineDefault(),
                griglia.getDestinazioneDefault()));
        stampaModalita(risultato, config);
        return risultato;
    }

    public void stampaModalita(RisultatoCammino risultato, ConfigurazioneAnalizzatore config) {
        StringBuilder modalita = new StringBuilder();

        // Riordina Frontiera
        modalita.append(config.isRiordinaFrontiera() ? "R" : "-");

        // Condizione Forte
        modalita.append(config.getCondForte() == 1 ? "C" : "-");

        // Memoria Sequenze
        modalita.append(config.getUsaMemoriaSequenze() == 1 ? "M" : "-");

        // Versione Ottimizzata
        modalita.append(config.getUsaLunghezzaGlob() == 1 ? "V" : "-");

        // Scrive la stringa nel risultato
        risultato.setModalita(modalita.toString());
    }

    // Ricostruisce il cammino minimo tra start e goal sulla matrice di ostacoli
    // fornita
    // utile alla rappresentazione grafica
    public List<Cella> ricostruisciCammino(
            List<Cella> cammino,
            Griglia griglia) {
        if (cammino.isEmpty())
            return cammino;

        return analizCam.ricostruisciCamminoCompleto(griglia.getCelle(), cammino);
    }

    /*
     * Classe interna di utilità per il monitoraggio della memoria heap
     */
    public final class HeapMonitor {

        private HeapMonitor() {
        }

        public static void prepare() {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            resetHeapPeaks();
        }

        public static void resetHeapPeaks() {
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() == MemoryType.HEAP &&
                        pool.isUsageThresholdSupported()) {
                    pool.resetPeakUsage();
                }
            }
        }

        public static long heapUsed() {
            long used = 0;
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() == MemoryType.HEAP) {
                    used += pool.getUsage().getUsed();
                }
            }
            return used;
        }

        public static long heapPeak() {
            long peak = 0;
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() == MemoryType.HEAP) {
                    peak += pool.getPeakUsage().getUsed();
                }
            }
            return peak;
        }
    }

}
