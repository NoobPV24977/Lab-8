import java.io.*;
import java.net.*;

public class Main {
    private static char[][] board = new char[3][3];
    private static PrintWriter[] clients = new PrintWriter[2];
    private static int currentPlayer = 0;
    private static boolean gameOver = false;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Сервер запущено. Очікуємо гравців...");

        resetBoard();

        for (int i = 0; i < 2; i++) {
            Socket socket = serverSocket.accept();
            clients[i] = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ClientHandler(i, reader)).start();
            clients[i].println("WELCOME " + i);
        }


        clients[currentPlayer].println("YOUR_TURN");
    }

    private static synchronized void broadcast(String message) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }

    private static synchronized boolean makeMove(int player, int row, int col) {
        if (gameOver) return false;
        if (player != currentPlayer) return false;
        if (row < 0 || row > 2 || col < 0 || col > 2) return false;
        if (board[row][col] != ' ') return false;

        char symbol = player == 0 ? 'X' : 'O';
        board[row][col] = symbol;

        broadcast("MOVE," + row + "," + col + "," + symbol);

        if (checkWin(symbol)) {
            broadcast("WIN," + symbol);
            gameOver = true;
        } else if (isBoardFull()) {
            broadcast("DRAW");
            gameOver = true;
        } else {
            currentPlayer = 1 - currentPlayer;
            clients[currentPlayer].println("YOUR_TURN");
        }
        return true;
    }

    private static boolean isBoardFull() {
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == ' ') return false;
            }
        }
        return true;
    }

    private static boolean checkWin(char symbol) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol) return true;
            if (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol) return true;
        }
        return (board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) ||
                (board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol);
    }

    private static void resetBoard() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                board[i][j] = ' ';
    }

    static class ClientHandler implements Runnable {
        private int player;
        private BufferedReader in;

        public ClientHandler(int player, BufferedReader in) {
            this.player = player;
            this.in = in;
        }

        public void run() {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    String[] parts = input.split(",");
                    if (parts.length != 2) {
                        clients[player].println("INVALID");
                        continue;
                    }
                    int row, col;
                    try {
                        row = Integer.parseInt(parts[0]);
                        col = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        clients[player].println("INVALID");
                        continue;
                    }

                    boolean success = makeMove(player, row, col);
                    if (!success) {
                        clients[player].println("INVALID");
                    }
                }
            } catch (IOException e) {
                System.out.println("Гравець " + player + " вийшов.");
            }
        }
    }
}
