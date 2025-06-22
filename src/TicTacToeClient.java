import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TicTacToeClient {
    private static char[][] board = new char[3][3];

    public static void main(String[] args) throws IOException {

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                board[i][j] = ' ';

        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(System.in);

        Thread listener = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("WELCOME")) {
                        System.out.println("Підключено як гравець #" + message.split(" ")[1]);
                    } else if (message.startsWith("MOVE")) {
                        String[] parts = message.split(",");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        char symbol = parts[3].charAt(0);
                        board[row][col] = symbol;
                        System.out.println("Хід: " + symbol + " на [" + row + "," + col + "]");
                        printBoard();
                    } else if (message.equals("INVALID")) {
                        System.out.println("Недопустимий хід. Спробуйте ще раз.");
                    } else if (message.equals("YOUR_TURN")) {
                        System.out.println("Ваш хід! Введіть рядок і стовпець (0-2) через кому (наприклад: 1,2):");
                    } else if (message.startsWith("WIN")) {
                        char winner = message.split(",")[1].charAt(0);
                        System.out.println("Гра закінчена! Переміг гравець: " + winner);
                        printBoard();
                        System.exit(0);
                    } else if (message.equals("DRAW")) {
                        System.out.println("Гра закінчена! Нічия.");
                        printBoard();
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.out.println("З'єднання з сервером втрачено.");
                System.exit(0);
            }
        });
        listener.start();

        while (true) {
            String input = scanner.nextLine();
            if (input.matches("[0-2],[0-2]")) {
                out.println(input);
            } else {
                System.out.println("Введіть хід у форматі: рядок,стовпець (наприклад: 0,1)");
            }
        }
    }

    private static void printBoard() {
        System.out.println("\nПоточне поле:");
        for (int i = 0; i < 3; i++) {
            System.out.print(" ");
            for (int j = 0; j < 3; j++) {
                System.out.print(board[i][j]);
                if (j < 2) System.out.print(" | ");
            }
            System.out.println();
            if (i < 2) System.out.println("---+---+---");
        }
        System.out.println();
    }
}
