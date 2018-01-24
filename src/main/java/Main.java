import io.github.bonigarcia.wdm.ChromeDriverManager;

public class Main {
    public static void main(String[] args) {
        ChromeDriverManager.getInstance().setup();
    }
}
