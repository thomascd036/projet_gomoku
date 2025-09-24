package projet1.gomoku.controllers.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import projet1.gomoku.controllers.PlayerController;
import projet1.gomoku.gamecore.Coords;
import projet1.gomoku.gamecore.GomokuBoard;
import projet1.gomoku.gamecore.enums.Player;
import projet1.gomoku.gamecore.enums.TileState;


public class AI_MinMax extends PlayerController {

    private final Random rng = new Random();

    private int depth = 3;           // profondeur par défaut
    private boolean localGen = true; // activer la génération locale
    private int radius = 2;          // rayon pour la génération locale

    public AI_MinMax() {}

    public AI_MinMax(int depth) {
        this.depth = Math.max(1, depth);
    }

    public AI_MinMax(int depth, boolean localGen, int radius) {
        this.depth = Math.max(1, depth);
        this.localGen = localGen;
        this.radius = Math.max(1, radius);
    }

    public void setDepth(int depth) { this.depth = Math.max(1, depth); }
    public void setLocalGen(boolean localGen) { this.localGen = localGen; }
    public void setRadius(int radius) { this.radius = Math.max(1, radius); }

    @Override
    public Coords play(GomokuBoard board, Player player) {
        List<Coords> moves = generateMoves(board);
        if (moves.isEmpty()) return new Coords(GomokuBoard.size / 2, GomokuBoard.size / 2);
        moves.sort(Comparator.comparingInt((Coords m) -> -moveOrderingHeuristic(board, m)));

        int bestScore = Integer.MIN_VALUE;
        List<Coords> bestMoves = new ArrayList<>();

        for (Coords m : moves) {
            board.set(m, tile(player));
            int score = minValue(board, depth - 1, player, m, 1); //
            board.set(m, TileState.Empty);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(m.clone());
            } else if (score == bestScore) {
                bestMoves.add(m.clone());
            }
        }

        return bestMoves.get(rng.nextInt(bestMoves.size()));
    }




    private int maxValue(GomokuBoard board, int d, Player rootPlayer, Coords lastMove, int ply) {

        if (lastMove != null && isWinningFrom(board, lastMove, tile(opponent(rootPlayer)))) {
            return -1_000_000 + ply;
        }
        if (d == 0 || isBoardFull(board)) {
            return Evaluation_1.evaluateBoard(board, rootPlayer);
        }

        int v = Integer.MIN_VALUE;
        List<Coords> moves = generateMoves(board);
        moves.sort(Comparator.comparingInt((Coords m) -> -moveOrderingHeuristic(board, m)));

        for (Coords m : moves) {
            board.set(m, tile(rootPlayer));
            v = Math.max(v, minValue(board, d - 1, rootPlayer, m, ply + 1));
            board.set(m, TileState.Empty);
        }
        return v;
    }


    private int minValue(GomokuBoard board, int d, Player rootPlayer, Coords lastMove, int ply) {

        if (lastMove != null && isWinningFrom(board, lastMove, tile(rootPlayer))) {
            return 1_000_000 - ply;
        }
        if (d == 0 || isBoardFull(board)) {
            return Evaluation_1.evaluateBoard(board, rootPlayer);
        }

        int v = Integer.MAX_VALUE;
        Player adv = opponent(rootPlayer);
        List<Coords> moves = generateMoves(board);
        moves.sort(Comparator.comparingInt((Coords m) -> -moveOrderingHeuristic(board, m)));

        for (Coords m : moves) {
            board.set(m, tile(adv));
            v = Math.min(v, maxValue(board, d - 1, rootPlayer, m, ply + 1));
            board.set(m, TileState.Empty);
        }
        return v;
    }



    private List<Coords> generateMoves(GomokuBoard board) {
        int n = GomokuBoard.size;
        List<Coords> list = new ArrayList<>();


        if (isBoardEmpty(board)) {
            list.add(new Coords(n / 2, n / 2));
            return list;
        }

        if (!localGen) {
            for (int r = 0; r < n; r++)
                for (int c = 0; c < n; c++)
                    if (board.get(c, r) == TileState.Empty) list.add(new Coords(c, r));
            return list;
        }


        boolean[][] mark = new boolean[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.get(c, r) != TileState.Empty) {
                    for (int dr = -radius; dr <= radius; dr++) {
                        for (int dc = -radius; dc <= radius; dc++) {
                            int rr = r + dr, cc = c + dc;
                            if (0 <= rr && rr < n && 0 <= cc && cc < n) {
                                if (!mark[rr][cc] && board.get(cc, rr) == TileState.Empty) {
                                    mark[rr][cc] = true;
                                    list.add(new Coords(cc, rr));
                                }
                            }
                        }
                    }
                }
            }
        }


        if (list.isEmpty()) {
            for (int r = 0; r < n; r++)
                for (int c = 0; c < n; c++)
                    if (board.get(c, r) == TileState.Empty) list.add(new Coords(c, r));
        }
        return list;
    }

    private int moveOrderingHeuristic(GomokuBoard board, Coords m) {
        int n = GomokuBoard.size;
        int center = n / 2;
        int centerScore = 20 - (Math.abs(m.row - center) + Math.abs(m.column - center));
        int adj = 0;
        for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) if (!(dr == 0 && dc == 0)) {
            int rr = m.row + dr, cc = m.column + dc;
            if (0 <= rr && rr < n && 0 <= cc && cc < n) {
                if (board.get(cc, rr) != TileState.Empty) adj++;
            }
        }
        return centerScore + adj * 3;
    }


    private static boolean isBoardFull(GomokuBoard board) {
        int n = GomokuBoard.size;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (board.get(c, r) == TileState.Empty) return false;
        return true;
    }

    private static boolean isBoardEmpty(GomokuBoard board) {
        int n = GomokuBoard.size;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (board.get(c, r) != TileState.Empty) return false;
        return true;
    }

    private static boolean isWinningFrom(GomokuBoard board, Coords last, TileState who) {
        if (last == null) return false;
        int[][] dirs = { {0,1}, {1,0}, {1,1}, {1,-1} };
        for (int[] d : dirs) {
            int count = 1;
            count += countDir(board, last,  d[0],  d[1], who);
            count += countDir(board, last, -d[0], -d[1], who);
            if (count >= 5) return true;
        }
        return false;
    }

    private static int countDir(GomokuBoard board, Coords start, int dr, int dc, TileState who) {
        int n = GomokuBoard.size;
        int r = start.row + dr, c = start.column + dc, cnt = 0;
        while (0 <= r && r < n && 0 <= c && c < n && board.get(c, r) == who) {
            cnt++; r += dr; c += dc;
        }
        return cnt;
    }

    private static Player opponent(Player p) { return p == Player.White ? Player.Black : Player.White; }
    private static TileState tile(Player p) { return p == Player.White ? TileState.White : TileState.Black; }
}
