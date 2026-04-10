package it.unibs;

import java.util.Random;

public class Configurazione {

    public int righe;
    public int colonne;

    public long seed;
    public int numSemplici = 5;
    public int numAgglomerati = 3;
    public int numDiagonali = 1;
    public int numBarre = 1;
    public int numAnelli = 1;

    public double density;
    public TipoPattern tipoPattern;

    public Configurazione(long inputSeed) {

        // Mix input seed with current system time
    	if(inputSeed!=0)
    		this.seed = inputSeed;
    	else
    		this.seed = inputSeed ^ System.currentTimeMillis();
    	
        Random rnd = new Random(this.seed);

        // Base values
        int[] values = {
                numSemplici,
                numAgglomerati,
                numDiagonali,
                numBarre,
                numAnelli
        };

        // Add controlled randomness (-1, 0, +1)
        for (int i = 0; i < values.length; i++) {
            values[i] += rnd.nextInt(3) - 1;
            if (values[i] < 0)
                values[i] = 0;
        }

        // Normalize to sum = 10
        normalizeToTen(values);

        // Reassign
        numSemplici = values[0];
        numAgglomerati = values[1];
        numDiagonali = values[2];
        numBarre = values[3];
        numAnelli = values[4];
    }

    private void normalizeToTen(int[] values) {
        int sum = 0;
        for (int v : values)
            sum += v;

        // Avoid division by zero
        if (sum == 0) {
            values[0] = 10;
            return;
        }

        // Scale proportionally
        double scale = 10.0 / sum;
        int newSum = 0;

        for (int i = 0; i < values.length; i++) {
            values[i] = (int) Math.round(values[i] * scale);
            newSum += values[i];
        }

        // Fix rounding errors
        while (newSum != 10) {
            int idx = (newSum > 10) ? maxIndex(values) : minIndex(values);
            values[idx] += (newSum > 10) ? -1 : 1;
            newSum += (newSum > 10) ? -1 : 1;
        }
    }

    private int maxIndex(int[] a) {
        int idx = 0;
        for (int i = 1; i < a.length; i++)
            if (a[i] > a[idx])
                idx = i;
        return idx;
    }

    private int minIndex(int[] a) {
        int idx = 0;
        for (int i = 1; i < a.length; i++)
            if (a[i] < a[idx])
                idx = i;
        return idx;
    }
}
