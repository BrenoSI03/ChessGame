package model;

import java.io.Serializable;

abstract class Piece implements Serializable{
    protected boolean isWhite;

    // Construtor base para todas as peças, define se é branca ou preta
    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    // Retorna true se a peça for branca
    public boolean isWhite() {
        return isWhite;
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento for válido segundo as regras da peça
    public abstract boolean isValidMove(Position from, Position to, Board board);
}
