package projet1.gomoku.controllers.ai;

import projet1.gomoku.gamecore.GomokuBoard;
import projet1.gomoku.gamecore.Coords;
import projet1.gomoku.gamecore.enums.Player;
import projet1.gomoku.gamecore.enums.TileState;

public final class Evaluation_1 {

    private static final int WIN5         = 200_000;
    private static final int FOUR_OPEN    = 20_000;
    private static final int FOUR_CLOSED  = 5_000;
    private static final int THREE_OPEN   = 2_000;
    private static final int THREE_CLOSED = 400;
    private static final int TWO_OPEN     = 150;
    private static final int TWO_CLOSED   = 40;
    private static final int ONE_OPEN     = 5;

    private static final int[][] DIRS = { {0,1}, {1,0}, {1,1}, {1,-1} };

    private Evaluation_1() {}

    /** Évalue le plateau du point de vue de 'perspective' */
    public static int evaluateBoard(GomokuBoard board, Player perspective) {
        return scoreFor(board, perspective) - scoreFor(board, opponent(perspective));
    }

    /** Score brut pour un joueur (somme des motifs) */
    public static int scoreFor(GomokuBoard board, Player p) {
        TileState mine = tile(p);
        int n = GomokuBoard.size;
        int score = 0;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.get(c, r) != mine) continue;

                for (int[] d : DIRS) {
                    int dr = d[0], dc = d[1];

                    // Ne compter la séquence que si (r - dr, c - dc) n'est pas une pierre à moi
                    int pr = r - dr, pc = c - dc;
                    if (inBounds(pr, pc, n) && board.get(pc, pr) == mine) {
                        continue; // on est au milieu d'une séquence déjà comptée, on saute
                    }

                    // Compter la longueur à partir de (r, c) dans (dr, dc)
                    int len = 0, rr = r, cc = c;
                    while (inBounds(rr, cc, n) && board.get(cc, rr) == mine) {
                        len++; rr += dr; cc += dc;
                    }

                    // Déterminer les extrémités ouvertes
                    int openEnds = 0;
                    if (inBounds(pr, pc, n) && board.get(pc, pr) == TileState.Empty) openEnds++;
                    if (inBounds(rr, cc, n) && board.get(cc, rr) == TileState.Empty) openEnds++;

                    score += patternScore(len, openEnds);
                }
            }
        }
        return score;
    }

    /** Barème des motifs */
    private static int patternScore(int len, int openEnds) {
        if (len >= 5) return WIN5;
        if (len == 4) return (openEnds == 2) ? FOUR_OPEN : (openEnds == 1 ? FOUR_CLOSED : 0);
        if (len == 3) return (openEnds == 2) ? THREE_OPEN : (openEnds == 1 ? THREE_CLOSED : 0);
        if (len == 2) return (openEnds == 2) ? TWO_OPEN : (openEnds == 1 ? TWO_CLOSED : 0);
        if (len == 1) return (openEnds == 2) ? ONE_OPEN : 0;
        return 0;
    }

    // ===== utilitaires =====
    private static boolean inBounds(int r, int c, int n) { return 0 <= r && r < n && 0 <= c && c < n; }
    private static Player opponent(Player p) { return p == Player.White ? Player.Black : Player.White; }
    private static TileState tile(Player p) { return p == Player.White ? TileState.White : TileState.Black; }
}

