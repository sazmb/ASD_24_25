package it.unibs;

import java.util.List;

public class RisultatoCammino {

	private double lunghezza;
	private List<Cella> seq;
	private Cella origine;
	private Cella destinazione;
	private long tempoCalcolo;
	private int istr17Contatore;
	private Griglia griglia;
	private int contatoreFrontiera;
	private int convergenza;// 1 se converge(ovvero metodo termina normalmente analizzando tutti i cammini
							// possibili), 0 se non-converge
	private int raggiungibile; // 1 se la destinazione è raggiungibile, 0 altrimenti
	private long crescitaMemoriaBytes;
	private double dlib;
	private int contatoreSalvataggi;
	private String modalita;

	public RisultatoCammino(double lunghezza, List<Cella> seq) {
		this.modalita = "-";
		this.lunghezza = lunghezza;
		this.seq = seq;
		this.origine = null;
		this.destinazione = null;
		this.tempoCalcolo = 0;
		this.istr17Contatore = 0;
		this.griglia = null;
		this.contatoreFrontiera = 0;
		this.convergenza = 1; // di default converge
		this.crescitaMemoriaBytes = 0;
		this.dlib = 0.0;
		this.contatoreSalvataggi = 0;
		this.raggiungibile = 1; // di default raggiungibile

	}

	// Costruttore di copia deep copy
	public RisultatoCammino(RisultatoCammino other) {
		this.lunghezza = other.lunghezza;

		// Deep copy della lista di Cella, se non null
		if (other.seq != null) {
			this.seq = new java.util.ArrayList<>();
			for (Cella c : other.seq) {
				this.seq.add(new Cella(c)); // Cella deve avere un costruttore di copia
			}
		} else {
			this.seq = null;
		}

		// Copia degli altri campi
		this.origine = (other.origine != null) ? new Cella(other.origine) : null;
		this.destinazione = (other.destinazione != null) ? new Cella(other.destinazione) : null;
		this.tempoCalcolo = other.tempoCalcolo;
		this.istr17Contatore = other.istr17Contatore;

		// ATTENZIONE: la Griglia potrebbe essere condivisa se è immutabile
		this.griglia = other.griglia; // shallow copy, se Griglia è mutabile bisogna fare deep copy

		this.contatoreFrontiera = other.contatoreFrontiera;
		this.convergenza = other.convergenza;
		this.crescitaMemoriaBytes = other.crescitaMemoriaBytes;
		this.dlib = other.dlib;
		this.contatoreSalvataggi = other.contatoreSalvataggi;
		this.raggiungibile = other.raggiungibile;
		this.modalita = other.modalita;
	}

	public double getLunghezza() {
		return lunghezza;
	}

	public List<Cella> getSeq() {
		return seq;
	}

	public void setTempo(long tempoCalcolo) {
		this.tempoCalcolo = tempoCalcolo;
	}

	public String stampa() {

	    StringBuilder sb = new StringBuilder();
	    sb.append(raggiungibile == 1 ? "(Destinazione raggiunta)\n\n" : "(Destinazione non raggiunta)\n\n");
	    
	    sb.append("INFORMAZIONI GENERALI:\n");
	    sb.append("- Dimensioni griglia: ").append(griglia.getRighe()).append(" x ").append(griglia.getColonne()).append("\n");
	    sb.append("- Tipologia griglia: ").append(griglia.getTipologia().toString()).append("\n");
	    sb.append("- Modalità di analisi: ").append(modalita).append("\n\n");
		
		sb.append("INFORMAZIONI MEMORIA:\n");
		sb.append("- Numero totale di nodi della frontiera analizzati: ").append(contatoreFrontiera).append("\n");
		sb.append("- Numero di salvataggi in memoria di nodi: ").append(contatoreSalvataggi).append("\n");
		sb.append("- Istruzioni di tipo 17 eseguite: ").append(istr17Contatore).append("\n");
		sb.append("- Crescita memoria heap: ").append(crescitaMemoriaBytes).append(" bytes\n\n");

		sb.append("INFORMAZIONI CAMMINO:\n");
		sb.append(convergenza == 1 ? "- Calcolo completo dei cammini possibili\n" : "- Calcolo parziale dei cammini possibili\n");
		sb.append("- Distanza libera: ").append(dlib).append("\n");
	    sb.append("- Lunghezza cammino: ").append(Math.round(lunghezza*100.0) / 100.0).append("\n");
		sb.append("- Tempo di calcolo: ").append(tempoCalcolo).append(" ms\n\n");
		
	    sb.append("Origine:\n").append(" - ").append("(" + origine +  ")");
	    
	    String tipo = "?";

		sb.append("\n\nSequenza dei landmark:\n");
		if (seq.size() != 0) { // seq.size() == 0 quando non esiste un cammino valido

			for (int i = 1; i < seq.size() - 1; i++) {
				sb.append(" - ").append(seq.get(i)).append("\n");
			}

			if (seq.size() != 1) { // seq.size() == 1 quando il calcolo è interrotto
				tipo = seq.get(seq.size() - 1).getTipo().name();

			}
		} else {
			sb.append("Non è stato trovato alcun cammino \nPerciò non è stato attraversato alcun landmark\n");
		}

		sb.append("\nDestinazione:\n").append(" - ").append("Cella(riga=" + destinazione.getRiga() + ", colonna=" + destinazione.getColonna() + ",  tipo=" + tipo + ")");

		return sb.toString();
	}

	public long getTempoCalcolo() {
		return tempoCalcolo;
	}

	public int getIstr17Contatore() {
		return istr17Contatore;
	}

	public Griglia getGriglia() {
		return griglia;
	}

	public int getContatoreFrontiera() {
		return contatoreFrontiera;
	}

	public int getConvergenza() {
		return convergenza;
	}

	public void setConvergenza(int convergenza) {
		this.convergenza = convergenza;
	}

	public void setCrescitaMemoriaBytes(long crescitaMemoriaBytes) {
		this.crescitaMemoriaBytes = crescitaMemoriaBytes;
	}

	public long getCrescitaMemoriaBytes() {
		return crescitaMemoriaBytes;
	}

	public void setLunghezza(double lunghezza) {
		this.lunghezza = lunghezza;
	}

	public void setSeq(List<Cella> seq) {
		this.seq = seq;
	}

	public void setOrigine(Cella origine) {
		this.origine = origine;
	}

	public void setDestinazione(Cella destinazione) {
		this.destinazione = destinazione;
	}

	public void setTempoCalcolo(long tempoCalcolo) {
		this.tempoCalcolo = tempoCalcolo;
	}

	public void setIstr17Contatore(int istr17Contatore) {
		this.istr17Contatore = istr17Contatore;
	}

	public void setGriglia(Griglia griglia) {
		this.griglia = griglia;
	}

	public void setContatoreFrontiera(int contatoreFrontiera) {
		this.contatoreFrontiera = contatoreFrontiera;
	}

	public void setDlib(double dlib) {
		this.dlib = dlib;
	}

	public double getDlib() {
		return dlib;
	}

	public void setContatoreSalvataggi(int contatoreSalvataggi) {
		this.contatoreSalvataggi = contatoreSalvataggi;
	}

	public int getContatoreSalvataggi() {
		return contatoreSalvataggi;
	}

	public int getRaggiungibile() {
		return raggiungibile;
	}

	public void setRaggiungibile(int raggiungibile) {
		this.raggiungibile = raggiungibile;
	}

	// setConvergenza(int) and setCrescitaMemoriaBytes(long) already exist
	public void setModalita(String modalita) {
		this.modalita = modalita;
	}

	public String getModalita() {
		return modalita;
	}
}