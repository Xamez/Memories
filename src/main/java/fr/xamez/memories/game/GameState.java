package fr.xamez.memories.game;

public enum GameState {

    STARTING("§dDémarrage"),
    MEMORIZATION("§bMémorisation"),
    BUILDING("§aConstruction"),
    WAITING("§7Attente"),
    FINISHED("§cTerminé");

    private final String state;

    GameState(String state){
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
