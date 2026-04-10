package it.unibs;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class PannelloGrigliaBase extends JPanel {
    private static final long serialVersionUID = 1L;

    protected boolean[][] obstacles;
    protected int rows, cols;

    protected int cellSize;
    protected int offsetX, offsetY;

    public PannelloGrigliaBase(boolean[][] obstacles) {
        setOstacoli(obstacles);
        setBackground(Color.WHITE);
    }

    public void setOstacoli(boolean[][] obstacles) {
        this.obstacles = obstacles;
        if (obstacles != null) {
            this.rows = obstacles.length;
            this.cols = obstacles[0].length;
        }
        repaint();
    }

    // Calcolo layout
    protected void computeGridLayout() {
        cellSize = Math.min(getWidth() / cols, getHeight() / rows);
        offsetX = (getWidth() - cols * cellSize) / 2;
        offsetY = (getHeight() - rows * cellSize) / 2;
    }

    // Disegno griglia
    protected void drawGrid(Graphics g) {
        if (obstacles == null)
            return;

        computeGridLayout();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                int x = offsetX + c * cellSize;
                int y = offsetY + r * cellSize;

                g.setColor(obstacles[r][c] ? Color.BLUE.darker() : Color.WHITE);
                g.fillRect(x, y, cellSize, cellSize);

                g.setColor(Color.GRAY);
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
    }
}