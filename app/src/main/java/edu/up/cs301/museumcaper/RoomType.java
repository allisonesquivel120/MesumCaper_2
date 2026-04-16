package edu.up.cs301.museumcaper;
/**
 * Tells us what room guard/thief in depending on character is outputs
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public enum RoomType {
    TEAL('t'),
    RED('r'),
    PURPLE('p'),
    HALLWAY('h'),
    WALL('w'),
    BLUE('b'),
    GREEN('g'),
    YELLOW('y'),
    DOOR('+'),
    DARKGRAY('o'),
    POWER('v');

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
