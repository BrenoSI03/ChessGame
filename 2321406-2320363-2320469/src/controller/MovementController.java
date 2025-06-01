package controller;

import model.ChessModel;
import model.Position;
import view.GameView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MovementController {
    private final ChessModel model;
    private final GameView view;
    private Position selectedPiece = null;

    public MovementController(ChessModel model, GameView view) {
        this.model = model;
        this.view = view;
        setupMouseListeners();
    }

    private void setupMouseListeners() {
        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / GameView.TILE_SIZE;
                int row = e.getY() / GameView.TILE_SIZE;
                
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick();
                    return;
                }
                
                handleBoardClick(row, col);
            }
        });
    }

    private void handleRightClick() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Partida em FEN");
        
        if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(model.toFEN());
                JOptionPane.showMessageDialog(view, 
                    "Partida salva como:\n" + model.toFEN(), 
                    "Salvo com sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, 
                    "Erro ao salvar: " + ex.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleBoardClick(int row, int col) {
        if (selectedPiece == null) {
            handlePieceSelection(row, col);
        } else {
            handleMovementAttempt(row, col);
        }
        view.repaint();
    }

    private void handlePieceSelection(int row, int col) {
        if (model.selectPiece(row, col)) {
            selectedPiece = new Position(row, col);
            highlightValidMoves();
        }
    }

    private void handleMovementAttempt(int row, int col) {
        if (model.selectTargetSquare(row, col)) {
            handleSuccessfulMove(row, col);
        } else {
            handleFailedMove(row, col);
        }
    }

    private void handleSuccessfulMove(int row, int col) {
        if (model.hasPendingPromotion()) {
            showPromotionPopup(row, col);
        }
        resetSelection();
    }

    private void handleFailedMove(int row, int col) {
        resetSelection();
        if (model.selectPiece(row, col)) {
            selectedPiece = new Position(row, col);
            highlightValidMoves();
        }
    }

    private void highlightValidMoves() {
        view.setHighlightedPositions(model.getValidMovesForPiece(selectedPiece));
    }

    private void showPromotionPopup(int row, int col) {
        JPopupMenu popup = new JPopupMenu();
        String[] promotionPieces = {"Rainha", "Torre", "Bispo", "Cavalo"};
        
        for (String piece : promotionPieces) {
            JMenuItem item = new JMenuItem(piece);
            item.addActionListener(e -> {
                model.promotePawn(piece.toLowerCase());
                view.repaint();
            });
            popup.add(item);
        }
        
        popup.show(view, col * GameView.TILE_SIZE, row * GameView.TILE_SIZE);
    }

    private void resetSelection() {
        selectedPiece = null;
        view.clearHighlights();
    }
}
