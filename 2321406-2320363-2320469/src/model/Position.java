package model;

import java.io.Serializable;

// Representa uma posição no tabuleiro de xadrez (linha e coluna de 0 a 7)
class Position implements Serializable{

    public final int row;
    public final int col;

    // Constrói uma posição com linha e coluna informadas
    // Lança IllegalArgumentException se as coordenadas estiverem fora do tabuleiro (0 a 7)
    public Position(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            throw new IllegalArgumentException("Coordenadas inválidas: (" + row + ", " + col + ")");
        }
        this.row = row;
        this.col = col;
    }

    // Compara se duas posições são iguais (mesma linha e mesma coluna)
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.row == other.row && this.col == other.col;
    }
}
