package projet1.gomoku;

/*
 * Code created by Thomas Cabanac Diaz (Esisar)
 * */

import projet1.gomoku.controllers.HumanPlayer;
import projet1.gomoku.controllers.PlayerController;
import projet1.gomoku.controllers.ai.AI_Random;
import projet1.gomoku.gamecore.Coords;
import projet1.gomoku.gamecore.GomokuBoard;
import projet1.gomoku.gamecore.enums.Player;
import projet1.gomoku.gamecore.enums.TileState;
import projet1.gomoku.gamecore.enums.WinnerState;

public class Game {

    /**Lancer une partie entre deux joueurs de Gomoku
     * @param player1 Joueur 1
     * @param player2 Joueur 2
     * @return Vainqueur de la partie
    */
    public static WinnerState startMatch(PlayerController player1, PlayerController player2){
        GomokuBoard board = new GomokuBoard();
        WinnerState winnerState;
        Player currentPlayer = Player.White;

        int roundCount = 0;

        long player1TotalPlayTime = 0;
        long player2TotalPlayTime = 0;
        long player1LongestPlayTime = 0;
        long player2LongestPlayTime = 0;
        long player1ShortestPlayTime = Long.MAX_VALUE;
        long player2ShortestPlayTime = Long.MAX_VALUE;

        while ((winnerState = board.getWinnerState()) == WinnerState.None){ // Tant que la partie n'est pas finie
            roundCount++;

            System.out.println("Tour " + roundCount + ": " + (currentPlayer == Player.White ? "BLANC" : "NOIR"));
            long startTime = System.currentTimeMillis();
            Coords move = currentPlayer == Player.White ? player1.play(board, Player.White) : player2.play(board, Player.Black); // Obtenir le coup du joueur
            long moveDuration = System.currentTimeMillis() - startTime;

            if (currentPlayer == Player.White){
                player1TotalPlayTime += moveDuration;
                player1LongestPlayTime = Math.max(player1LongestPlayTime, moveDuration);
                player1ShortestPlayTime = Math.min(player1ShortestPlayTime, moveDuration);
            }
            else{
                player2TotalPlayTime += moveDuration;
                player2LongestPlayTime = Math.max(player2LongestPlayTime, moveDuration);
                player2ShortestPlayTime = Math.min(player2ShortestPlayTime, moveDuration);
            }

            board.set(move, currentPlayer == Player.White ? TileState.White : TileState.Black); // Jouer le coup

            System.out.println("Ligne: " + move.row);
            System.out.println("Colonne: " + move.column);
            System.out.println();
            board.print();
            System.out.println();

            currentPlayer = currentPlayer == Player.White ? Player.Black : Player.White; // Changer de joueur
        }

        if (winnerState == WinnerState.Tie) System.out.println("Égalité !");
        else System.out.println("Vainqueur: " + (winnerState == WinnerState.White ? "Blanc" : "Noir"));

        double movePerPlayer = (double)(roundCount / 2);
        int player1MoveCount = (int)Math.ceil(movePerPlayer);
        int player2MoveCount = (int)Math.floor(movePerPlayer);

        System.out.println("\nStatistiques :");
        System.out.println("Blanc: " + player1MoveCount + " coups, " + (player1TotalPlayTime/player1MoveCount) + " ms/coup, " + player1LongestPlayTime + " ms (max), " + player1ShortestPlayTime + " ms (min)");
        System.out.println("Noir: " + player2MoveCount + " coups, " + (player2TotalPlayTime/player2MoveCount) + " ms/coup, " + player2LongestPlayTime + " ms (max), " + player2ShortestPlayTime + " ms (min)");

        return winnerState;
    }

    public static void main(String[] args) {
        java.util.Scanner in = new java.util.Scanner(System.in);
        while (true) {
            System.out.println("=== Gomoku ===");
            System.out.println("1) Joueur vs Joueur (HotSeat)");
            System.out.println("2) Joueur (Blanc) vs IA (Noir)");
            System.out.println("3) IA (Blanc) vs Joueur (Noir)");
            System.out.println("4) IA Random vs IA MinMax");
            System.out.println("5) IA MinMax vs IA Minmax (choix des profondeurs)");
            System.out.println("0) Quitter");
            System.out.print("> ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1": // HotSeat
                    startMatch(new HumanPlayer(), new HumanPlayer());
                    break;

                case "2": { // Humain Blanc vs IA Noir
                    PlayerController ia = new projet1.gomoku.controllers.ai.AI_MinMax(3); // profondeur 3

                    startMatch(new HumanPlayer(), ia);
                    break;
                }

                case "3": { // IA Blanc vs Humain Noir
                    PlayerController ia = new projet1.gomoku.controllers.ai.AI_MinMax(3); // profondeur 3

                    startMatch(ia, new HumanPlayer());
                    break;
                }

                case "4": {
                    PlayerController ia_minmax = new projet1.gomoku.controllers.ai.AI_MinMax(3);
                    PlayerController ia_random = new projet1.gomoku.controllers.ai.AI_Random();

                    startMatch(ia_minmax, ia_random);
                }

                case "5": {
                    System.out.println("Profondeur de l'IA blanche");
                    String s1 = in.nextLine().trim();
                    System.out.println("Profondeur de l'IA noire");
                    String s2 = in.nextLine().trim();
                    int p1 = Integer.parseInt(s1); // régler le problème de la prodondeur qui est toujours 1 !!!
                    int p2 = Integer.parseInt(s2);
                    System.out.println(p1);
                    System.out.println(p1);

                    PlayerController ia_blanc = new projet1.gomoku.controllers.ai.AI_MinMax(p1);
                    PlayerController ia_noir = new projet1.gomoku.controllers.ai.AI_MinMax(p2);

                    startMatch(ia_blanc, ia_noir);
                };

                case "0":
                    System.out.println("Au revoir !");
                    return;

                default:
                    System.out.println("Choix invalide.\n");
            }
        }
    }


}
