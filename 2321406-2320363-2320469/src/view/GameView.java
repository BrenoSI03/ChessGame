package view;

import controller.ObservadoIF;
import controller.ObservadorIF;

import controller.GameController;
import model.ChessModel;
import model.ObservadorIF;
import model.ObservadoIF;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * Classe responsável pela visualização do tabuleiro de xadrez.
 * Renderiza o estado atual do ChessModel e trata interações do usuário via mouse.
 */
public class GameView extends JPanel {
    private static final int TILE_SIZE = 80; // Tamanho de cada casa do tabuleiro em pixels
    private static final int BOARD_SIZE = 8; // Tamanho do tabuleiro (8x8)

    private ChessModel model; 
    private GameController controller;           
    private Image[] images;
    private String[] codes = {"bp", "br", "bn", "bb", "bq", "bk", "wp", "wr", "wn", "wb", "wq", "wk"};
    private Color white = new Color(243, 233, 208);  
    private Color black = new Color(60, 25, 99); 
    private List<int[]> validMoves = new ArrayList<>(); // lista de arrays de posicoes validas
    private int selectedRow = -1;  // Linha da peça selecionada
    private int selectedCol = -1;  // Coluna da peça selecionada
    private boolean promotionMenuVisible = false;
    private ChessViewFacade facade;

    /**
     * Construtor que inicializa o painel gráfico do tabuleiro.
     * Define o tamanho, carrega imagens e configura eventos de clique.
     */
    public GameView(ChessModel model) {
        this.model = model;
        this.images = new Image[codes.length];
        this.facade = new ChessViewFacade(this);
        model.addObservador(facade);
        setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));
        loadImages(); // Carrega imagens das peças

        // Adiciona listener de mouse para tratar seleção e movimentação de peças
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getX(), e.getY());
                    return;
                }
                
                int col = e.getX() / TILE_SIZE;
                int row = e.getY() / TILE_SIZE;

                if (model.hasPendingPromotion()) {
                    showPromotionMenu(e.getX(), e.getY());
                    return;
                }

                if (model.selectPiece(row, col)) {
                    selectedRow = row;
                    selectedCol = col;
                    validMoves = model.getValidMovesForPiece(row, col);
                } else {
                    try {
                        if (model.selectTargetSquare(row, col)) {
                            if (model.hasPendingPromotion()) {
                                showPromotionMenu(e.getX(), e.getY());
                            } else {
                                selectedRow = -1;
                                selectedCol = -1;
                                if (controller != null) {
                                    controller.checkEndOfGame(); // Verifica se o jogo terminou
                                }
                            }
                        } else {
                            selectedRow = -1;
                            selectedCol = -1;
                        }
                    } catch (IllegalArgumentException ex) {
                        // Coordenadas inválidas, ignora o clique
                        return;
                    }
                    validMoves.clear();
                }
                repaint(); // Atualiza o painel
            }
        });
    }

    /**
     * Método chamado pelo facade para atualizar a view quando o modelo muda
     */
    public void updateView() {
        validMoves.clear();
        selectedRow = -1;
        selectedCol = -1;
        repaint();
    }

    // Define o controller
    public void setController(GameController controller) {
        this.controller = controller;
    }
    

    // Exibe o menu de promoção de peão ao chegar na última linha.
    private void showPromotionMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        for (String opt : options) {
            JMenuItem item = new JMenuItem(opt);
            item.addActionListener(e -> {
                if (controller != null) {
                    controller.setPawnPromotion(opt);
                }
                selectedRow = -1;
                selectedCol = -1;
                promotionMenuVisible = false;
            });
            menu.add(item);
        }
        promotionMenuVisible = true;
        menu.show(this, x, y);
    }

    /**
     * Carrega imagens das peças a partir dos recursos do projeto.
     */
    private void loadImages() {
        for (int i = 0; i < codes.length; i++) {
            try {
                images[i] = ImageIO.read(getClass().getResource("/images/" + codes[i] + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Erro ao carregar imagem " + codes[i] + ": " + e.getMessage());
                System.exit(1); // Encerra o programa em caso de erro crítico
            }
        }
    }

    /**
     * Redesenha o tabuleiro e as peças.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean color_type = (row + col) % 2 == 0;
                g2.setColor(color_type ? white : black);
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                String code = model.getPieceCode(row, col);
                int index = indexOfCode(code);
                if (index >= 0 && images[index] != null) {
                    g2.drawImage(images[index], col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }

        // Destaca os movimentos válidos da peça selecionada
        g2.setColor(new Color(128, 0, 0, 180));
        for (int[] move : validMoves) {
            g2.fillRect(move[1] * TILE_SIZE, move[0] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    /**
     * Retorna o índice da imagem correspondente a uma peça, dado seu código.
     */
    private int indexOfCode(String code) {
        if (code == null) return -1;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(code)) return i;
        }
        return -1;
    }

    /**
     * Permite atualizar o modelo associado ao painel.
     * @param model nova instância do modelo do jogo
     */
    public void setModel(ChessModel model) {
        if (this.model != null) {
            this.model.removeObservador(facade);
        }
        this.model = model;
        if (model != null) {
            model.addObservador(facade);
        }
        repaint();
    }
    
    private void showContextMenu(int x, int y) {
        if (promotionMenuVisible) {
            return;  // Não exibe se o menu de promoção estiver aberto
        }

        JPopupMenu menu = new JPopupMenu();
        JMenuItem salvarEVoltar = new JMenuItem("Salvar e Voltar");

        salvarEVoltar.addActionListener(e -> {
            if (controller != null) {
                boolean salvo = controller.salvarPartida(this);
                if (salvo) {
                    SwingUtilities.getWindowAncestor(this).dispose(); // Fecha janela atual
                    new StartView(); // Abre a tela inicial
                }
            }
        });

        menu.add(salvarEVoltar);
        menu.show(this, x, y);
    }
}
