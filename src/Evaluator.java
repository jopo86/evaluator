import java.util.ArrayList;

public abstract class Evaluator {

    public Evaluator() {}

    private static char[] _digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'E', '.' };
    private static char[] _signs = { '+', '-', '*', '/', '^' };

    private static boolean nextTermIsNegative = false;
    public static boolean _showSteps = false;

    public static String rules() {
       return "RULE #1: ONLY use \'*\' for multiplication. " +
                "(e.g. \"(2+2)*(2+2)\" instead of \"(2+2)(2+2)\" and -1 * (2+2)\" instead of \"-(2+2)\")\n" +
                "RULE #2 (fix in progress): To negate a power operation, use \"-1 * ...\" " +
                "(e.g. \"-1 * 5^2\" instead of \"-5^2\")";
    }

    public static double eval(String eq) {

        ArrayList<Double> terms = new ArrayList<Double>();
        ArrayList<Character> signs = new ArrayList<Character>();

        // STEP 1: PARSE
        int first = 0;
        int current = 0;
        int last = 0;
        for (int i = 0; i < eq.length(); i++) {

            // check for & solve parenthesis first
            if (eq.charAt(current) == '(') {
                int index = current + 1;
                int open = current;
                int close = 0;
                int count = 1;

                while (close == 0) {
                    if (eq.charAt(index) == ')') {
                        count--;
                        if (count == 0) close = index;
                        else index++;
                    } else if (eq.charAt(index) == '(') {
                        count++;
                        index++;
                    } else index++;
                }

                eq = String.valueOf(new StringBuilder(eq)
                        .replace(open, close + 1, String.valueOf(eval((eq.substring(open + 1, close))))));
                if (_showSteps) System.out.println(eq);

//                System.out.println(eq);
                return eval(eq);


            }

            // after parenthesis sorted out, continue on to normal parsing
            current = i;

            // check if first term should be negative
            if (eq.charAt(0) == '-' && current == 1) nextTermIsNegative = true;

            if (isDigit(eq.charAt(current))) {
                if (current == 0) first = current;
                else if (isDigit(eq.charAt(current - 1))) last = current;
                else first = current;

                if (i == eq.length() - 1 && isDigit(eq.charAt(eq.length() - 1))) {
                    last = i + 1;
                    terms.add((
                            nextTermIsNegative ?
                                    -Double.parseDouble(eq.substring(first, last)) :
                                    Double.parseDouble(eq.substring(first, last))
                    ));
                    nextTermIsNegative = false;
                    break;
                }

            } else {
                if (current != 0) {
                    if (isDigit(eq.charAt(current - 1))) {
                        last = current;
                        terms.add((
                                nextTermIsNegative ?
                                        -Double.parseDouble(eq.substring(first, last)) :
                                        Double.parseDouble(eq.substring(first, last))
                        ));
                        nextTermIsNegative = false;
                    }
                }

                if (isSign(eq.charAt(current))) {
                    if (eq.charAt(current) != '-') signs.add(eq.charAt(current));
                    else {
                        int index = current - 1;
                        while (!nextTermIsNegative) {
                            if (current == 0) {
                                nextTermIsNegative = true;
                                break;
                            }
                            if (isDigit(eq.charAt(index))) {
                                signs.add(eq.charAt(current));
                                break;
                            }
                            if (isSign(eq.charAt(index))) {
                                nextTermIsNegative = true;
                                break;
                            }
                            if (eq.charAt(index) == '(' || eq.charAt(index) == ')') {
                                signs.add(eq.charAt(current));
                                break;
                            }
                            index--;
                        }
                    }
                }
            }
        }

//        for (double term : terms) System.out.println(term);
//        for (char sign : signs) System.out.println(sign);

        // STEP 2: MATH

        // exponents first (parenthesis already done in parsing)
        if (signs.size() > 1) {
            for (int i = 0; i < signs.size(); i++) {
                double replacement;
                if (signs.get(i) == '^') {
                    replacement = Math.pow(terms.get(i), terms.get(i + 1));
                    signs.remove(i);
                    terms.remove(i);
                    terms.remove(i);
                    terms.add(i, (nextTermIsNegative ? -replacement : replacement));
                    nextTermIsNegative = false;
                    if (i != 0) i--;
                }
            }

        }

        // then multiplication and division
        if (signs.size() > 1) {
            for (int i = 0; i < signs.size(); i++) {
                double replacement = 0;
                if (signs.get(i) == '*') replacement = terms.get(i) * terms.get(i + 1);
                else if (signs.get(i) == '/') replacement = terms.get(i) / terms.get(i + 1);
                if (signs.get(i) == '*' || signs.get(i) == '/') {
                    signs.remove(i);
                    terms.remove(i);
                    terms.remove(i);
                    terms.add(i, (nextTermIsNegative ? -replacement : replacement));
                    nextTermIsNegative = false;
                    if (i != 0) i--;
                }
            }
        }

        if (terms.size() == 1) return terms.get(0);

        double answer = 0;
        int index = 0;

        while (index < signs.size()) {

            if (index == 0) {
                switch (signs.get(index)) {
                    case '+' -> answer = terms.get(index) + terms.get(index + 1);
                    case '-' -> answer = terms.get(index) - terms.get(index + 1);
                    case '*' -> answer = terms.get(index) * terms.get(index + 1);
                    case '/' -> answer = terms.get(index) / terms.get(index + 1);
                    case '^' -> answer = Math.pow(terms.get(index), terms.get(index + 1));
                }
            } else {
                switch (signs.get(index)) {
                    case '+' -> answer += terms.get(index + 1);
                    case '-' -> answer -= terms.get(index + 1);
                    case '*' -> answer *= terms.get(index + 1);
                    case '/' -> answer /= terms.get(index + 1);
                    case '^' -> answer = Math.pow(answer, terms.get(index + 1));
                }
            }
            index++;
        }

//        for (int i = 0; i < terms.size(); i++) {
//            System.out.println(terms.get(i));
//            if (i != terms.size() - 1) System.out.println(signs.get(i));
//        }

        return answer;
    }

    private static boolean isDigit(char c) {
        for (char digit : _digits) if (digit == c) return true;
        return false;
    }

    private static boolean isSign(char c) {
        for (char sign : _signs) if (sign == c) return true;
        return false;
    }

    public static void showSteps(boolean bool) {
        _showSteps = bool;
    }

}
