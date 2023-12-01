import javax.swing.JOptionPane;
import java.util.*;

public class warning {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String answer, word;
        answer = sc.nextLine();

        if (answer.equals("Person")) {
            word = "사람";
        } else if (answer.equals("Animal")) {
            word = "동물";
        } else if (answer.equals("Car")) {
            word = "자동차";
        } else {
            word = "알 수 없는 단어"; // "Unknown word" in Korean
        }

        JOptionPane.showMessageDialog(null, word);
    }
}
