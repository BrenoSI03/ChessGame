package view;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import model.ChessModel;
import controller.GameController;

public class StartView extends JFrame {
    public StartView() {
        super("Início - Jogo de Xadrez");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton newGameBtn = new JButton("Nova Partida");
        JButton loadFENFileBtn = new JButton("Carregar Partida de Arquivo");

        // Nova partida
        newGameBtn.addActionListener(e -> {
            dispose();
            ChessModel.resetInstance();
            new ConsoleView(ChessModel.getInstance());
        });

        // Carregar FEN de arquivo .txt
        loadFENFileBtn.addActionListener(e -> {
            GameController controller = new GameController(ChessModel.getInstance());
            ChessModel loadedModel = controller.carregarPartidaViaArquivo(this);
            if (loadedModel != null) {
                dispose();
                ConsoleView view = new ConsoleView(loadedModel);
                controller.setConsoleView(view);
                view.setController(controller);
            }
        });

        panel.add(newGameBtn);
        panel.add(loadFENFileBtn);

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }
}
