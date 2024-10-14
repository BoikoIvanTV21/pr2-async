import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FactorialCalculator {

    // ConcurrentHashMap для збереження чисел і їх факторіалів
    private static final ConcurrentHashMap<Integer, BigInteger> factorialMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Створюємо ExecutorService для керування потоками
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Список для збереження Future результатів
        List<Future<BigInteger>> futureResults = new ArrayList<>();

        // Масив чисел для обчислення факторіалів
        int[] numbers = {5, 7, 10, 12, 15};

        // Створюємо Callable задачі для кожного числа
        for (int number : numbers) {
            Callable<BigInteger> task = createFactorialTask(number);
            Future<BigInteger> future = executor.submit(task);
            futureResults.add(future);
        }

        // Обробка результатів з таймаутом
        processResultsWithTimeout(futureResults, numbers);

        // Завершуємо роботу ExecutorService
        executor.shutdown();

        // Виведення всієї мапи факторіалів
        printFactorialMap();
    }

    // Створення задачі для обчислення факторіалу
    private static Callable<BigInteger> createFactorialTask(int number) {
        return () -> {
            // Перевірка на недопустимі числа
            if (number < 0) {
                throw new IllegalArgumentException("Факторіал не визначений для від'ємних чисел: " + number);
            }
            BigInteger result = calculateFactorial(number);
            factorialMap.put(number, result);
            return result;
        };
    }

    // Метод для обчислення факторіалу
    private static BigInteger calculateFactorial(int number) {
        BigInteger factorial = BigInteger.ONE;
        for (int i = 2; i <= number; i++) {
            factorial = factorial.multiply(BigInteger.valueOf(i));
        }
        return factorial;
    }

    // Обробка результатів з перевіркою на скасування і таймаутом
    private static void processResultsWithTimeout(List<Future<BigInteger>> futureResults, int[] numbers) {
        for (int i = 0; i < futureResults.size(); i++) {
            Future<BigInteger> future = futureResults.get(i); // Correct variable initialization here
            try {
                // Отримуємо результат з таймаутом 2 секунди
                BigInteger result = future.get(2, TimeUnit.SECONDS);
                System.out.println("Факторіал числа " + numbers[i] + " = " + result);
            } catch (TimeoutException e) {
                System.err.println("Обчислення для числа " + numbers[i] + " не завершилось у встановлений час.");
                future.cancel(true); // Скасовуємо задачу після таймауту
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Помилка під час обчислення факторіалу для числа " + numbers[i] + ": " + e.getMessage());
            } catch (CancellationException e) {
                System.out.println("Обчислення для числа " + numbers[i] + " було скасовано.");
            }
        }
    }

    // Виведення всіх факторіалів, збережених у мапі
    private static void printFactorialMap() {
        System.out.println("\nЗбережені факторіали:");
        for (Map.Entry<Integer, BigInteger> entry : factorialMap.entrySet()) {
            System.out.println("Число: " + entry.getKey() + ", Факторіал: " + entry.getValue());
        }
    }
}
