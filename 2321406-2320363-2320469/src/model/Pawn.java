package model;

class Pawn extends Piece implements Serializable{

    // Constrói um peão branco ou preto
    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    // Recebe: posição inicial, posição final e o tabuleiro
    // Retorna: true se o movimento for válido (avanço simples, avanço duplo inicial ou captura diagonal)
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        // Movimento para frente (1 casa)
        if (from.col == to.col) {
            if (to.row == from.row + direction && board.isEmpty(to.row, to.col)) {
                return true;
            }
            // Movimento inicial (2 casas)
            if (from.row == startRow && to.row == from.row + 2 * direction
                && board.isEmpty(to.row, to.col)
                && board.isEmpty(from.row + direction, to.col)) {
                return true;
            }
        }

        // Captura diagonal
        if (Math.abs(from.col - to.col) == 1 && to.row == from.row + direction) {
            Piece target = board.getPiece(to.row, to.col);
            return target != null && target.isWhite() != isWhite;
        }

        return false;
    }
}
