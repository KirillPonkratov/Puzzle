import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    /**
     * main method
     *
     * @param args -Xms256m -Xmx256m
     */
    public static void main(String[] args) {
        ConundrumSolver solver = new ConundrumSolverImpl();
        System.out.println("Введите цифры от 0 до 7:");
        Scanner scanner = new Scanner(System.in);
        int[] initialState = new int[8];
        for (int i = 0; i < 8; i++) {
            try {
                initialState[i] = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Необходимо ввести цифры от 0 до 7");
                scanner.next();
                i--;
            }
        }
        System.out.println("Результат:");
        System.out.println(Arrays.toString(solver.resolve(initialState)));
    }
}
