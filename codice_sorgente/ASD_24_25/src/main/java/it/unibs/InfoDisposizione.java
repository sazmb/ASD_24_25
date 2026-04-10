package it.unibs;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class InfoDisposizione {

    public final int dimCella;
    public final int offsetX;
    public final int offsetY;
    public final int spazioEtichetteRighe;
    public final int spazioEtichetteColonne;

    // Costruttore finale con font
    public InfoDisposizione(int larghezzaDisponibile,
            int altezzaDisponibile,
            Griglia griglia,
            FontMetrics fm) {
        if (griglia.getColonne() == 0 || griglia.getRighe() == 0) {
            dimCella = 0;
            offsetX = offsetY = 0;
            spazioEtichetteColonne = spazioEtichetteRighe = 0;
            return;
        }

        // spazio etichette calcolato dal font
        spazioEtichetteColonne = fm.getHeight() + 8;
        spazioEtichetteRighe = fm.stringWidth(String.valueOf(griglia.getRighe())) + 8;

        int larghezzaGrigliaMax = larghezzaDisponibile - spazioEtichetteRighe;
        int altezzaGrigliaMax = altezzaDisponibile - spazioEtichetteColonne;

        double cellW = (double) larghezzaGrigliaMax / griglia.getColonne();
        double cellH = (double) altezzaGrigliaMax / griglia.getRighe();

        dimCella = (int) Math.floor(Math.min(cellW, cellH));
        if (dimCella < 1) {
            offsetX = offsetY = 0;
            return;
        }

        int larghezzaGriglia = dimCella * griglia.getColonne();
        int altezzaGriglia = dimCella * griglia.getRighe();

        offsetX = (larghezzaDisponibile - (larghezzaGriglia + spazioEtichetteRighe)) / 2 + spazioEtichetteRighe;
        offsetY = (altezzaDisponibile - (altezzaGriglia + spazioEtichetteColonne)) / 2 + spazioEtichetteColonne;
    }

    // Layout provvisorio senza font (solo per stimare dimCella)
    public static InfoDisposizione creaProvvisorio(int larghezzaDisponibile,
            int altezzaDisponibile,
            Griglia griglia,
            Graphics g) {
        if (griglia.getColonne() == 0 || griglia.getRighe() == 0) {
            return new InfoDisposizione(0, 0, griglia, g.getFontMetrics(new Font("SansSerif", Font.PLAIN, 10)));
        }

        double cellW = (double) larghezzaDisponibile / griglia.getColonne();
        double cellH = (double) altezzaDisponibile / griglia.getRighe();
        int dimCella = (int) Math.floor(Math.min(cellW, cellH));

        Font fontProv = new Font("SansSerif", Font.PLAIN, Math.max(10, (int) (dimCella * 0.5)));
        FontMetrics fmProv = g.getFontMetrics(fontProv);

        return new InfoDisposizione(larghezzaDisponibile, altezzaDisponibile, griglia, fmProv);
    }
}