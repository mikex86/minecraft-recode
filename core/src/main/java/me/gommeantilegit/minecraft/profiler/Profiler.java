package me.gommeantilegit.minecraft.profiler;

//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.scene.Scene;
//import javafx.scene.chart.LineChart;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.XYChart;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;

import me.gommeantilegit.minecraft.utils.Clock;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Profiler object for profiling code.
 */
public class Profiler {

    /**
     * Name of the profiler
     */
    @NotNull
    private final String name;

    /**
     * Nanoseconds resolution clock instance for timing.
     */
    @NotNull
    private final Clock clock = new Clock(true);

    /**
     * State if the profiling data should be visualized in a javafx chart.
     */
    private final boolean logHistory;

    /**
     * Stores the average time it takes to perform the task profiled.
     */
    private long averageNanos;

    /**
     * Stores the count how often the action has been performed.
     */
    private long actionsPerformed;

    /**
     * Stores the time actions totally were being performed.
     */
    private long totalNanoseconds;

    /**
     * Stores the highest time an action took.
     */
    private long high = -1;

    /**
     * Stores the lowest time an action took.
     */
    private long low = -1;

    /**
     * Stores all Profilers used for different actions with their parent name
     */
    private final HashMap<String, Profiler> subActions = new HashMap<>();

    /**
     * Default constructor for the Profiler object
     *
     * @param name the name of the profiler
     */
    public Profiler(@NotNull String name, boolean logHistory) {
        this.name = name;
        this.logHistory = logHistory;
//        if (logHistory)
//            new Thread(() -> {
//                ProfilerChart.profiler = this;
//                ProfilerChart.launch(ProfilerChart.class);
//            }).start();
    }

    /**
     * Call method to indicate that your action starts.
     */
    public void actionStart() {
        clock.reset();
    }

    /**
     * Starts a sub-action of the profiler
     *
     * @param action the key of the action
     */
    public void actionStart(String action) {
        Profiler profiler = this.subActions.get(action);
        if (profiler == null) {
            profiler = new Profiler(action, false);
        }
        this.subActions.put(action, profiler);
        profiler.actionStart();
    }

    /**
     * Ends a sub-action of the profiler
     *
     * @param action the key of the action
     */
    public void actionEnd(String action) {
        Profiler profiler = this.subActions.get(action);
        if (profiler != null) {
            profiler.actionEnd();
        } else throw new IllegalStateException("Action \"" + action + "\" was not started!");
    }

    /**
     * Call method to indicate that your action ends.
     */
    public void actionEnd() {
        long passed = clock.getTimePassed();
        totalNanoseconds += passed;
        actionsPerformed++;
        averageNanos = (long) ((double) totalNanoseconds / (double) actionsPerformed);
//        if (this.logHistory) {
//            Platform.runLater((() -> ProfilerChart.chart.getData().get(0).getData().add(new XYChart.Data<>(actionsPerformed / 100, passed))));
//        }
        if (passed > high)
            high = passed;
        if (passed < low || low == -1)
            low = passed;
    }

    /**
     * Prints the profiling results.
     */
    public void printResults() {
        this.printSelfResults();
        for (Profiler profiler : this.subActions.values()) {
            profiler.printSelfResults();
        }
    }

    public void printSelfResults() {
        System.out.println("------------------Profiling results of Profiler \"" + name + "\"------------------");
        System.out.println("Average nanoseconds: " + averageNanos + " / " + averageNanos / 1E6 + "ms");
        System.out.println("Total time spent on action: " + totalNanoseconds + " / " + totalNanoseconds / 1E6 + "ms");
        System.out.println("Highest time: " + high + " / " + high / 1E6 + "ms");
        System.out.println("Lowest time: " + low + " / " + low / 1E6 + "ms");
        System.out.println("Actions performed: " + actionsPerformed);
        System.out.println("___________________________________________________________________________________");
    }


//    public static class ProfilerChart extends Application {
//
//        @NotNull
//        private static Profiler profiler;
//
//
//        @NotNull
//        private static LineChart<Number, Number> chart;
//        private Stage stage;
//
//        public void start(Stage stage) {
//            this.stage = stage;
//            initGui();
//        }
//
//        public void initGui() {
//            stage.setTitle("History of \"" + profiler.name + "\"");
//            StackPane layout = new StackPane();
//            Scene scene = new Scene(layout);
//            stage.setScene(scene);
//            //Configure UI
//            {
//                XYChart.Series<Number, Number> series = new XYChart.Series<>();
//                series.setName(profiler.name);
//                {
//                    // Create the X-Axis
//                    NumberAxis xAxis = new NumberAxis();
//                    xAxis.setLabel("Time");
//                    // Customize the X-Axis, so points are scattered uniformly
//                    xAxis.setAutoRanging(false);
//                    xAxis.setTickUnit(50);
//
//                    // Create the Y-Axis
//                    NumberAxis yAxis = new NumberAxis();
//                    yAxis.setLabel("Nanoseconds");
//                    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
//                    ProfilerChart.chart = chart;
//                    chart.setTitle("Nanoseconds over time");
//                    chart.getData().add(series);
//                    layout.getChildren().add(chart);
//                }
//            }
//            stage.show();
//        }
//
//    }

    @NotNull
    public String getName() {
        return name;
    }

    public long getAverageNanos() {
        return averageNanos;
    }
}
