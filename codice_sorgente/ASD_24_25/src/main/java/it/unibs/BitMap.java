package it.unibs;

import java.util.BitSet;

public class BitMap {
    private int rows, cols;
    private BitSet bitset;

    public BitMap(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.bitset = new BitSet(rows * cols);
    }

    private int index(int r, int c) {
        return r * cols + c;
    }

    public void set(int r, int c, boolean value) {
        bitset.set(index(r, c), value);
    }

    public boolean get(int r, int c) {
        return bitset.get(index(r, c));
    }
}