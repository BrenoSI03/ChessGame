package view;

import model.ObservadoIF;
import model.ObservadorIF;

public class ChessViewFacade implements ObservadorIF {
    private GameView gameView;

    public ChessViewFacade(GameView gameView) {
        this.gameView = gameView;
    }

    @Override
    public void notificar(ObservadoIF observado) {
        gameView.updateView();
    }

    // Métodos de fachada para a view
    public void repaintBoard() {
        gameView.repaint();
    }
    
    public void showPromotionMenu(int x, int y) {
        gameView.showPromotionMenu(x, y);
    }
}