package model;

class King extends Piece implements Serializable{

    // Constrói um rei branco ou preto
    public King(boolean isWhite) {
        super(isWhite);
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento for válido (1 casa em qualquer direção, sem capturar peça aliada)
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(from.row - to.row);
        int colDiff = Math.abs(from.col - to.col);

        if (rowDiff <= 1 && colDiff <= 1) {
            Piece destinationPiece = board.getPiece(to.row, to.col);
            return destinationPiece == null || destinationPiece.isWhite() != this.isWhite;
        }
        return false;
    }
}
