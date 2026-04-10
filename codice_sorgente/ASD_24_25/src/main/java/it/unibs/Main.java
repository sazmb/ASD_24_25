package it.unibs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	public static void main(String[] args) {
		GrigliaController controller;

		// Controllo argomenti minimi
		if (args.length == 0) {
			System.out.println("Utilizzo corretto:");
			System.out.println("  java Main -b <directory>    (modalità batch)");
			System.out.println("  java Main -g                 (modalità grafica)");
			return;
		}

		String mode = args[0];

		switch (mode) {

			// ------------------------------
			// MODALITÀ BATCH (-b)
			// ------------------------------
			case "-b", "-t" -> {

				if (args.length != 2) {
					System.out.println("Errore: per -b serve il path della directory.");
					System.out.println("Utilizzo: java Main -b <directory>");
					return;
				}

				String dirPath = args[1];
				File directory = new File(dirPath);

				if (!directory.exists() || !directory.isDirectory()) {
					System.out.println("Errore: il path non è una directory valida.");
					return;
				}

				ObjectMapper mapper = new ObjectMapper();
				List<Griglia> tutteLeGriglie = new ArrayList<>();

				// Filtra solo i file .json
				File[] jsonFiles = directory.listFiles((d, name) -> name.endsWith(".json"));

				if (jsonFiles == null || jsonFiles.length == 0) {
					System.out.println("Nessun file JSON trovato nella directory.");
					return;
				}

				// Lettura dei JSON
				for (File file : jsonFiles) {
					try {
						// System.out.println("Carico: " + file.getName());

						// Controllo se contiene array o singolo oggetto
						if (fileContainsArray(mapper, file)) {

							List<Griglia> griglie = mapper.readValue(file, new TypeReference<List<Griglia>>() {
							});
							tutteLeGriglie.addAll(griglie);

						} else {
							Griglia g = mapper.readValue(file, Griglia.class);
							tutteLeGriglie.add(g);
						}

					} catch (IOException e) {
						System.out.println("Errore nel leggere " + file.getName() + ": " + e.getMessage());
					}
				}
				System.out.println("==============================");
				System.out.println("|   AVVIO MODALITA' CLI...   |");
				System.out.println("==============================\n");

				// Output finale
				System.out.println("GRIGLIE CARICATE: " + tutteLeGriglie.size() + "\n");
				for (int i = 0; i < tutteLeGriglie.size(); i++) {
					Griglia g = tutteLeGriglie.get(i);
					System.out.println("Griglia " + (i + 1) + ": " +
							g.getRighe() + "x" + g.getColonne());
				}
				if ("-t".equals(mode))
					controller = new GrigliaController(2, tutteLeGriglie);
				else
					controller = new GrigliaController(0, tutteLeGriglie);
			}

			// ------------------------------
			// MODALITÀ GRAFICA (-g)
			// ------------------------------
			case "-g" -> {
				System.out.println("==================================");
				System.out.println("|   AVVIO MODALITA' GRAFICA...   |");
				System.out.println("==================================\n");
				// passo null perchè in modalità grafica, è presente un'interfaccia per caricare
				// le griglie
				controller = new GrigliaController(1, null);

			}

			case "-gen"->{
				controller = new GrigliaController(3, null);
			}

			// ------------------------------
			// OPZIONE SCONOSCIUTA
			// ------------------------------
			default -> {
				System.out.println("Opzione non riconosciuta: " + mode);
				System.out.println("Usa -b <directory> oppure -g");
			}
		}
	}

	/**
	 * Controlla se il JSON inizia con '[' → array di griglie
	 */
	private static boolean fileContainsArray(ObjectMapper mapper, File file) throws IOException {
		String firstTrimmed = mapper.readTree(file).toString().trim();
		return firstTrimmed.startsWith("[");
	}
}