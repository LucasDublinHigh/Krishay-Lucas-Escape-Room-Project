import java.util.Scanner;

public class EscapeRoom
{
    public static void main(String[] args)
    {      
        // Welcome message
        System.out.println("Welcome to EscapeRoom!");
        System.out.println("Get to the other side of the room, avoiding walls and invisible traps,");
        System.out.println("pick up all the prizes.\n");

        GameGUI game = new GameGUI();
        game.createBoard();

        final int m = 60;
        int score = 0;

        Scanner in = new Scanner(System.in);
        String[] validCommands = {
            "right","left","up","down","r","l","u","d",
            "jump","jr","jumpleft","jl","jumpup","ju","jumpdown","jd",
            "pickup","p","quit","q","replay","help","?"
        };

        boolean play = true;
        while (play)
        {
            System.out.print("Enter command: ");
            String input = UserInput.getValidInput(validCommands);

            int deltaScore = 0; // track score changes per action

            switch (input) {
                case "right": case "r":
                    deltaScore += game.movePlayer(m, 0);
                    break;
                case "left":  case "l":
                    deltaScore += game.movePlayer(-m, 0);
                    break;
                case "up":    case "u":
                    deltaScore += game.movePlayer(0, -m);
                    break;
                case "down":  case "d":
                    deltaScore += game.movePlayer(0, m);
                    break;
                case "jump": case "jr":
                    deltaScore += game.movePlayer(2*m, 0);
                    break;
                case "jumpleft": case "jl":
                    deltaScore += game.movePlayer(-2*m, 0);
                    break;
                case "jumpup": case "ju":
                    deltaScore += game.movePlayer(0, -2*m);
                    break;
                case "jumpdown": case "jd":
                    deltaScore += game.movePlayer(0, 2*m);
                    break;
                case "pickup": case "p":
                    deltaScore += game.pickupPrize();
                    break;
                case "replay":
                    deltaScore += game.replay();
                    System.out.println("Board reset.");
                    game.showReplayScreen();  // NEW: Show replay screen
                    break;
                case "help": case "?":
                    showTextHelp();
                    break;
                case "quit": case "q":
                    play = false;
                    break;
            }

            score += deltaScore;

            // LOSS: Trap hit
            if (game.isTrap(0, 0)) {
                score += game.springTrap(0, 0);
                System.out.println("GAME OVER! You hit a trap!");
                game.showGameOverScreen(); // Loss screen
                play = false;
            }

            // WIN: All prizes collected or custom win logic
            if (game.didWin()) {  // Assume this method is defined in GameGUI
                System.out.println("CONGRATULATIONS! You escaped!");
                game.showWinScreen(); // Win screen
                play = false;
            }

            System.out.println("Current score: " + score);
        }

        // Final scoring
        score += game.endGame();

        System.out.println("Final score: " + score);
        System.out.println("Steps taken: " + game.getSteps());
    }

    private static void showTextHelp() {
        System.out.println("\n=== Commands ===");
        System.out.println("right/left/up/down  (r/l/u/d) : move one space");
        System.out.println("jump/jr/jl/ju/jd              : jump two spaces (cannot cross walls)");
        System.out.println("pickup or p                   : pick up prize at current spot");
        System.out.println("replay                        : reset board and steps");
        System.out.println("quit or q                     : end the game");
        System.out.println("help or ?                     : show this list\n");
    }
}

        
