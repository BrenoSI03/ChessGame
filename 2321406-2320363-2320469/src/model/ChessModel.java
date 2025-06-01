package model;

import java.util.ArrayList;
import java.util.List;


/**
 * Classe principal que representa o modelo do jogo de xadrez (ChessModel).
 * Controla o estado do tabuleiro, o turno atual e as regras básicas de movimentação e cheque.
 */
public class ChessModel {
    private static ChessModel instance;
    private Board board;
    private boolean whiteTurn = true;
    private Position selectedPiecePos = null;
    private Position pendingPromotionPos = null;  // se != null, há promoção pendente


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

    // Define um tabuleiro customizado. Usado para setups específicos ou testes.
    public void setBoard(Board customBoard) {
        this.board = customBoard;
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

            board.movePiece(selectedPiecePos, target);

            // 🔁 Verifica promoção pendente
            if (piece instanceof Pawn) {
                if ((piece.isWhite() && target.row == 0) || (!piece.isWhite() && target.row == 7)) {
                    pendingPromotionPos = target;
                    selectedPiecePos = null;
                    return true; // movimento feito, mas promoção pendente
                }
            }

            selectedPiecePos = null;
            whiteTurn = !whiteTurn;
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

        board.movePiece(from, to);
        boolean stillInCheck = isInCheck(isWhite);
        board.movePiece(to, from);
        board.setPiece(to.row, to.col, captured);

        return !stillInCheck;
    }


    // Retorna verdadeiro se for a vez das peças brancas jogarem.
    public boolean isWhiteTurn() {
        return whiteTurn;
    }
    
    
    /**
     * Retorna o código da peça localizada em uma determinada posição do tabuleiro.
     * O código é composto por uma letra indicando a cor ('w' para branco, 'b' para preto)
     * seguida pela letra inicial do tipo da peça em minúsculo.
     * Exemplo: "wp" para peão branco, "bk" para rei preto.
     * A letra do cavalo (Knight) é representada por 'n' para evitar conflito com o rei (King).
     */
    public String getPieceCode(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece == null) return null;

        String cor = piece.isWhite() ? "w" : "b";
        String tipo = piece.getClass().getSimpleName().toLowerCase().substring(0, 1);

        // Corrige para 'n' no caso do Knight
        if (piece.getClass().getSimpleName().equals("Knight")) {
            tipo = "n";
        }

        return cor + tipo;
    }
    
    /**
     * Verifica se o jogador da vez está em situação de xeque-mate.
     * Para isso, primeiro verifica se o rei está em cheque.
     * Se estiver, e se não houver nenhum movimento legal que possa livrar o jogador do cheque,
     * então a partida terminou em xeque-mate.
     * */
    public boolean isCheckMate() {
        boolean isWhite = whiteTurn;
        if (!isInCheck(isWhite)) {
            return false;
        }
        return !hasAnyLegalMove(isWhite);
    }

    /**
     * Verifica se o jogador da vez está em situação de afogamento (stalemate).
     * Isso ocorre quando o jogador não está em cheque, mas também não possui
     * nenhum movimento legal possível — o que caracteriza um empate.
     */
    public boolean isStalelMate() {
        boolean isWhite = whiteTurn;
        if (isInCheck(isWhite)) {
            return false;
        }
        return !hasAnyLegalMove(isWhite);
    }

    /**
     * Verifica se o jogador da vez possui ao menos um movimento legal disponível.
     * Percorre todas as peças do jogador no tabuleiro, testando todos os movimentos possíveis.
     * Se existir ao menos um movimento válido que não coloque o próprio rei em cheque,
     * então o jogador ainda pode jogar.
     */
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
    
    /**
     * Realiza a promoção de um peão que alcançou a última linha do tabuleiro.
     * A nova peça é escolhida com base no tipo passado como argumento.
     * Após a promoção, a posição de promoção pendente é limpa e o turno muda para o outro jogador.
     * Caso o tipo de peça fornecido seja inválido, uma exceção é lançada.
     */
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
        whiteTurn = !whiteTurn; // troca o turno só após a promoção
        return true;
    }

    /**
     * Verifica se há uma promoção de peão pendente.
     * Isso indica que o jogador deve escolher uma peça para substituir o peão promovido.
     */
    public boolean hasPendingPromotion() {
        return pendingPromotionPos != null;
    }
    
    
    /**
     * Retorna a lista de movimentos válidos para uma peça em uma determinada posição.
     * Considera tanto as regras individuais de movimentação da peça quanto a necessidade
     * de o movimento não deixar o próprio rei em cheque.
     * Caso a posição não contenha uma peça válida da vez, a lista retornada estará vazia.
     */
    public List<Position> getValidMovesForPiece(Position from) {
        List<Position> validMoves = new ArrayList<>();

        Piece piece = board.getPiece(from.row, from.col);
        if (piece == null || piece.isWhite() != whiteTurn) {
            return validMoves; // vazio
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position to = new Position(row, col);
                if (piece.isValidMove(from, to, board) && canMoveToEscapeCheck(from, to)) {
                    validMoves.add(to);
                }
            }
        }

        return validMoves;
    }
    
    public List<Position> getValidMovesForPiece(Position from) {
        List<Position> validMoves = new ArrayList<>();
        Piece piece = board.getPiece(from.row, from.col);
        
        if (piece == null || piece.isWhite() != whiteTurn) {
            return validMoves;
        }
    
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position to = new Position(row, col);
                if (piece.isValidMove(from, to, board) && canMoveToEscapeCheck(from, to)) {
                    validMoves.add(to);
                }
            }
        }
        return validMoves;
    }
}
