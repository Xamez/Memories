package fr.xamez.memories.game;

public enum GameState {

    STARTING("§dDémarrage"),
    MEMORIZATION("§bMémorisation"),
    BUILDING("§aConstruction"),
    WAITING("§7Attente");

    private final String state;

    GameState(String state){
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
