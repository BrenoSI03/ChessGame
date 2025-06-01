package model;

class Knight extends Piece implements Serializable{

    // Constrói um cavalo branco ou preto
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento for em "L" e o destino estiver vazio ou com peça adversária
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int dx = Math.abs(from.row - to.row);
        int dy = Math.abs(from.col - to.col);

        boolean isLShape = (dx == 2 && dy == 1) || (dx == 1 && dy == 2);

        if (isLShape) {
            Piece destinationPiece = board.getPiece(to.row, to.col);
            return destinationPiece == null || destinationPiece.isWhite() != this.isWhite;
        }

        return false;
    }
}
