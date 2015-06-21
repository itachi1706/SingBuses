package com.itachi1706.busarrivalsg.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopsGeoObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kenneth on 21/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.Database
 */
public class BusStopsGeoDB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    //DB Name
    public static final String DATABASE_NAME = "appdb.db";

    //DB table Name
    public static final String TABLE_ITEMS = "BusStopsGeo";

    //Bus Stops Table Column Names
    public static final String GEO_NO = "code";
    public static final String GEO_LAT = "lat";
    public static final String GEO_LNG = "lng";
    public static final String GEO_NAME = "name";

    public BusStopsGeoDB(Context context){
        super(context, context.getExternalFilesDir(null) + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BUS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "(" + GEO_NO + " TEXT PRIMARY KEY,"
                + GEO_LAT + " TEXT," + GEO_LNG + " TEXT," + GEO_NAME + " TEXT);";
        db.execSQL(CREATE_BUS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public void dropAndRebuildDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    private void addFromJSON(BusStopsGeoObject busStop){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GEO_NO, busStop.getNo());
        cv.put(GEO_LAT, busStop.getLat());
        cv.put(GEO_LNG, busStop.getLng());
        cv.put(GEO_NAME, busStop.getName());
        db.insert(TABLE_ITEMS, null, cv);
        db.close();
    }

    public boolean checkIfExistAlready(BusStopsGeoObject busStop){
        String code = busStop.getNo();
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + GEO_NO + "='" + code + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count != 0;
    }

    /**
     * Adds to the database if the record do not exist
     * @param busStop The object to compare and add
     */
    public void addToDBIfNotExist(BusStopsGeoObject busStop){
        if (checkIfExistAlready(busStop))
            return;
        addFromJSON(busStop);
    }

    /**
     * Gets all Bus Stops in the database
     * @return An ArrayList of all bus stops in the DB
     */
    public ArrayList<BusStopsGeoObject> getAllBusStops(){
        String query = "SELECT * FROM " + TABLE_ITEMS;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopsGeoObject> results = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do {
                BusStopsGeoObject bs = new BusStopsGeoObject();
                bs.setNo(cursor.getString(0));
                bs.setLat(cursor.getString(1));
                bs.setLat(cursor.getString(2));
                bs.setName(cursor.getString(3));

                results.add(bs);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return results;
    }

    /**
     * Returns a single Bus Stop Object based on Unique Bus Stop Code
     * @param busStopCode Bus Stop Code
     * @return Bus Stop Object
     */
    public BusStopsGeoObject getBusStopByBusStopCode(String busStopCode){
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + GEO_NO + "='" + busStopCode + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopsGeoObject bs = new BusStopsGeoObject();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                bs.setNo(cursor.getString(0));
                bs.setLat(cursor.getString(1));
                bs.setLat(cursor.getString(2));
                bs.setName(cursor.getString(3));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bs;
    }

    /**
     * Returns a single Bus Stop Object based on Bus Stop Name
     * @param stopName Bus Stop Name
     * @return Bus Stop Object
     */
    public BusStopsGeoObject getBusStopByStopName(String stopName){
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + GEO_NAME + "='" + stopName + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopsGeoObject bs = new BusStopsGeoObject();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                bs.setNo(cursor.getString(0));
                bs.setLat(cursor.getString(1));
                bs.setLat(cursor.getString(2));
                bs.setName(cursor.getString(3));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bs;
    }

    /**
     * Updates an available bus stop if exist, else it adds it into the database
     * @param busStop The Bus Stop to update
     */
    public void updateBusStop(BusStopsGeoObject busStop){
        if (checkIfExistAlready(busStop)) {
            //Remove and update
            String query = "DELETE FROM " + TABLE_ITEMS + " WHERE " + GEO_NO + "='" + busStop.getNo() + "';";
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(query);
            db.close();
        }
        //ADD
        addFromJSON(busStop);
    }

    public int getSize(){
        String query = "SELECT * FROM " + TABLE_ITEMS + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }
}
