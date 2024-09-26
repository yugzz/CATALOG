import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {
    public static void main(String[] args) {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get("input.json")));
            JSONObject json = new JSONObject(jsonString);

            int n = json.getJSONObject("keys").getInt("n");
            int k = json.getJSONObject("keys").getInt("k");

            System.out.println("n: " + n + ", k: " + k);

            List<Point> points = new ArrayList<>();

            for (int i = 1; i <= n; i++) {
                if (json.has(Integer.toString(i))) {
                    JSONObject point = json.getJSONObject(Integer.toString(i));
                    int base = Integer.parseInt(point.getString("base"));
                    String value = point.getString("value");
                    BigInteger y = new BigInteger(value, base);
                    points.add(new Point(BigInteger.valueOf(i), y));
                    System.out.println("Point " + i + ": (" + i + ", " + y + ")");
                }
            }

            if (points.size() < k) {
                System.out.println("Error: Not enough points provided to reconstruct the secret.");
                return;
            }

            BigInteger secret = lagrangeInterpolation(points, k);
            System.out.println("Calculated secret: " + secret);

        } catch (IOException e) {
            System.out.println("Error reading input file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BigInteger lagrangeInterpolation(List<Point> points, int k) {
        BigInteger secret = BigInteger.ZERO;
        BigInteger prime = BigInteger.valueOf(2).pow(127).subtract(BigInteger.ONE); // Mersenne prime

        for (int i = 0; i < k; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    numerator = numerator.multiply(points.get(j).x.negate()).mod(prime);
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x)).mod(prime);
                }
            }

            BigInteger lagrangeTerm = points.get(i).y.multiply(numerator).multiply(denominator.modInverse(prime))
                    .mod(prime);
            secret = secret.add(lagrangeTerm).mod(prime);
        }

        return secret;
    }

    static class Point {
        BigInteger x;
        BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}
