package it.unibs;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PannelloGrigliaManuale extends PannelloGrigliaBase {

    private static final long serialVersionUID = 1L;

    private boolean painting = false;
    private boolean erasing = false;

    public PannelloGrigliaManuale(int rows, int cols) {
        super(new boolean[rows][cols]);
    }

    public PannelloGrigliaManuale(boolean[][] obstacles) {
        super(obstacles);
    }

    private int getRow(MouseEvent e) {
        int y = e.getY() - offsetY;
        return (y < 0) ? -1 : y / cellSize;
    }

    private int getCol(MouseEvent e) {
        int x = e.getX() - offsetX;
        return (x < 0) ? -1 : x / cellSize;
    }

    private boolean valid(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    {
        // blocco di inizializzazione, oppure metti questo codice nel costruttore
        MouseAdapter listener = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int r = getRow(e);
                int c = getCol(e);
                if (!valid(r, c))
                    return;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    obstacles[r][c] = true;
                    painting = true;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    obstacles[r][c] = false;
                    erasing = true;
                }

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                painting = false;
                erasing = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int r = getRow(e);
                int c = getCol(e);
                if (!valid(r, c))
                    return;

                if (painting)
                    obstacles[r][c] = true;
                if (erasing)
                    obstacles[r][c] = false;

                repaint();
            }
        };

        addMouseListener(listener);
        addMouseMotionListener(listener);
    }
}