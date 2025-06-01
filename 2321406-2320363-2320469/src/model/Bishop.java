package model;

class Bishop extends Piece implements Serializable{

    // Constrói um bispo branco ou preto
    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento do bispo for válido (diagonal, caminho livre, e sem capturar peça aliada)
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int dx = Math.abs(from.row - to.row);
        int dy = Math.abs(from.col - to.col);

        // Movimento deve ser na diagonal
        if (dx != dy) return false;

        int rowStep = (to.row > from.row) ? 1 : -1;
        int colStep = (to.col > from.col) ? 1 : -1;

        int currentRow = from.row + rowStep;
        int currentCol = from.col + colStep;

        // Verifica se há peças no caminho
        while (currentRow != to.row) {
            if (!board.isEmpty(currentRow, currentCol)) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // Permite o movimento se o destino estiver vazio ou ocupado por peça adversária
        Piece destinationPiece = board.getPiece(to.row, to.col);
        return destinationPiece == null || destinationPiece.isWhite() != this.isWhite;
    }
}
