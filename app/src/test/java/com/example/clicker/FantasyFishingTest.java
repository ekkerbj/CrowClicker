package com.example.clicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FantasyFishingTest {
    List<List<Object>> sheetData = new ArrayList<>();
/*
FF Selections
Dan               Tony         Jeff
S. Gateway (F)    Adams (C)    New Spot(V)
3 Point           Careys       Chase(C,V)

Catch -   angler, size , location, owner, video
1           Amy,    41,     Careys,  Tony,  false
2          Tony,    41,     Careys,  Tony,  true
3           Dan,    41,   Adams (C),  Dan,  false
Result -    Dan          Tony        Jeff
1             0            41           0
2             0            51           0
2             41            0           0


  */

    @Before
    public void before() {
        List<Object> header = new ArrayList<>();
        header.add( "Dan" );
        header.add( "Tony" );
        header.add( "Jeff" );
        sheetData.add( header );
        List<Object> row1 = new ArrayList<>();
        row1.add( "S. Gateway (F)" );
        row1.add( "Adams (C)" );
        row1.add( "New Spot(V)" );
        sheetData.add( row1 );

        List<Object> row2 = new ArrayList<>();
        row2.add( "3 Point" );
        row2.add( "Careys" );
        row2.add( "Chase(C,V)" );
        sheetData.add( row2 );
    }

    @Test
    public void testLoadAnglers() {
        FantasyFishing ff = new FantasyFishing();
        ff.loadAnglers( sheetData );
        assertEquals( 3, ff.anglers.size() );
        assertEquals( 2, ff.anglers.get( "Dan" ).size() );
        assertTrue( ff.anglers.get( "Dan" ).get( 0 ).isFranchise );
        assertTrue( ff.anglers.get( "Jeff" ).get( 1 ).isCommunity );
        assertTrue( ff.anglers.get( "Jeff" ).get( 1 ).isVirgin );
    }

    @Test
    public void testGetOwners() {
        FantasyFishing ff = new FantasyFishing();
        ff.loadAnglers( sheetData );
        String[] owners = ff.getOwners();
        assertEquals( 3, owners.length );
        assertEquals( "Dan", owners[ 0 ] );
        assertEquals( "Tony", owners[ 1 ] );
        assertEquals( "Jeff", owners[ 2 ] );
    }

    @Test
    public void testGetLocations() {
        FantasyFishing ff = new FantasyFishing();
        ff.loadAnglers( sheetData );
        String[] locations = ff.getLocations();
        assertEquals( 6, locations.length );
        assertEquals( "S. Gateway (F)", locations[ 0 ] );
        assertEquals( "3 Point", locations[ 1 ] );
        assertEquals( "Adams (C)", locations[ 2 ] );
        assertEquals( "Careys", locations[ 3 ] );
        assertEquals( "New Spot(V)", locations[ 4 ] );
        assertEquals( "Chase(C,V)", locations[ 5 ] );
    }

    @Test
    public void testScoreCatch() {
        FantasyFishing ff = new FantasyFishing();
        ff.loadAnglers( sheetData );
        List<String> row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Amy" );
        row1.add( "41" );
        row1.add( "Careys" );
        row1.add( "Tony" );
        row1.add( "" );
        row1.add( "41.0" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Amy", "Careys", "41", "Tony", "June 6", false, false, false ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Tony" );
        row1.add( "41" );
        row1.add( "Careys" );
        row1.add( "Tony" );
        row1.add( "" );
        row1.add( "51.0" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Tony", "Careys", "41", "Tony", "June 6", true, false, false ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Dan" );
        row1.add( "41" );
        row1.add( "S. Gateway (F)" );
        row1.add( "Dan" );
        row1.add( "82.0" );
        row1.add( "" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Dan", "S. Gateway (F)", "41", "Dan", "June 6", false, false, false ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Dan" );
        row1.add( "41" );
        row1.add( "Adams (C)" );
        row1.add( "Dan" );
        row1.add( "41.0" );
        row1.add( "" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Dan", "Adams (C)", "41", "Dan", "June 6", false, false, false ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Dan" );
        row1.add( "41" );
        row1.add( "Careys" );
        row1.add( "Dan" );
        row1.add( "27.5" );
        row1.add( "25.5" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Dan", "Careys", "41", "Dan", "June 6", true, false, true ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Jeff" );
        row1.add( "42" );
        row1.add( "New Spot(V)" );
        row1.add( "Jeff" );
        row1.add( "" );
        row1.add( "" );
        row1.add( "64.0" );
        assertEquals( row1, ff.scoreCatch( "Jeff", "New Spot(V)", "42", "Jeff", "June 6", true, true, true ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Calvin" );
        row1.add( "42" );
        row1.add( "Chase(C,V)" );
        row1.add( "Dan" );
        row1.add( "52.0" );
        row1.add( "" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Calvin", "Chase(C,V)", "42", "Dan", "June 6", false, false, false ) );
        row1 = new ArrayList<>();
        // date, angler, size , location, owner , ... anglers
        row1.add( "June 6" );
        row1.add( "Tony" );
        row1.add( "42" );
        row1.add( "S. Gateway (F)" );
        row1.add( "Dan" );
        row1.add( "52.0" );
        row1.add( "28.0" );
        row1.add( "" );
        assertEquals( row1, ff.scoreCatch( "Tony", "S. Gateway (F)", "42", "Dan", "June 6", true, true, true ) );
    }
}

