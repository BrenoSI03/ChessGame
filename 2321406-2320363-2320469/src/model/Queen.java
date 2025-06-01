package model;

class Queen extends Piece implements Serializable{

    // Constrói uma rainha branca ou preta
    public Queen(boolean isWhite) {
        super(isWhite);
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento for válido (reto ou diagonal, com caminho livre e destino desocupado ou com peça adversária)
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(from.row - to.row);
        int colDiff = Math.abs(from.col - to.col);

        int rowStep = Integer.compare(to.row, from.row);
        int colStep = Integer.compare(to.col, from.col);

        // Verifica se o movimento é reto (horizontal/vertical) ou diagonal
        if (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) {

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

        return false;
    }
}
