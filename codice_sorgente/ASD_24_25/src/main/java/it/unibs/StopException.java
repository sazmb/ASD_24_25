package it.unibs;

public class StopException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    final RisultatoCammino partialResult;

    StopException(RisultatoCammino res) {
        this.partialResult = res;
    }
}