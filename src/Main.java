import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println(Evaluator.rules());
        Evaluator.showSteps(false);

        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();

        System.out.println(Evaluator.eval(response));
    }
}
