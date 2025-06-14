package view;

import controller.ObservadoIF;
import controller.ObservadorIF;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import model.ChessModel;
import model.ObservadorIF;
import model.ObservadoIF;
import controller.GameController;

public class ConsoleView extends JFrame {
    private ChessModel model;
    private GameView gameView;
    private GameController controller;
    private JLabel turnoLabel;

    /**
     * Construtor da janela principal. Recebe o modelo já pronto (novo ou carregado).
     */
    public ConsoleView(ChessModel model) {
        super("Jogo de Xadrez");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(660, 700);

        this.model = model;
        this.controller = new GameController(model);
        this.controller.setConsoleView(this);

        this.gameView = new GameView(model);
        this.controller.setGameView(gameView);
        this.gameView.setController(controller);

        // Registra como observador para atualizar o turno
        model.addObservador(new ObservadorIF() {
            @Override
            public void notificar(ObservadoIF observado) {
                updateTurn();
            }
        });

        setJMenuBar(createMenuBar());
        updateTurn(); // Atualiza o turno inicial
        add(gameView);

        setVisible(true);
    }

    /**
     * Cria a barra de menu com opções de jogo e exibe o turno atual.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Jogo");

        JMenuItem newGame = new JMenuItem("Reiniciar Partida");
        newGame.addActionListener(e -> controller.restartGame());

        JMenuItem carregarPartida = new JMenuItem("Carregar Partida");
        carregarPartida.addActionListener(e -> carregarPartida());

        gameMenu.add(newGame);
        gameMenu.add(carregarPartida);
        menuBar.add(gameMenu);

        turnoLabel = new JLabel();
        menuBar.add(turnoLabel);

        return menuBar;
    }

    /**
     * Atualiza a label de turno no menu.
     */
    public void updateTurn() {
        SwingUtilities.invokeLater(() -> {
            String cor = model.isWhiteTurn() ? "brancas" : "pretas";
            turnoLabel.setText("Turno de " + cor);
        });
    }

    public void setController(GameController controller) {
        this.controller = controller;
        if (gameView != null) {
            controller.setGameView(gameView);
            gameView.setController(controller);
        }
    }
    
    private void carregarPartida() {
        ChessModel newModel = controller.carregarPartidaViaArquivo(this);
        if (newModel != null) {
            // Remove observador do modelo antigo
            model.removeObservador(controller);
            
            this.model = newModel;

            // Remove o GameView antigo
            remove(gameView);

            // Cria um novo GameView vinculado ao novo modelo
            gameView = new GameView(newModel);

            // Atualiza o controller para o novo modelo e nova view
            controller = new GameController(newModel);
            controller.setConsoleView(this);
            controller.setGameView(gameView);
            gameView.setController(controller);

            // Adiciona observador para o novo modelo
            newModel.addObservador(new ObservadorIF() {
                @Override
                public void notificar(ObservadoIF observado) {
                    updateTurn();
                }
            });

            // Adiciona o novo GameView na tela
            add(gameView);

            updateTurn();
            revalidate();
            repaint();
        }
    }
    
    public void setModel(ChessModel model) {
        // Remove observador do modelo antigo
        if (this.model != null) {
            this.model.removeObservador(controller);
        }
        
        this.model = model;
        
        // Adiciona observador para o novo modelo
        if (model != null) {
            model.addObservador(new ObservadorIF() {
                @Override
                public void notificar(ObservadoIF observado) {
                    updateTurn();
                }
            });
        }
    }
    
    public void setGameView(GameView gameView) {
        if (this.gameView != null) {
            remove(this.gameView);
        }
        this.gameView = gameView;
        add(gameView);
        revalidate();
        repaint();
    }
}
