package edu.up.cs301.museumcaper;

public enum RoomType {
    TEAL('t'),
    RED('r'),
    PURPLE('p'),
    HALLWAY('h'),
    WALL('w'),
    BLUE('b'),
    GREEN('g'),
    YELLOW('y'),
    DARK('d');

    private final char symbol;

    RoomType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public static RoomType fromChar(char c) {
        for (RoomType rt : values()) {
            if (rt.symbol == c) return rt;
        }
        return HALLWAY; // safe fallback
    }
}
