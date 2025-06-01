package view;

import controller.GameController;
import controller.MovementController;
import model.ChessModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConsoleView extends JFrame {
    private ChessModel model;
    private GameView gameView;
    private GameController gameController;

    public ConsoleView() {
        super("Jogo de Xadrez");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(660, 700);
        setLocationRelativeTo(null);

        initializeModel();
        initializeViewAndControllers();
        setupMenu();

        setVisible(true);
    }

    private void initializeModel() {
        model = ChessModel.getInstance();
    }

    private void initializeViewAndControllers() {
        gameView = new GameView(model);
        gameController = new GameController(model);
        new MovementController(model, gameView); // Controller de movimentação
        gameController.setGameView(gameView);
        add(gameView);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Jogo");

        JMenuItem newGameItem = new JMenuItem("Nova Partida");
        newGameItem.addActionListener(e -> resetGame());

        JMenuItem loadGameItem = new JMenuItem("Carregar Partida...");
        loadGameItem.setEnabled(false); // Para 4ª iteração

        gameMenu.add(newGameItem);
        gameMenu.add(loadGameItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void resetGame() {
        ChessModel.resetInstance();
        model = ChessModel.getInstance();
        gameView.setModel(model);
        gameView.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConsoleView());
    }
}
