package com.weather;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.ArrayList;

public class Main {

    private static final String[] CITIES = { "Aracaju", "Belém", "Belo Horizonte", "Boa Vista", "Brasília",
            "Campo Grande", "Cuiabá", "Curitiba", "Florianópolis", "Fortaleza", "Goiânia", "João Pessoa", "Macapá",
            "Maceió", "Manaus", "Natal", "Palmas", "Porto Alegre", "Porto Velho", "Recife", "Rio Branco",
            "Rio de Janeiro", "Salvador", "São Luís", "São Paulo", "Teresina", "Vitória" };

    private static final double[][] COORDINATES = { { -10.9472, -37.0731 }, { -1.4558, -48.5039 },
            { -19.9191, -43.9386 }, { 2.8235, -60.6758 }, { -15.7801, -47.9292 }, { -20.4697, -54.6201 },
            { -15.5961, -56.0967 }, { -25.4284, -49.2733 }, { -27.5954, -48.5480 }, { -3.7172, -38.5434 },
            { -16.6869, -49.2648 }, { -7.1153, -34.8610 }, { 0.0355, -51.0705 }, { -9.6658, -35.7350 },
            { -3.1019, -60.0250 }, { -5.7945, -35.2110 }, { -10.24, -48.3558 }, { -30.0277, -51.2287 },
            { -8.7619, -63.9039 }, { -8.0476, -34.8770 }, { -9.9754, -67.8243 }, { -22.9083, -43.1970 },
            { -12.9714, -38.5014 }, { -2.5387, -44.2825 }, { -23.5505, -46.6333 }, { -5.0910, -42.8038 },
            { -20.3155, -40.3128 } };

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        runExperiment(1); // Versão de referência
        runExperiment(3); // Versão com 3 threads
        runExperiment(9); // Versão com 9 threads
        runExperiment(27); // Versão com 27 threads
    }

    private static void runExperiment(int numThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CITIES.length; i++) {
            final int index = i;
            CompletableFuture.runAsync(() -> {
                try {
                    List<Double> data = WeatherDataCollector.getWeatherData(COORDINATES[index][0],
                            COORDINATES[index][1], "2024-01-01", "2024-01-31");
                    System.out.println("City: " + CITIES[index]);
                    WeatherDataCollector.processWeatherData(data);
                    System.out.println("-------------------------------------------------");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, executor).get();
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println("Experiment with " + numThreads + " threads took " + (endTime - startTime) + " ms");
    }
}

class WeatherDataCollector {

    @SuppressWarnings("unused")
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    @SuppressWarnings("unused")
    private static final String PARAMETERS = "&hourly=temperature_2m";

    public static List<Double> getWeatherData(double latitude, double longitude, String startDate, String endDate)
            throws Exception {
        // Simulação de chamada HTTP para obter dados de clima
        List<Double> hourlyTemperatures = new ArrayList<>();
        for (int i = 0; i < 31 * 24; i++) { // 31 dias * 24 horas por dia
            hourlyTemperatures.add(20.0 + Math.random() * 10); // Temperatura simulada entre 20 e 30 graus
        }
        return hourlyTemperatures;
    }

    public static void processWeatherData(List<Double> hourlyTemperatures) {
        double[] dailyMin = new double[31];
        double[] dailyMax = new double[31];
        double[] dailySum = new double[31];
        int[] dailyCount = new int[31];

        for (int i = 0; i < hourlyTemperatures.size(); i++) {
            int day = i / 24;
            double temp = hourlyTemperatures.get(i);
            if (dailyCount[day] == 0) {
                dailyMin[day] = temp;
                dailyMax[day] = temp;
            } else {
                dailyMin[day] = Math.min(dailyMin[day], temp);
                dailyMax[day] = Math.max(dailyMax[day], temp);
            }
            dailySum[day] += temp;
            dailyCount[day]++;
        }

        for (int i = 0; i < 31; i++) {
            if (dailyCount[i] != 0) {
                double avg = dailySum[i] / dailyCount[i];
                System.out.println(String.format("Day %2d: Min=%.2f, Max=%.2f, Avg=%.2f", i + 1, dailyMin[i], dailyMax[i], avg));
            }
        }
    }
}