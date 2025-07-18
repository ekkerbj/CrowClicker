package com.example.clicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FantasyFishingTest {
    List<List<Object>> sheetData = new ArrayList<>();
    List<List<Object>> standings = new ArrayList<>();
    FantasyFishing ff;
/*
FF Selections
Dan               Tony         Jeff
S. Gateway (F)    Adams (C)    New Spot(V)
3 Point           Carey's       Chase(C,V)

Catch -   angler, size , location, owner, video
1           Amy,    41,     Carey's,  Tony,  false
2          Tony,    41,     Carey's,  Tony,  true
3           Dan,    41,   Adams (C),  Dan,  false
Result -    Dan          Tony        Jeff
1             0            41           0
2             0            51           0
2             41            0           0


  */

    @Before
    public void before() {
        List<Object> header = new ArrayList<>();
        header.add("Selection");
        header.add("Dan");
        header.add("Tony");
        header.add("Jeff");
        sheetData.add(header);
        List<Object> row1 = new ArrayList<>();
        row1.add("1");
        row1.add("S. Gateway (F)");
        row1.add("Adams (C)");
        row1.add("New Spot(V)");
        sheetData.add(row1);

        List<Object> row2 = new ArrayList<>();
        row2.add("2");
        row2.add("3 Point");
        row2.add("Carey's");
        row2.add("Chase(C,V)");
        sheetData.add(row2);
        ff = new FantasyFishing();
        ff.setStandings(standings);
        ff.loadAnglers(sheetData);
    }

    @Test
    public void testLoadAnglers() {
        assertEquals(3, ff.anglers.size());
        assertEquals(2, ff.anglers.get("Dan").size());
        assertTrue(ff.anglers.get("Dan").get(0).isFranchise);
        assertTrue(ff.anglers.get("Jeff").get(1).isCommunity);
        assertTrue(ff.anglers.get("Jeff").get(1).isVirgin);
    }

    @Test
    public void testGetOwners() {
        String[] owners = ff.getOwners();
        assertEquals(4, owners.length);
        assertEquals("Dan", owners[1]);
        assertEquals("Tony", owners[2]);
        assertEquals("Jeff", owners[3]);
    }

    @Test
    public void testGetLocations() {
        String[] locations = ff.getLocations();
        assertEquals(7, locations.length);
        assertEquals("S. Gateway (F) : Dan", locations[1]);
        assertEquals("3 Point : Dan", locations[2]);
        assertEquals("Adams (C) : Tony", locations[3]);
        assertEquals("Carey's : Tony", locations[4]);
        assertEquals("New Spot(V) : Jeff", locations[5]);
        assertEquals("Chase(C,V) : Jeff", locations[6]);
    }

    @Test
    public void testOnlyOneVirgin() {


        standings = new ArrayList<>();
        List<Object> srow1 = new ArrayList<>();
        srow1.add("Date");
        srow1.add("Angler");
        srow1.add("Size");
        srow1.add("Location");
        srow1.add("Team");
        srow1.add("Dan");
        srow1.add("Tony");
        srow1.add("Jeff");
        srow1.add("Bonus");
        List<Object> srow2 = new ArrayList<>();
        srow2.add("June 6");
        srow2.add("Dan");
        srow2.add("20");
        srow2.add("New Spot(V)");
        srow2.add("Dan");
        srow2.add("");
        srow2.add("20");
        srow2.add("");
        srow2.add(" Video LifeVest Virgin");
        standings.add(srow1);
        standings.add(srow2);
        ff = new FantasyFishing();
        ff.setStandings(standings);
        ff.loadAnglers(sheetData);
        List<String> row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Amy");
        row1.add("41.75");
        row1.add("New Spot(V)");
        row1.add("Tony");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("21.0");
        row1.add("20.75");
        row1.add("");
        assertEquals(row1, ff.scoreCatch("Amy", "New Spot(V)", "41.75", "Tony", "Muskellunge", "8/9/2024 23:11:00", false, false));
    }

    @Test
    public void testScoreCatch() {
        List<String> row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Amy");
        row1.add("41.75");
        row1.add("Carey's");
        row1.add("Tony");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("41.75");
        row1.add("");
        row1.add("");
        assertEquals(row1, ff.scoreCatch("Amy", "Carey's", "41.75", "Tony", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Tony");
        row1.add("41.25");
        row1.add("Carey's");
        row1.add("Tony");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("51.25");
        row1.add("");
        row1.add(" Video");
        assertEquals(row1, ff.scoreCatch("Tony", "Carey's", "41.25", "Tony", "Muskellunge", "8/9/2024 23:11:00", true, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Dan");
        row1.add("41.75");
        row1.add("S. Gateway (F)");
        row1.add("Dan");
        row1.add("Muskellunge");
        row1.add("83.5");
        row1.add("");
        row1.add("");
        row1.add(" Franchise");
        assertEquals(row1, ff.scoreCatch("Dan", "S. Gateway (F)", "41.75", "Dan", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Dan");
        row1.add("41");
        row1.add("Adams (C)");
        row1.add("Dan");
        row1.add("Muskellunge");
        row1.add("41.0");
        row1.add("");
        row1.add("");
        row1.add(" Community");
        assertEquals(row1, ff.scoreCatch("Dan", "Adams (C)", "41", "Dan", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Dan");
        row1.add("41.25");
        row1.add("Carey's");
        row1.add("Dan");
        row1.add("Muskellunge");
        row1.add("32.75");
        row1.add("20.5");
        row1.add("");
        row1.add(" Video LifeVest");
        assertEquals(row1, ff.scoreCatch("Dan", "Carey's", "41.25", "Dan", "Muskellunge", "8/9/2024 23:11:00", true, true));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Jeff");
        row1.add("42");
        row1.add("New Spot(V)");
        row1.add("Jeff");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("");
        row1.add("64.0");
        row1.add(" Video LifeVest Virgin");
        assertEquals(row1, ff.scoreCatch("Jeff", "New Spot(V)", "42", "Jeff", "Muskellunge", "8/9/2024 23:11:00", true, true));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Calvin");
        row1.add("42");
        row1.add("Chase(C,V)");
        row1.add("Dan");
        row1.add("Muskellunge");
        row1.add("52.0");
        row1.add("");
        row1.add("");
        row1.add(" Virgin Community");
        assertEquals(row1, ff.scoreCatch("Calvin", "Chase(C,V)", "42", "Dan", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Tony");
        row1.add("42.75");
        row1.add("S. Gateway (F)");
        row1.add("Dan");
        row1.add("Muskellunge");
        row1.add("42.5");
        row1.add("33.5");
        row1.add("");
        row1.add(" Video LifeVest Franchise");
        assertEquals(row1, ff.scoreCatch("Tony", "S. Gateway (F)", "42.75", "Dan", "Muskellunge", "8/9/2024 23:11:00", true, true));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Tony");
        row1.add("30");
        row1.add("Chase(C,V)");
        row1.add("Tony");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("40.0");
        row1.add("");
        row1.add(" Virgin Community");
        assertEquals(row1, ff.scoreCatch("Tony", "Chase(C,V)", "30", "Tony", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Tony");
        row1.add("30.25");
        row1.add("New Spot(V)");
        row1.add("Tony");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("25.25");
        row1.add("15.0");
        row1.add(" Virgin");
        assertEquals(row1, ff.scoreCatch("Tony", "New Spot(V)", "30.25", "Tony", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Calvin");
        row1.add("32");
        row1.add("S. Gateway (F)");
        row1.add("Dan");
        row1.add("Muskellunge");
        row1.add("");
        row1.add("");
        row1.add("");
        row1.add(" Franchise");
        assertEquals(row1, ff.scoreCatch("Calvin", "S. Gateway (F)", "32", "Dan", "Muskellunge", "8/9/2024 23:11:00", false, false));
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add("8/9/2024 23:11:00");
        row1.add("Dan");
        row1.add("41.25");
        row1.add("Carey's");
        row1.add("Dan");
        row1.add("Lake Trout");
        row1.add("41.0");
        row1.add("");
        row1.add("");
        row1.add(" Video LifeVest");
        assertEquals(row1, ff.scoreCatch("Dan", "Carey's", "41.25", "Dan", "Lake Trout", "8/9/2024 23:11:00", true, true));
    }
}

