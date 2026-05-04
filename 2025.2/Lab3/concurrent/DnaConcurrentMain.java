import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DnaConcurrentMain {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java DnaConcurrentMain DIRETORIO_ARQUIVOS PADRAO");
            System.exit(1);
        }

        String dirName = args[0];
        String pattern = args[1];

        File dir = new File(dirName);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));

        List<Worker> workers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        // 🔹 cria uma thread por arquivo
        for (File f : files) {
            Worker w = new Worker(f, pattern);
            Thread t = new Thread(w);

            workers.add(w);
            threads.add(t);

            t.start();
        }

        // 🔹 espera todas terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 🔹 soma resultados (sem concorrência aqui)
        long total = 0;
        for (Worker w : workers) {
            total += w.getResult();
        }

        System.out.println("Sequência " + pattern + " foi encontrada " + total + " vezes.");
    }


}

class Worker implements Runnable {

    private File file;
    private String pattern;
    private long result;

    public Worker(File file, String pattern) {
        this.file = file;
        this.pattern = pattern;
    }

    @Override
    public void run() {
        try {
            result = countInFile(file, pattern);
        } catch (IOException e) {
            result = 0;
        }
    }

    public long getResult() {
        return result;
    }

    public static long countInFile(File file, String pattern) throws IOException {
        long total = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    total += countInSequence(line, pattern);
                }
            }
        }
        return total;    
    }

    public static long countInSequence(String sequence, String pattern) {
        if (sequence == null || pattern == null) {
            return 0;
        }
        int n = sequence.length();
        int m = pattern.length();
        if (m == 0 || n < m) {
            return 0;
        }
        long count = 0;
        for (int i = 0; i <= n - m; i++) {
            if (sequence.regionMatches(false, i, pattern, 0, m)) {
                count++;
            }
        }
        return count;
    }
}