package smoke;

public final class Smoke {
    public static void main(String[] args) {
        if (1 + 1 != 2) {
            System.err.println("smoke: arithmetic disagrees");
            System.exit(1);
        }
        System.out.println("smoke: java ok");
    }
}
