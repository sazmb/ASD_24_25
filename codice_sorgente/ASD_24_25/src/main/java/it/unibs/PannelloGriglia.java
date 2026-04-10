package it.unibs;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class PannelloGriglia extends JPanel {
    private static final long serialVersionUID = 1L;

    private Griglia griglia;
    private Cella origine;
    private Cella destinazione;
    private int modalitaColorazioneCammino = 1; // 1=normale colora i landmark di un colore fisso,
                                                // 0= creo un percorso da cella a cella tramire linea nera

    private List<Cella> camminoOrdinato = new ArrayList<>();

    public PannelloGriglia(Griglia griglia) {
        this.griglia = griglia;

        setBackground(Color.WHITE);
    }

    /*
     * ============================================================
     * === 1) METODI PUBBLICI PER AGGIORNAMENTO DATI
     * ============================================================
     */

    public void aggiornaDati() {
        this.camminoOrdinato = null;
        repaint();
    }

    public void aggiornaCammino(List<Cella> cammino, int modalitaColorazioneCammino) {
        this.camminoOrdinato = cammino;
        this.modalitaColorazioneCammino = modalitaColorazioneCammino;
        repaint();
    }

    public Point cellaDaCoordinatePixel(int px, int py) {
        Graphics g = getGraphics();
        if (g == null)
            return null;

        InfoDisposizione infoProv = InfoDisposizione.creaProvvisorio(getWidth(), getHeight(), griglia, g);
        if (infoProv.dimCella <= 0)
            return null;

        Font fontAssi = getFontAssi(infoProv.dimCella);
        FontMetrics fmAssi = g.getFontMetrics(fontAssi);

        InfoDisposizione info = new InfoDisposizione(getWidth(), getHeight(), griglia, fmAssi);

        int col = (px - info.offsetX) / info.dimCella;
        int row = (py - info.offsetY) / info.dimCella;

        if (col < 0 || row < 0 ||
                col >= griglia.getColonne() ||
                row >= griglia.getRighe())
            return null;

        return new Point(col, row);
    }

    private Font getFontAssi(int dimCella) {
        // proporzionale alla cella, con soglia minima per leggibilità
        int size = Math.max(14, (int) (dimCella * 0.5));
        return new Font("SansSerif", Font.BOLD, size);
    }

    /*
     * ============================================================
     * === 3) PAINT COMPONENT
     * ============================================================
     */

    @Override
    protected void paintComponent(Graphics g) {

        origine = griglia.getOrigineDefault();
        destinazione = griglia.getDestinazioneDefault();
        super.paintComponent(g);

        // layout provvisorio
        InfoDisposizione infoProv = InfoDisposizione.creaProvvisorio(getWidth(), getHeight(), griglia, g);
        if (infoProv.dimCella <= 0)
            return;

        // font coerente con cella provvisoria
        Font fontAssi = getFontAssi(infoProv.dimCella);
        g.setFont(fontAssi);
        FontMetrics fmAssi = g.getFontMetrics(fontAssi);

        // layout finale con font corretto
        InfoDisposizione info = new InfoDisposizione(getWidth(), getHeight(), griglia, fmAssi);
        int dimCella = info.dimCella;
        int offsetX = info.offsetX;
        int offsetY = info.offsetY;

        // disegno etichette
        drawEtichetteColonne(g, fmAssi, info);
        drawEtichetteRighe(g, fmAssi, info);
        Cella[][] celle = griglia.getCelle();
        for (int y = 0; y < griglia.getRighe(); y++) {
            for (int x = 0; x < griglia.getColonne(); x++) {

                int px = offsetX + x * dimCella;
                int py = offsetY + y * dimCella;

                // sfondo (ostacolo o libero)
                if (celle[y][x].getTipo() == TipoCella.OSTACOLO) {
                    g.setColor(Color.BLUE.darker());
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(px, py, dimCella, dimCella);

                // contesto / complemento
                if (celle[y][x].getTipo() == TipoCella.CONTESTO) {
                    g.setColor(Color.GREEN.darker());
                    g.fillRect(px, py, dimCella, dimCella);
                } else if (celle[y][x].getTipo() == TipoCella.COMPLEMENTO) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(px, py, dimCella, dimCella);
                }

                // assi rispetto all'origine
                if (griglia.getOrigineDefault() != null && destinazione == null &&
                        (y == origine.getRiga() || x == origine.getColonna()) &&
                        celle[y][x].getTipo() != TipoCella.OSTACOLO) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(px, py, dimCella, dimCella);
                }

                // origine
                if (griglia.getOrigineDefault() != null && x == griglia.getOrigineDefault().getColonna()
                        && y == griglia.getOrigineDefault().getRiga()) {
                    g.setColor(Color.RED);
                    g.fillRect(px, py, dimCella, dimCella);
                    drawLetteraCella(g, "O", px, py, dimCella);
                }

                // destinazione
                if (griglia.getDestinazioneDefault() != null && x == griglia.getDestinazioneDefault().getColonna()
                        && y == griglia.getDestinazioneDefault().getRiga()) {
                    g.setColor(Color.RED);
                    g.fillRect(px, py, dimCella, dimCella);
                    drawLetteraCella(g, "D", px, py, dimCella);
                }

                // frontiera

                if (celle[y][x].isFrontiera()) {
                    g.setColor(Color.BLACK);
                    int pad = 2;
                    g.drawOval(px + pad, py + pad, dimCella - 2 * pad, dimCella - 2 * pad);
                }

                // bordo cella
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(px, py, dimCella, dimCella);
            }
        }

        /*
         * ------------------------------
         * 3. Disegno cammino UNA SOLA VOLTA
         * ------------------------------
         */

        Graphics2D g2 = (Graphics2D) g;
        drawCammino(g2, dimCella, offsetX, offsetY);
    }

    /*
     * ============================================================
     * === 4) METODI DI DISEGNO
     * ============================================================
     */

    private void drawEtichetteColonne(Graphics g, FontMetrics fm, InfoDisposizione info) {
        int dimCella = info.dimCella;

        // parametri estetici
        final int gapNumeroTacchetta = 2; // distanza tra numero e tacchetta
        final int taccaLen = 6; // lunghezza tacchetta

        for (int x = 0; x < griglia.getColonne(); x++) {
            if ((griglia.getColonne() > 25 || griglia.getRighe() > 25) && x % 5 != 0)
                continue;

            String txt = String.valueOf(x);
            int w = fm.stringWidth(txt);

            // numero: sopra la tacchetta
            int px = info.offsetX + x * dimCella + (dimCella - w) / 2;
            int py = info.offsetY - fm.getDescent() - (taccaLen + gapNumeroTacchetta);
            g.drawString(txt, px, py);

            // tacchetta: subito sopra la griglia
            int cx = info.offsetX + x * dimCella + dimCella / 2;
            int yTaccaTop = info.offsetY - taccaLen;
            int yTaccaBottom = info.offsetY - 1;
            g.drawLine(cx, yTaccaTop, cx, yTaccaBottom);
        }
    }

    private void drawEtichetteRighe(Graphics g, FontMetrics fm, InfoDisposizione info) {
        int dimCella = info.dimCella;

        // parametri estetici
        final int gapNumeroTacchetta = 4; // distanza tra numero e tacchetta
        final int taccaLen = 6; // lunghezza tacchetta

        for (int y = 0; y < griglia.getRighe(); y++) {
            if ((griglia.getRighe() > 25 || griglia.getColonne() > 25) && y % 5 != 0)
                continue;

            String txt = String.valueOf(y);

            // posizione verticale centrata nella cella
            int cy = info.offsetY + y * dimCella + dimCella / 2;
            int py = cy + (fm.getAscent() - fm.getDescent()) / 2;

            // numero: più a sinistra
            int pxNumero = info.offsetX - taccaLen - gapNumeroTacchetta - fm.stringWidth(txt);
            g.drawString(txt, pxNumero, py);

            // tacchetta: subito a destra del numero, prima della griglia
            int xTaccaLeft = info.offsetX - taccaLen;
            int xTaccaRight = info.offsetX - 2;
            g.drawLine(xTaccaLeft, cy, xTaccaRight, cy);
        }
    }

    private void drawLetteraCella(Graphics g, String lettera, int px, int py, int dim) {
        int size = Math.max(8, (int) (dim * 0.6));
        Font f = g.getFont().deriveFont((float) size);
        g.setFont(f);

        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(lettera);
        int h = fm.getAscent();

        int tx = px + (dim - w) / 2;
        int ty = py + (dim - h) / 2 + fm.getAscent();

        g.setColor(Color.WHITE);
        g.drawString(lettera, tx, ty);
    }

    private void drawCammino(Graphics2D g2, int dim, int offsetX, int offsetY) {
        if (camminoOrdinato == null || camminoOrdinato.size() < 2)
            return;

        // Se la modalità è 2, disegniamo anche le linee tra le celle
        if (modalitaColorazioneCammino == 0) {
            g2.setStroke(new BasicStroke(Math.max(2, dim / 5),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.BLACK);

            for (int i = 0; i < camminoOrdinato.size() - 1; i++) {
                Cella a = camminoOrdinato.get(i);
                Cella b = camminoOrdinato.get(i + 1);

                int ax = offsetX + a.getColonna() * dim + dim / 2;
                int ay = offsetY + a.getRiga() * dim + dim / 2;

                int bx = offsetX + b.getColonna() * dim + dim / 2;
                int by = offsetY + b.getRiga() * dim + dim / 2;

                g2.drawLine(ax, ay, bx, by);
            }
        }
        if (modalitaColorazioneCammino == 1 || modalitaColorazioneCammino == 0) {

            Font oldFont = g2.getFont();
            Color oldColor = g2.getColor();

            // Font size proportional to cell size
            Font font = new Font("Arial", Font.BOLD, Math.max(12, dim / 3));
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int contatoreLandmark = 1;
            // Prima disegniamo le celle numerate azzurre con numeri rossi
            for (int i = 1; i < camminoOrdinato.size() - 1; i++) {

                if (modalitaColorazioneCammino == 1
                        || (modalitaColorazioneCammino == 0 && camminoOrdinato.get(i).isLandMark())) {
                    Cella c = camminoOrdinato.get(i);

                    int x = offsetX + c.getColonna() * dim;
                    int y = offsetY + c.getRiga() * dim;

                    // Riempimento cella in azzurro
                    g2.setColor(Color.CYAN);
                    g2.fillRect(x, y, dim, dim);

                    // Bordo nero della cella
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, dim, dim);

                    // Numero centrato in rosso
                    String text = String.valueOf(contatoreLandmark);
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    int textX = x + (dim - textWidth) / 2;
                    int textY = y + (dim + textHeight) / 2 - 2;

                    // aggiorno contator landmark se c'era un landmark
                    contatoreLandmark++;
                    g2.setColor(Color.RED);
                    g2.drawString(text, textX, textY);
                }
            }

            g2.setFont(oldFont);
            g2.setColor(oldColor);
            return;
        }
    }

}