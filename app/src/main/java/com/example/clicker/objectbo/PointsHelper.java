package com.example.clicker.objectbo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.clicker.ContactType;
import com.example.clicker.ObjectBoxApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;
import io.objectbox.query.QueryCondition;

public class PointsHelper {
    private final Box<Point> pointBox;
    private final SharedPreferences prefs;

    public PointsHelper(Context context) {
        BoxStore boxStore = ((ObjectBoxApp) context.getApplicationContext()).getBoxStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        pointBox = boxStore.boxFor(Point.class);
    }

    public String getDailyCatch() {
        return retrieveDaily(ContactType.CATCH, null, prefs.getString("Lake", ""));
    }

    public String getDailyContact() {
        return retrieveDaily(ContactType.CONTACT, null, prefs.getString("Lake", ""));
    }

    public String getDailyFollow() {
        return retrieveDaily(ContactType.FOLLOW, null, prefs.getString("Lake", ""));
    }

    public String getDailyCatch(String angler) {
        return retrieveDaily(ContactType.CATCH, angler, prefs.getString("Lake", ""));
    }

    public String getDailyContact(String angler) {
        return retrieveDaily(ContactType.CONTACT, angler, prefs.getString("Lake", ""));
    }

    public String getDailyFollow(String angler) {
        return retrieveDaily(ContactType.FOLLOW, angler, prefs.getString("Lake", ""));
    }

    public String getTripCatch(Calendar[] trip_range) {
        return retrieveTrip(trip_range, ContactType.CATCH, prefs.getString("Lake", ""));
    }

    public String getTripContact(Calendar[] trip_range) {
        return retrieveTrip(trip_range, ContactType.CONTACT, prefs.getString("Lake", ""));
    }

    public String getTripFollow(Calendar[] trip_range) {
        return retrieveTrip(trip_range, ContactType.FOLLOW, prefs.getString("Lake", ""));
    }

    public String getTotalCatch(String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(ContactType.CATCH.toString())
                .and(Point_.lake.equal(lake))
                .and(Point_.fishSize.notEqual(""));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public String getTotalContact(String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(ContactType.CONTACT.toString()).and(Point_.lake.equal(lake));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public String getTotalFollow(String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(ContactType.FOLLOW.toString()).and(Point_.lake.equal(lake));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public Point getPointById(long id) {
        return pointBox.query().equal(Point_.id, id).build().findUnique();
    }

    public void addOrUpdatePoint(Point point) {
        pointBox.put(point);
    }

    public void clearPoints(String lake) {
        pointBox.query(Point_.lake.equal(lake)).build().remove();
    }

    public long clearAllPointsOf(ContactType type, String lake) {
        return pointBox.query(Point_.contactType.equal(type.toString()).and(Point_.sheetId.notEqual(0)).and(Point_.lake.equal(lake))).build().remove();
    }

    public void deletePoint(long id) {
        Point point = getPointById(id);
        if (point != null) {
            pointBox.remove(id);
        }
    }

    public List<Point> getAll() {
        return pointBox.query().build().find();
    }

    public ArrayList<Point> getPointsForTrip(Calendar[] trip_range, String lake) {
        int flags = QueryBuilder.NULLS_LAST | QueryBuilder.DESCENDING;

        List<Point> tempPoints = pointBox.query(Point_.lake.equal(lake))
                .order(Point_.timeStamp, flags)
                .between(Point_.timeStamp, trip_range[0].getTime(), trip_range[1].getTime())
                .build().find();
        ArrayList<Point> points = new ArrayList<>();
        for (Point p : tempPoints) {
            if (!p.getName().equalsIgnoreCase("Label") && !p.getName().equalsIgnoreCase("FF"))
                points.add(p);
        }
        return points;
    }

    private String retrieveDaily(ContactType type, String angler, String lake) {
        Calendar[] trip_range = new Calendar[2];
        trip_range[0] = Calendar.getInstance(Locale.US);
        trip_range[0].set(Calendar.HOUR_OF_DAY, 0);
        trip_range[0].set(Calendar.MINUTE, 0);
        trip_range[0].set(Calendar.SECOND, 0);
        trip_range[0].set(Calendar.MILLISECOND, 0);

        trip_range[1] = Calendar.getInstance(Locale.US);
        trip_range[1].set(Calendar.HOUR_OF_DAY, 23);
        trip_range[1].set(Calendar.MINUTE, 59);
        trip_range[1].set(Calendar.SECOND, 59);
        trip_range[1].set(Calendar.MILLISECOND, 999);

        return retrieveFor(trip_range, type, angler, lake);
    }

    private String retrieveTrip(Calendar[] trip_range, ContactType type, String lake) {
        return retrieveFor(trip_range, type, null, lake);
    }

    private String retrieveFor(Calendar[] trip_range, ContactType type, String angler, String lake) {
        boolean haveAngler = (angler != null && !angler.trim().isEmpty());
        QueryCondition<Point> baseQuery = Point_.contactType.equal(type.toString())
                .and(Point_.timeStamp.between(trip_range[0].getTime(), trip_range[1].getTime()))
                .and(Point_.lake.equal(lake))
                .and(Point_.name.notEqual("FF"));
        if (haveAngler)
            baseQuery = baseQuery.and(Point_.name.equal(angler));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public List<Point> getAllLabels(String lake) {
        QueryCondition<Point> baseQuery = Point_.name.equal("label").and(Point_.lake.equal(lake));
        return pointBox.query(baseQuery).build().find();
    }

    public List<Point> getAllPointsOf(ContactType type, String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(type.toString()).and(Point_.lake.equal(lake));
        return pointBox.query(baseQuery).build().find();
    }

    public Collection<Point> getAllFFs(String lake) {
        QueryCondition<Point> baseQuery = Point_.name.equal("FF").and(Point_.lake.equal(lake));
        return pointBox.query(baseQuery).build().find();
    }
}
