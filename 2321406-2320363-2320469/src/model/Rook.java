package model;

class Rook extends Piece implements Serializable{

    // Constrói uma torre branca ou preta
    public Rook(boolean isWhite) {
        super(isWhite);
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento for válido (reto, sem obstáculos e sem capturar peça aliada)
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        // Movimento deve ser estritamente horizontal ou vertical
        if (from.row != to.row && from.col != to.col) {
            return false;
        }

        int rowStep = Integer.compare(to.row, from.row);
        int colStep = Integer.compare(to.col, from.col);

        // Verifica se há peças no caminho até o destino
        int currentRow = from.row + rowStep;
        int currentCol = from.col + colStep;

        while (currentRow != to.row || currentCol != to.col) {
            if (!board.isEmpty(currentRow, currentCol)) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // Verifica se o destino está vazio ou contém peça adversária
        Piece destinationPiece = board.getPiece(to.row, to.col);
        return destinationPiece == null || destinationPiece.isWhite() != this.isWhite;
    }
}
