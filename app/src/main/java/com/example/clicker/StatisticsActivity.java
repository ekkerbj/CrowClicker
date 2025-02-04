package com.example.clicker;

import static java.util.stream.Collectors.joining;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.clicker.databinding.ActivityStatisticsBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private ActivityStatisticsBinding binding;
    private PointsHelper pointsHelper;
    private PieChart chart;

    public static int adjustColor(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Math.round(Color.red(color) * factor);
        int green = Math.round(Color.green(color) * factor);
        int blue = Math.round(Color.blue(color) * factor);

        red = Math.min(red, 255);
        green = Math.min(green, 255);
        blue = Math.min(blue, 255);

        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pointsHelper = new PointsHelper(this);
        updateCounts();
        buildLeaderBoard();
    }

    private void displayPieChart(Map<String, Long> data, int total) {
        chart = findViewById(R.id.baitChart);
        //chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);

        chart.setDragDecelerationFrictionCoef(0.95f);

        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.getLegend().setEnabled(false);

        chart.setHoleRadius(30f);
        chart.setDrawHoleEnabled(true);
        chart.setDrawSlicesUnderHole(false);
        chart.setTransparentCircleRadius(0);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        // entry label styling
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(12f);

        chart.setCenterTextColor(Color.BLACK);
        chart.setCenterText("Total:\n" + total);

        List<PieEntry> entries = data.entrySet().stream()
                .map(entry -> {
                         PieEntry pieEntry = new PieEntry(entry.getValue().floatValue(), entry.getKey());
                         return pieEntry;
                     }
                ).collect(Collectors.toList());

        PieDataSet dataSet = new PieDataSet(entries, "Catch Breakdown by Bait");
        Map<String, Integer> colorMap = generateBaitColors(data.keySet());
        dataSet.setColors(new ArrayList<>(colorMap.values()));

        dataSet.setSliceSpace(5f);

        PieData pdata = new PieData(dataSet);
        pdata.setValueFormatter(new DefaultValueFormatter(0));
        pdata.setValueTextSize(12f);
        pdata.setValueTextColor(Color.BLACK);

        chart.setData(pdata);
        chart.highlightValues(null);
        chart.invalidate();
    }

    private Map<String, Integer> generateBaitColors(Set<String> keys) {

        List<Integer> colorList = new ArrayList<>();
        // Add some predefined colors
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colorList.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colorList.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colorList.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colorList.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colorList.add(c);

        Map<String, Integer> baitColors = new HashMap<>();
        String[] baits = getResources().getStringArray(R.array.bait_array);
        for (int i = 0; i < baits.length; i++) {
            baitColors.put(baits[i], colorList.get(i));
        }

        Map<String, Integer> colorMap = new LinkedHashMap<>(keys.size());
        for (String key : keys) {
            String[] parts = key.split(":");
            String bait = parts[0];
            String contactType = parts[1];
            int baseColor = baitColors.get(bait).intValue();

            // Generate shades for CATCH, FOLLOW, and CONTACT
            int catchColor = baseColor;
            int followColor = adjustColor(baseColor, 0.85f); // Slightly darker
            int contactColor = adjustColor(baseColor, 0.7f); // Even darker

            // Assign colors based on contact type
            switch (contactType) {
                case "CATCH":
                    colorMap.put(key, catchColor);
                    break;
                case "FOLLOW":
                    colorMap.put(key, followColor);
                    break;
                case "CONTACT":
                    colorMap.put(key, contactColor);
                    break;
            }
        }
        return colorMap;
    }

    private void updateCounts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("Username", "Angler");
        String lake = prefs.getString("Lake", "");

        binding.userDailyLabel.setText(String.format("%s's Daily Count", username));
        binding.userDailyCatch.setText(pointsHelper.getDailyCatch(username));
        binding.userDailyContact.setText(pointsHelper.getDailyContact(username));
        binding.userDailyFollow.setText(pointsHelper.getDailyFollow(username));

        binding.dailyCatch.setText(pointsHelper.getDailyCatch());
        binding.dailyContact.setText(pointsHelper.getDailyContact());
        binding.dailyFollow.setText(pointsHelper.getDailyFollow());

        Calendar[] trip_range = DateRangePreference.parseDateRange(prefs.getString("my_date_range_preference", "2020-01-01,2020-12-31"));
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        binding.tripDateRange.setText(String.format("%s - %s", format.format(trip_range[0].getTime()), format.format(trip_range[1].getTime())));

        binding.tripCatch.setText(pointsHelper.getTripCatch(trip_range));
        binding.tripContact.setText(pointsHelper.getTripContact(trip_range));
        binding.tripFollow.setText(pointsHelper.getTripFollow(trip_range));

        binding.totalCatch.setText(pointsHelper.getTotalCatch(lake));
        binding.totalContact.setText(pointsHelper.getTotalContact(lake));
        binding.totalFollow.setText(pointsHelper.getTotalFollow(lake));
    }

    private void buildLeaderBoard() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Calendar[] trip_range = DateRangePreference.parseDateRange(prefs.getString("my_date_range_preference", "2020-01-01,2020-12-31"));

        List<Point> points = pointsHelper.getPointsForTrip(trip_range, prefs.getString("Lake", ""));

        // Top anglers by inches caught
        String max = maxLen(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %.2f\"", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.maxLen.setText(max);

        // Top anglers by average caught
        String averageLen = average(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %.2f\"", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.averageLen.setText(averageLen);

        // Top anglers by catches
        String mostCatches = mostCatches(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostCatches.setText(mostCatches);

        // Top anglers who lost fish
        String mostLosses = mostLosses(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostLosses.setText(mostLosses);

        // Top anglers by follows
        String mostFollows = mostFollows(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostFollows.setText(mostFollows);

        // Catches only
        // Map<String, Long> caught_baits = points.stream().filter(point -> point.getContactType().equals(ContactType.CATCH.toString())).collect(Collectors.groupingBy(point -> point.getBait().trim(), Collectors.counting()));

        // Just bait, all contacts combined
        // Map<String, Long> all_contact_baits = points.stream().collect(Collectors.groupingBy(point -> point.getBait().trim(), Collectors.counting()));

        // Bait and Contact
        Map<String, Long> baits = points.stream().collect(Collectors.groupingBy(point -> point.getBait().trim() + ":" + point.getContactType().trim(), TreeMap::new, Collectors.counting()));
        displayPieChart(baits, points.size());
    }

    private Map<String, Double> average(List<Point> points) {
        Map<String, Double> average = new LinkedHashMap<>();
        Map<String, Double> intermediateInches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(),
                                               Collectors.averagingDouble(point -> point.getFishSizeAsDouble())));
        intermediateInches.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue()
                                .reversed()).forEachOrdered(e -> average.put(e.getKey(), e.getValue()));
        return average;
    }

    private Map<String, Double> maxLen(List<Point> points) {
        Map<String, Double> maxLens = new LinkedHashMap<>();
        Map<String, Double> finalMaxLens = new LinkedHashMap<>();
        Map<String, Point> intermediateInches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.toMap(Point::getName, Function.identity(),
                                          BinaryOperator.maxBy(Comparator.comparing(Point::getFishSizeAsDouble))));
        intermediateInches.keySet().stream().forEach(k -> maxLens.put(k, intermediateInches.get(k).getFishSizeAsDouble()));

        maxLens.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue()
                                .reversed()).forEachOrdered(e -> finalMaxLens.put(e.getKey(), e.getValue()));
        return finalMaxLens;
    }

    private Map<String, Long> mostCatches(List<Point> points) {
        Map<String, Long> intermediateCatches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(), Collectors.counting()));
        Map<String, Long> topCatches = new LinkedHashMap<>();
        intermediateCatches.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                                .reversed()).forEachOrdered(e -> topCatches.put(e.getKey(), e.getValue()));
        return topCatches;
    }

    private Map<String, Long> mostLosses(List<Point> points) {
        Map<String, Long> intermediateLosses = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CONTACT.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(), Collectors.counting()));
        Map<String, Long> topLosses = new LinkedHashMap<>();
        intermediateLosses.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                                .reversed()).forEachOrdered(e -> topLosses.put(e.getKey(), e.getValue()));
        Log.d(TAG, "Most losses: " + topLosses);
        return topLosses;
    }

    private Map<String, Long> mostFollows(List<Point> points) {
        Map<String, Long> intermediateFollows = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.FOLLOW.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(), Collectors.counting()));
        Map<String, Long> topFollows = new LinkedHashMap<>();
        intermediateFollows.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                                .reversed()).forEachOrdered(e -> topFollows.put(e.getKey(), e.getValue()));
        return topFollows;
    }
}