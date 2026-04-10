package it.unibs;

/* PSEUDOCODICE

INPUT: rows, cols, seed, config (percentuali/numeri per ogni ostacolo)
CREA grid[rows][cols] inizializzata a 0 (libera)
INIZIALIZZA Random con seed
PER ogni tipologia di ostacolo in una lista configurata:
    calcola quante istanze/probabilità usare in base a config e dimensione griglia
    per i=1..numero_istanze:
        prova a generare un'istanza valida (più tentativi)
        --- per ogni tipo: costruisci la forma (lista di celle)
        --- controlla che tutte le celle siano dentro griglia e libere
        --- applica la forma (setta a 1)
se richiesto, esegui postprocessing (es. assicurare connettività della area libera) -- opzionale
STAMPA/SALVA grid

STRATEGIE DI POSIZIONAMENTO:
 - SingleCell: scegli cella libera a caso
 - Agglomerato: scegli cella seed, espandi ortogonalmente fino alla dimensione scelta
 - Diagonale: scegli direzione diagonale e lunghezza, piazza celle in (r+i, c+i) o (r+i, c-i)
 - Barra: scegli lunghezza L e spessore S, scegli orientamento orizz/vert e posiziona rectangle
 - Anello: scegli centro e raggio (o half-width), scrivi perimetro di un rettangolo/cerchio
 - Blob (random walk): partire da seed e camminare aggiungendo nuove celle

VERIFICHE:
 - Controlla collisioni con celle già occupate
 - Evita di posizionare anelli se nasconderebbero completamente la griglia o non lascerebbero spazio interno
 - Limita tentativi -> se fallisce skip o riduci dimensione
 */

import java.util.Random;

public class GeneratoreGriglieOstacoli {
	private final Configurazione cfg;
	private final Random rnd;
	private int rows;
	private int cols;
	// private final Griglia griglia;
	private boolean[][] mat_ostacoli;

	public GeneratoreGriglieOstacoli(Configurazione cfg, boolean[][] mat_ostacoli) {
		this.cfg = cfg;
		this.rnd = new Random(cfg.seed);
		this.rows = cfg.righe;
		this.cols = cfg.colonne;
		// this.griglia = new Griglia(cfg.righe, cfg.colonne);
		this.mat_ostacoli = mat_ostacoli;
	}

	// Genera tutte le tipologie di ostacoli rispettando la densità specificata
	public void generaTutto() {
		switch (cfg.tipoPattern) {
			case DENSITA_BASSA, DENSITA_ALTA, DENSITA_MEDIA:
				generaOstacoliCasuali();
				break;
			case SPIRALE:
				generaSpirale();
				break;
			case SCACCHI:
				generaScacchiera();
				break;
			case RIGHE:
				generaRighe();
				break;
			case UNICO_OSTACOLO:
				generaUnicoOstacolo();
				break;
			default:
				generaOstacoliCasuali();
				break;
		}
	}

	// NON usato
	/*
	 * public void pulisciGriglia() {
	 * for (int r = 0; r < cfg.righe; r++) {
	 * for (int c = 0; c < cfg.colonne; c++) {
	 * griglia.getCella(r, c).setAttraversabile(true);
	 * }
	 * }
	 * }
	 */
	/*
	 * private void rimuoviOstacoliEccesso(double densitaAttuale) {
	 * double densitaTarget = cfg.density;
	 * int totaleCelle = rows * cols;
	 * int ostacoliDaRimuovere = (int) ((densitaAttuale - densitaTarget) *
	 * totaleCelle);
	 * 
	 * while (ostacoliDaRimuovere > 0) {
	 * int index=rnd.nextInt(griglia.getOstacoli().size());
	 * Cella ostacolo = griglia.getOstacoli().get(index);
	 * int r = ostacolo.getRiga();
	 * int c = ostacolo.getColonna();
	 * griglia.rimuoviOstacolo(r, c);;
	 * ostacoliDaRimuovere--;
	 * }
	 * }
	 */

	// ----------------- MAIN GENERATION LOGIC -----------------------
	private void generaOstacoliCasuali() {

		if (cfg.density <= 0)
			return;

		int totalCells = rows * cols;
		int targetCells = (int) Math.round(cfg.density * totalCells);

		// Ripartizione proporzionale in base ai numeri indicati in configurazione
		int totalShapes = cfg.numSemplici + cfg.numAgglomerati + cfg.numDiagonali +
				cfg.numBarre + cfg.numAnelli;

		// Evita divisioni per zero
		if (totalShapes == 0)
			return;

		// Budget proporzionale di celle
		int budgetSemplici = targetCells * cfg.numSemplici / totalShapes;
		int budgetAgglomerati = targetCells * cfg.numAgglomerati / totalShapes;
		int budgetDiagonali = targetCells * cfg.numDiagonali / totalShapes;
		int budgetBarre = targetCells * cfg.numBarre / totalShapes;
		int budgetAnelli = targetCells * cfg.numAnelli / totalShapes;

		// Genera ciascun tipo rispettando i budget
		generaSemplici(budgetSemplici);
		generaAgglomerati(budgetAgglomerati);
		generaDiagonali(budgetDiagonali);
		generaBarre(budgetBarre);
		generaAnelli(budgetAnelli);

//		System.out.println("Finito con densità: " + calcolaDensita());
	}

	// ----- TIPOLOGIE DI OSTACOLI -----

	// Genera singole celle
	private void generaSemplici(int budget) {
		int placed = 0;
		while (placed < budget) {
			int r = rnd.nextInt(rows);
			int c = rnd.nextInt(cols);

			if (!mat_ostacoli[r][c]) {
				mat_ostacoli[r][c] = true;
				placed++;
			}
		}
	}

	// Genera insiemi con 2, 3 o 4 celle
	private void generaAgglomerati(int budget) {
		int placed = 0;

		while (placed < budget) {

			int baseR = rnd.nextInt(rows);
			int baseC = rnd.nextInt(cols);
			int dim = 2 + rnd.nextInt(3); // 2–4 celle

			for (int j = 0; j < dim && placed < budget; j++) {
				int r = baseR + rnd.nextInt(2);
				int c = baseC + rnd.nextInt(2);

				if (controllaCoordinate(r, c) && !mat_ostacoli[r][c]) {
					mat_ostacoli[r][c] = true;
					placed++;
				}
			}
		}
	}

	// Genera insiemi con 2, 3 o 4 celle in diagonale
	private void generaDiagonali(int budget) {
		int placed = 0;

		while (placed < budget) {

			int startR = rnd.nextInt(rows);
			int startC = rnd.nextInt(cols);
			int length = 2 + rnd.nextInt(4); // lunghezza 2–5

			for (int i = 0; i < length && placed < budget; i++) {
				int r = startR + i;
				int c = startC + i;
				if (controllaCoordinate(r, c) && !mat_ostacoli[r][c]) {
					mat_ostacoli[r][c] = true;
					placed++;
				}
			}
		}
	}

	// Genera rettangoli con 3, 4, 5, 6 o 7 celle in lunghezza e con 1 o 2 celle in
	// spessore
	private void generaBarre(int budget) {
		int placed = 0;

		while (placed < budget) {

			boolean horizontal = rnd.nextBoolean();
			int startR = rnd.nextInt(rows);
			int startC = rnd.nextInt(cols);
			int length = 3 + rnd.nextInt(5); // 3–7 celle

			for (int i = 0; i < length && placed < budget; i++) {

				int r = startR + (horizontal ? 0 : i);
				int c = startC + (horizontal ? i : 0);

				if (controllaCoordinate(r, c) && !mat_ostacoli[r][c]) {
					mat_ostacoli[r][c] = true;
					placed++;
				}
			}
		}
	}

	// Genera anelli non attraversabili che racchiudono celle attraversabili
	private void generaAnelli(int budget) {
		int placed = 0;

		while (placed < budget) {

			int baseR = rnd.nextInt(rows - 2);
			int baseC = rnd.nextInt(cols - 2);

			// 4 celle dell'anello minimo 2x2
			int[][] coords = {
					{ baseR, baseC }, { baseR, baseC + 1 },
					{ baseR + 1, baseC }, { baseR + 1, baseC + 1 }
			};

			for (int[] cell : coords) {
				if (placed >= budget)
					break;

				int r = cell[0];
				int c = cell[1];

				if (!mat_ostacoli[r][c]) {
					mat_ostacoli[r][c] = true;
					placed++;
				}
			}
		}
	}

	private double calcolaDensita() {
		double count = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (mat_ostacoli[i][j] == true)
					count++;
			}
		}
		return count / (rows * cols);
	}

	/*
	 * Funzione per verificare se delle coordinarte indicano una cella valida
	 */
	public boolean controllaCoordinate(int row, int col) {
		if (row >= 0 && col >= 0 && row < rows && col < cols) {
			return true;
		}
		return false;
	}

	// ---- GRIGLIA CON PATTERN PREDEFINITI ----

	private void generaSpirale() {
		// Initially fill with true
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				mat_ostacoli[i][j] = true;
			}
		}

		int top = 0, bottom = rows - 1;
		int left = 0, right = cols - 1;
		boolean fillFalse = true;

		while (top <= bottom && left <= right) {
			if (fillFalse) {
				// Top row
				for (int j = ((left - 1) > 0 ? left - 1 : 0); j <= right; j++) {
					mat_ostacoli[top][j] = false;
				}
				top++;

				// Right column
				for (int i = top; i <= bottom; i++) {
					mat_ostacoli[i][right] = false;
				}
				right--;

				// Bottom row
				if (top <= bottom) {
					for (int j = right; j >= left; j--) {
						mat_ostacoli[bottom][j] = false;
					}
					bottom--;
				}

				// Left column
				if (left <= right) {
					for (int i = bottom; i > top; i--) {
						mat_ostacoli[i][left] = false;
					}
					left++;
				}
			} else {
				top++;
				right--;
				bottom--;
				left++;
			}

			fillFalse = !fillFalse; // alternate layer
		}
	}

	private void generaScacchiera() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				mat_ostacoli[i][j] = (i + j) % 2 == 0; // true su caselle pari
			}
		}
	}

	private void generaRighe() {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				mat_ostacoli[i][j] = !(i % 2 == 0); // true su righe dispari(la prima è 0)
			}
			if (i % 4 == 1) {
				mat_ostacoli[i][this.cols - 1] = false; // lascia un varco ogni 4 righe
			} else if (i % 4 == 3) {
				mat_ostacoli[i][0] = false; // lascia un varco ogni 4 righe
			}
		}
	}

	private void generaUnicoOstacolo() {
		int centerCol = cols / 2;

		for (int r = 0 / 2; r < rows - 1; r++) {
			mat_ostacoli[r][centerCol] = true;
		}
	}

	public boolean[][] getMat_ostacoli() {
		return mat_ostacoli;
	}
}