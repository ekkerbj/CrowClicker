package com.example.clicker.objectbo;

import android.content.Context;

import com.example.clicker.ContactType;
import com.example.clicker.ObjectBoxApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;

public class PointsHelper {
    private final Box<Point> pointBox;

    public PointsHelper(Context context) {
        BoxStore boxStore = ((ObjectBoxApp) context.getApplicationContext()).getBoxStore();
        pointBox = boxStore.boxFor(Point.class);
    }

    public PointsHelper(Box<Point> pointBox) {
        this.pointBox = pointBox;
    }

    public String getDailyCatch() {
        return retrieveDaily(ContactType.CATCH);
    }

    public String getDailyContact() {
        return retrieveDaily(ContactType.CONTACT);
    }

    public String getDailyFollow() {
        return retrieveDaily(ContactType.FOLLOW);
    }

    public String getTripCatch(int tripLength) {
        return retrieveTrip(tripLength, ContactType.CATCH);
    }

    public String getTripContact(int tripLength) {
        return retrieveTrip(tripLength, ContactType.CONTACT);
    }

    public String getTripFollow(int tripLength) {
        return retrieveTrip(tripLength, ContactType.FOLLOW);
    }

    public String getTotalCatch() {
        return Long.toString(pointBox.query().equal(Point_.contactType, ContactType.CATCH.toString()).build().count());
    }

    public String getTotalContact() {
        return Long.toString(pointBox.query().equal(Point_.contactType, ContactType.CONTACT.toString()).build().count());
    }

    public String getTotalFollow() {
        return Long.toString(pointBox.query().equal(Point_.contactType, ContactType.FOLLOW.toString()).build().count());
    }

    public Point getPointById(long id) {
        return pointBox.query().equal(Point_.id, id).build().findUnique();
    }

    public void addOrUpdatePoint(Point point) {
        pointBox.put(point);
    }

    public void clearPoints() {
        pointBox.removeAll();
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

    public ArrayList<Point> getPointsForTrip(int tripLength) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        int flags = QueryBuilder.NULLS_LAST | QueryBuilder.DESCENDING;

        List<Point> tempPoints = pointBox.query()
                .order(Point_.timeStamp, flags)
                .greater(Point_.timeStamp, today.getTime())
                .build().find();
        ArrayList<Point> points = new ArrayList<>();
        for (Point p : tempPoints) {
            if (!p.getName().equalsIgnoreCase("Label"))
                points.add(p);
        }
        return points;
    }

    private String retrieveDaily(ContactType type) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        return retrieveFor(today, type);
    }

    private String retrieveTrip(int tripLength, ContactType type) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        return retrieveFor(today, type);
    }

    private String retrieveFor(Calendar date, ContactType type) {
        return Long.toString(pointBox.query().equal(Point_.contactType, type.toString()).greater(Point_.timeStamp, date.getTime()).build().count());
    }
}
