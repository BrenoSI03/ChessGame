package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal que representa o modelo do jogo de xadrez (ChessModel).
 * Controla o estado do tabuleiro, o turno atual e as regras básicas de movimentação e cheque.
 */
public class ChessModel implements ObservadoIF {
    private static ChessModel instance;
    private Board board;
    private boolean whiteTurn = true;
    private Position selectedPiecePos = null;
    private Position pendingPromotionPos = null;  // se != null, há promoção pendente
    private Position enPassantTarget = null; // Posição do peão que pode ser capturado por en passant (válido apenas no turno seguinte)
    private List<ObservadorIF> observadores = new ArrayList<>();

    // Construtor privado (padrão Singleton). Inicializa o tabuleiro com a configuração padrão.
    private ChessModel() {
        board = new Board(false);
    }

    // Retorna a instância única do modelo. Cria uma nova se ainda não existir.
    public static ChessModel getInstance() {
        if (instance == null) {
            instance = new ChessModel();
        }
        return instance;
    }

    // Reseta a instância atual do modelo (usado principalmente em testes).
    public static void resetInstance() {
        instance = null;
    }

    @Override
    public void addObservador(ObservadorIF o) {
        observadores.add(o);
    }

    @Override
    public void removeObservador(ObservadorIF o) {
        observadores.remove(o);
    }

    @Override
    public void notificarObservadores() {
        for (ObservadorIF o : observadores) {
            o.notificar(this);
        }
    }

    // Define um tabuleiro customizado. Usado para setups específicos ou testes.
    public void setBoard(Board customBoard) {
        this.board = customBoard;
        notificarObservadores();
    }

    // Retorna o tabuleiro atual do modelo.
    public Board getBoard() {
        return board;
    }

    // Seleciona uma peça com base nas coordenadas (linha e coluna).
    // Só permite selecionar se for uma peça da vez (branca ou preta conforme o turno).
    public boolean selectPiece(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece != null && piece.isWhite() == whiteTurn) {
            selectedPiecePos = new Position(row, col);
            return true;
        }
        return false;
    }

    // Tenta mover a peça selecionada para a casa de destino informada (linha e coluna).
    // Só realiza o movimento se for válido e se o rei não ficar em cheque após isso.
    public boolean selectTargetSquare(int row, int col) {
        if (selectedPiecePos == null) return false;

        Position target = new Position(row, col);
        Piece piece = board.getPiece(selectedPiecePos.row, selectedPiecePos.col);

        if (piece.isValidMove(selectedPiecePos, target, board)) {
            if (!canMoveToEscapeCheck(selectedPiecePos, target)) {
                return false;
            }

            // Trata movimento especial: en passant (remoção do peão capturado)
            if (piece instanceof Pawn) {
                if (target.equals(enPassantTarget) && board.isEmpty(target.row, target.col)) {
                    int capturedRow = whiteTurn ? target.row + 1 : target.row - 1;
                    board.setPiece(capturedRow, target.col, null); // Remove o peão capturado
                }
            }

            // Trata movimento especial: roque (movimenta a torre também)
            if (piece instanceof King && Math.abs(target.col - selectedPiecePos.col) == 2) {
                int rookCol   = (target.col > selectedPiecePos.col) ? 7 : 0;
                Position rook = new Position(selectedPiecePos.row, rookCol);

                // Valida sem alterar o jogo
                if (!attemptCastling(selectedPiecePos, rook)) return false;

                // Faz o movimento – primeiro rei, depois torre
                int rookTargetCol = (rookCol == 7) ? 5 : 3;
                board.movePiece(selectedPiecePos, target);
                board.movePiece(rook, new Position(selectedPiecePos.row, rookTargetCol));

                selectedPiecePos = null;
                pendingPromotionPos = null;
                enPassantTarget = null;
                whiteTurn = !whiteTurn;
                notificarObservadores();
                return true;
            }
            
            // Atualiza a posição de en passant, se for um peão que se moveu duas casas
            if (piece instanceof Pawn) {
                if (Math.abs(target.row - selectedPiecePos.row) == 2) {
                    enPassantTarget = new Position((target.row + selectedPiecePos.row) / 2, target.col);
                } else {
                    enPassantTarget = null; // Limpa se não for jogada válida para en passant
                }
            } else {
                enPassantTarget = null; // Limpa se não for um peão
            }

            // Verifica promoção pendente
            if (piece instanceof Pawn) {
                if ((piece.isWhite() && target.row == 0) || (!piece.isWhite() && target.row == 7)) {
                    board.movePiece(selectedPiecePos, target);  // Move peão para a última linha
                    pendingPromotionPos = target;
                    selectedPiecePos = null;
                    notificarObservadores();
                    return true;
                }
            }
            
            // Move a peça principal
            board.movePiece(selectedPiecePos, target);

            selectedPiecePos = null;
            whiteTurn = !whiteTurn;
            notificarObservadores();
            return true;
        }
        return false;
    }

    // Verifica se o rei da cor indicada está em cheque.
    // A função percorre o tabuleiro procurando por ameaças ao rei.
    public boolean isInCheck(boolean isWhite) {
        Position kingPos = findKingPosition(isWhite);
        if (kingPos == null) return false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.isWhite() != isWhite) {
                    Position from = new Position(row, col);
                    if (piece.isValidMove(from, kingPos, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Procura e retorna a posição do rei da cor especificada.
    // Retorna null se o rei não for encontrado (teoricamente nunca deve acontecer).
    private Position findKingPosition(boolean isWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece instanceof King && piece.isWhite() == isWhite) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    // Verifica se mover uma peça de uma posição para outra remove o rei do cheque.
    // Faz o movimento de forma temporária, verifica o estado, e depois desfaz tudo.
    public boolean canMoveToEscapeCheck(Position from, Position to) {
        Piece piece = board.getPiece(from.row, from.col);
        boolean isWhite = piece.isWhite();
        Piece captured = board.getPiece(to.row, to.col);
        boolean movedBefore = piece.hasMoved();
        boolean capturedMoved = captured != null && captured.hasMoved();

        board.movePiece(from, to);
        boolean stillInCheck = isInCheck(isWhite);
        board.movePiece(to, from);
        board.setPiece(to.row, to.col, captured);

        piece.setHasMoved(movedBefore);
        if (captured != null) captured.setHasMoved(capturedMoved);
        
        return !stillInCheck;
    }

    // Retorna verdadeiro se for a vez das peças brancas jogarem.
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public String getPieceCode(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece == null) return null;

        String cor = piece.isWhite() ? "w" : "b";
        String tipo = piece.getClass().getSimpleName().toLowerCase().substring(0, 1);

        if (piece.getClass().getSimpleName().equals("Knight")) {
            tipo = "n";
        }

        return cor + tipo;
    }

    public boolean isCheckMate() {
        boolean isWhite = whiteTurn;
        if (!isInCheck(isWhite)) {
            return false;
        }
        return !hasAnyLegalMove(isWhite);
    }

    public boolean isStalelMate() {
        boolean isWhite = whiteTurn;
        if (isInCheck(isWhite)) {
            return false;
        }
        return !hasAnyLegalMove(isWhite);
    }

    private boolean hasAnyLegalMove(boolean isWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.isWhite() == isWhite) {
                    Position from = new Position(row, col);
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            Position to = new Position(toRow, toCol);
                            if (piece.isValidMove(from, to, board)) {
                                if (canMoveToEscapeCheck(from, to)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean promotePawn(String pieceType) {
        if (pendingPromotionPos == null) return false;

        boolean isWhite = board.getPiece(pendingPromotionPos.row, pendingPromotionPos.col).isWhite();
        Piece newPiece;

        String type = pieceType.toLowerCase();

        if (type.equals("queen")) {
            newPiece = new Queen(isWhite);
        } else if (type.equals("rook")) {
            newPiece = new Rook(isWhite);
        } else if (type.equals("bishop")) {
            newPiece = new Bishop(isWhite);
        } else if (type.equals("knight")) {
            newPiece = new Knight(isWhite);
        } else {
            throw new IllegalArgumentException("Peça inválida para promoção: " + pieceType);
        }

        board.setPiece(pendingPromotionPos.row, pendingPromotionPos.col, newPiece);
        pendingPromotionPos = null;
        whiteTurn = !whiteTurn;
        notificarObservadores();
        return true;
    }

    public boolean hasPendingPromotion() {
        return pendingPromotionPos != null;
    }

    public List<int[]> getValidMovesForPiece(int row, int col) {
        List<int[]> validMoves = new ArrayList<>();

        Piece piece = board.getPiece(row, col);
        if (piece == null || piece.isWhite() != whiteTurn) {
            return validMoves;
        }

        Position from = new Position(row, col);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position to = new Position(r, c);
                if (piece.isValidMove(from, to, board) && canMoveToEscapeCheck(from, to)) {
                    validMoves.add(new int[]{r, c});
                }
            }
        }

        return validMoves;
    }
    
    public boolean attemptCastling(Position kingPos, Position rookPos) {
        Piece king = board.getPiece(kingPos.row, kingPos.col);
        Piece rook = board.getPiece(rookPos.row, rookPos.col);

        if (!(king instanceof King) || !(rook instanceof Rook)) return false;
        if (king.isWhite() != whiteTurn)                     return false;
        if (king.hasMoved() || rook.hasMoved())              return false;
        if (kingPos.row != rookPos.row)                      return false;

        int dir = (rookPos.col > kingPos.col) ? 1 : -1;

        for (int c = kingPos.col + dir; c != rookPos.col; c += dir)
            if (!board.isEmpty(kingPos.row, c)) return false;

        if (isInCheck(king.isWhite())) return false;

        for (int i = 1; i <= 2; i++) {
            Position step = new Position(kingPos.row, kingPos.col + i * dir);
            if (!canMoveToEscapeCheck(kingPos, step)) return false;
        }
        return true;
    }
    
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }
    
    public void setEnPassantTarget(Position pos) {
        this.enPassantTarget = pos;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
        notificarObservadores();
    }

    public String generateFEN() {
        StringBuilder fen = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;

            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);

                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }

                    char symbol = getFENSymbol(piece);
                    fen.append(symbol);
                }
            }

            if (emptyCount > 0) {
                fen.append(emptyCount);
            }

            if (row < 7) {
                fen.append('/');
            }
        }

        fen.append(' ');
        fen.append(whiteTurn ? 'w' : 'b');
        fen.append(" - - 0 1");

        return fen.toString();
    }

    private char getFENSymbol(Piece piece) {
        char symbol;

        if (piece instanceof King) symbol = 'k';
        else if (piece instanceof Queen) symbol = 'q';
        else if (piece instanceof Rook) symbol = 'r';
        else if (piece instanceof Bishop) symbol = 'b';
        else if (piece instanceof Knight) symbol = 'n';
        else if (piece instanceof Pawn) symbol = 'p';
        else symbol = '?';

        return piece.isWhite() ? Character.toUpperCase(symbol) : symbol;
    }

    public void loadFEN(String fen) {
        String[] parts = fen.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("FEN inválido: " + fen);
        }

        String boardPart = parts[0];
        String turnPart = parts[1];

        board.clear();
        int row = 0, col = 0;

        for (char ch : boardPart.toCharArray()) {
            if (ch == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(ch)) {
                col += Character.getNumericValue(ch);
            } else {
                boolean isWhite = Character.isUpperCase(ch);
                Piece piece = switch (Character.toLowerCase(ch)) {
                    case 'k' -> new King(isWhite);
                    case 'q' -> new Queen(isWhite);
                    case 'r' -> new Rook(isWhite);
                    case 'b' -> new Bishop(isWhite);
                    case 'n' -> new Knight(isWhite);
                    case 'p' -> new Pawn(isWhite);
                    default -> throw new IllegalArgumentException("Peça desconhecida no FEN: " + ch);
                };
                board.setPiece(row, col, piece);
                col++;
            }
        }

        this.whiteTurn = turnPart.equals("w");
        notificarObservadores();
    }
}
