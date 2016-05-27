package com.itachi1706.busarrivalsg.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.Database
 */
public class BusStopsDB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    //DB Name
    public static final String DATABASE_NAME = "appdb.db";

    //DB table Name
    public static final String TABLE_ITEMS = "BusStops";

    //Bus Stops Table Column Names
    public static final String CODE_ID = "id";
    public static final String BUS_STOP_CODE = "busCode";
    public static final String BUS_STOP_ROAD = "busRoad";
    public static final String BUS_STOP_DESC = "busDesc";
    public static final String BUS_STOP_SUMMARY = "busSummary";
    public static final String BUS_STOP_CREATE_DATE = "busStopDate";

    public BusStopsDB(Context context){
        super(context, context.getExternalFilesDir(null) + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BUS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "(" + CODE_ID + " INTEGER PRIMARY KEY,"
                + BUS_STOP_CODE + " TEXT," + BUS_STOP_ROAD + " TEXT," + BUS_STOP_DESC + " TEXT," + BUS_STOP_SUMMARY + " TEXT,"
                + BUS_STOP_CREATE_DATE + " TEXT);";
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

    private void addFromJSON(BusStopJSON busStop){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CODE_ID, busStop.getBusStopCodeID());
        cv.put(BUS_STOP_CODE, busStop.getCode());
        cv.put(BUS_STOP_ROAD, busStop.getRoad());
        cv.put(BUS_STOP_DESC, busStop.getBusStopName());
        cv.put(BUS_STOP_SUMMARY, busStop.getSummary());
        cv.put(BUS_STOP_CREATE_DATE,busStop.getCreateDate());
        db.insert(TABLE_ITEMS, null, cv);
        db.close();
    }

    public boolean checkIfExistAlready(BusStopJSON busStop){
        int code = busStop.getBusStopCodeID();
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + CODE_ID + "=" + code + ";";
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
    public void addToDBIfNotExist(BusStopJSON busStop){
        if (checkIfExistAlready(busStop))
            return;
        addFromJSON(busStop);
    }

    /**
     * Gets all Bus Stops in the database
     * @return An ArrayList of all bus stops in the DB
     */
    public ArrayList<BusStopJSON> getAllBusStops(){
        String query = "SELECT * FROM " + TABLE_ITEMS;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopJSON> results = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do {
                BusStopJSON bs = new BusStopJSON();
                bs.setBusStopCodeID(cursor.getInt(0));
                bs.setCode(cursor.getString(1));
                bs.setRoad(cursor.getString(2));
                bs.setDescription(cursor.getString(3));
                bs.setSummary(cursor.getString(4));
                bs.setCreateDate(cursor.getString(5));

                results.add(bs);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return results;
    }

    /**
     * Returns a single Bus Stop Object based on Unique Bus Stop ID
     * @param busStopCode Bus Stop ID
     * @return Bus Stop Object
     */
    public BusStopJSON getBusStopByCode(int busStopCode){
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + CODE_ID + "=" + busStopCode + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopJSON bs = new BusStopJSON();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                bs.setBusStopCodeID(cursor.getInt(0));
                bs.setCode(cursor.getString(1));
                bs.setRoad(cursor.getString(2));
                bs.setDescription(cursor.getString(3));
                bs.setSummary(cursor.getString(4));
                bs.setCreateDate(cursor.getString(5));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bs;
    }

    /**
     * Returns a single Bus Stop Object based on Unique Bus Stop Code
     * @param busStopCode Bus Stop Code
     * @return Bus Stop Object
     */
    public BusStopJSON getBusStopByBusStopCode(String busStopCode){
        busStopCode = DatabaseUtils.sqlEscapeString(busStopCode);
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_CODE + "='" + busStopCode + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopJSON bs = new BusStopJSON();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                bs.setBusStopCodeID(cursor.getInt(0));
                bs.setCode(cursor.getString(1));
                bs.setRoad(cursor.getString(2));
                bs.setDescription(cursor.getString(3));
                bs.setSummary(cursor.getString(4));
                bs.setCreateDate(cursor.getString(5));
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
    public BusStopJSON getBusStopByStopName(String stopName){
        stopName = DatabaseUtils.sqlEscapeString(stopName);
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_DESC + "='" + stopName + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopJSON bs = new BusStopJSON();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                bs.setBusStopCodeID(cursor.getInt(0));
                bs.setCode(cursor.getString(1));
                bs.setRoad(cursor.getString(2));
                bs.setDescription(cursor.getString(3));
                bs.setSummary(cursor.getString(4));
                bs.setCreateDate(cursor.getString(5));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bs;
    }

    /**
     * Returns a list of Bus Stop Object based on Bus Stop Name
     * @param stopName Bus Stop Name
     * @return Bus Stop Object ArrayList
     */
    public ArrayList<BusStopJSON> getBusStopsByStopName(String stopName){
        stopName = DatabaseUtils.sqlEscapeString(stopName);
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_DESC + "='" + stopName + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopJSON> result = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                BusStopJSON bs = new BusStopJSON();
                bs.setBusStopCodeID(cursor.getInt(0));
                bs.setCode(cursor.getString(1));
                bs.setRoad(cursor.getString(2));
                bs.setDescription(cursor.getString(3));
                bs.setSummary(cursor.getString(4));
                bs.setCreateDate(cursor.getString(5));

                result.add(bs);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * Returns a list of Bus Stops based on query string
     * @param query Query String
     * @return Bus Stop Object ArrayList
     */
    public ArrayList<BusStopJSON> getBusStopsByQuery(String query){
        query = DatabaseUtils.sqlEscapeString(query);
        String queryString = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_CODE + " LIKE '%" + query + "%' COLLATE NOCASE OR "
                + BUS_STOP_ROAD + " LIKE '%" + query + "%' COLLATE NOCASE OR " + BUS_STOP_DESC + " LIKE '%" + query + "%' COLLATE NOCASE;";
        System.out.println("DB QUERY-STRING: "+ queryString);
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopJSON> result = new ArrayList<>();

        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                BusStopJSON bs = new BusStopJSON();
                bs.setBusStopCodeID(cursor.getInt(0));
                bs.setCode(cursor.getString(1));
                bs.setRoad(cursor.getString(2));
                bs.setDescription(cursor.getString(3));
                bs.setSummary(cursor.getString(4));
                bs.setCreateDate(cursor.getString(5));

                result.add(bs);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * Updates an available bus stop if exist, else it adds it into the database
     * @param busStop The Bus Stop to update
     */
    public void updateBusStop(BusStopJSON busStop){
        if (checkIfExistAlready(busStop)) {
            //Remove and update
            String query = "DELETE FROM " + TABLE_ITEMS + " WHERE " + CODE_ID + "=" + busStop.getBusStopCodeID() + ";";
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
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
