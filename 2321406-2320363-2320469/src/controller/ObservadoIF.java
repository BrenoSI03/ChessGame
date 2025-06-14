package model;

public interface ObservadoIF {
    void addObservador(ObservadorIF o);
    void removeObservador(ObservadorIF o);
    void notificarObservadores();
}