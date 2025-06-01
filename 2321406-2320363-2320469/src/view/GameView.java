package view;

import model.ChessModel;
import model.Position;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameView extends JPanel {
    public static final int TILE_SIZE = 80;
    public static final int BOARD_SIZE = 8;

    private ChessModel model;
    private Image[] images;
    private String[] codes = {"bp", "br", "bn", "bb", "bq", "bk", "wp", "wr", "wn", "wb", "wq", "wk"};
    private Color whiteTile = new Color(51, 74, 75); 
    private Color blackTile = new Color(59, 4, 30);
    private Color highlightColor = new Color(255, 200, 0, 100);
    private List<Position> highlightedPositions = new ArrayList<>();

    public GameView(ChessModel model) {
        this.model = model;
        this.images = new Image[codes.length];
        setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));
        loadImages();
    }

    private void loadImages() {
        for (int i = 0; i < codes.length; i++) {
            try {
                images[i] = ImageIO.read(getClass().getResource("/images/" + codes[i] + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Erro ao carregar imagem " + codes[i] + ": " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Desenha o tabuleiro
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isWhite = (row + col) % 2 == 0;
                g2.setColor(isWhite ? whiteTile : blackTile);
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Desenha os highlights
                if (isPositionHighlighted(row, col)) {
                    g2.setColor(highlightColor);
                    g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                // Desenha as peças
                String code = model.getPieceCode(row, col);
                int index = indexOfCode(code);
                if (index >= 0 && images[index] != null) {
                    g2.drawImage(images[index], col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }

    private boolean isPositionHighlighted(int row, int col) {
        for (Position pos : highlightedPositions) {
            if (pos.row == row && pos.col == col) {
                return true;
            }
        }
        return false;
    }

    private int indexOfCode(String code) {
        if (code == null) return -1;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(code)) return i;
        }
        return -1;
    }

    public void setHighlightedPositions(List<Position> positions) {
        highlightedPositions = new ArrayList<>(positions);
    }

    public void clearHighlights() {
        highlightedPositions.clear();
    }

    public void setModel(ChessModel model) {
        this.model = model;
        repaint();
    }
}
