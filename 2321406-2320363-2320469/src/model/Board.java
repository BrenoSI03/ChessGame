package model;

// Representa o tabuleiro de xadrez, composto por uma matriz 8x8 de peças.
class Board {
    private Piece[][] board;

    // Construtor. Se o parâmetro "empty" for falso, inicializa o tabuleiro com a posição padrão.
    public Board(boolean empty) {
        board = new Piece[8][8];
        if (!empty) {
            setupInitialPosition();
        }
    }

    // Preenche o tabuleiro com a configuração inicial padrão do xadrez.
    private void setupInitialPosition() {
        // Peões
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn(false); // pretos
            board[6][i] = new Pawn(true);  // brancos
        }

        // Torres
        board[0][0] = new Rook(false);
        board[0][7] = new Rook(false);
        board[7][0] = new Rook(true);
        board[7][7] = new Rook(true);

        // Cavalos
        board[0][1] = new Knight(false);
        board[0][6] = new Knight(false);
        board[7][1] = new Knight(true);
        board[7][6] = new Knight(true);

        // Bispos
        board[0][2] = new Bishop(false);
        board[0][5] = new Bishop(false);
        board[7][2] = new Bishop(true);
        board[7][5] = new Bishop(true);

        // Rainhas
        board[0][3] = new Queen(false);
        board[7][3] = new Queen(true);

        // Reis
        board[0][4] = new King(false);
        board[7][4] = new King(true);
    }

    // Remove todas as peças do tabuleiro, deixando todas as casas vazias.
    public void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }
    }

    // Retorna a peça presente na posição indicada (linha e coluna).
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    // Move uma peça da posição "from" para a posição "to".
    // A posição de origem fica vazia após o movimento.
    public void movePiece(Position from, Position to) {
        Piece piece = board[from.row][from.col];
        board[to.row][to.col] = piece;
        board[from.row][from.col] = null;
    }

    // Coloca uma peça na posição indicada (linha e coluna).
    // Substitui qualquer peça anterior naquela casa.
    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    // Retorna true se a casa na posição indicada estiver vazia.
    public boolean isEmpty(int row, int col) {
        return board[row][col] == null;
    }
    
    public String toFEN() {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(getFENSymbol(p));
                }
            }
            if (empty > 0) fen.append(empty);
            if (row < 7) fen.append("/");
        }
        return fen.toString();
    }
    
    private char getFENSymbol(Piece p) {
        char c = ' ';
        if (p instanceof Pawn) c = 'p';
        else if (p instanceof Rook) c = 'r';
        else if (p instanceof Knight) c = 'n';
        else if (p instanceof Bishop) c = 'b';
        else if (p instanceof Queen) c = 'q';
        else if (p instanceof King) c = 'k';
        return p.isWhite() ? Character.toUpperCase(c) : c;
    }
}
