package com.itachi1706.busarrivalsg.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.Database
 */
public class BusStopsDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    //DB Name
    private static final String DATABASE_NAME = "appdb.db";

    //DB table Name
    private static final String TABLE_ITEMS = "BusStops";

    //Bus Stops Table Column Names
    private static final String CODE_ID = "id";
    private static final String BUS_STOP_CODE = "busStopCode";
    private static final String BUS_STOP_ROAD = "roadName";
    private static final String BUS_STOP_DESC = "description";
    private static final String BUS_STOP_LATITUDE = "latitude";
    private static final String BUS_STOP_LONGITUDE = "longitude";
    private static final String BUS_STOP_SERVICES = "services";
    private static final String BUS_STOP_TIMESTAMP = "timestamp";

    public BusStopsDB(Context context){
        super(context, context.getExternalFilesDir(null) + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BUS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "(" + CODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BUS_STOP_CODE + " TEXT," + BUS_STOP_ROAD + " TEXT," + BUS_STOP_DESC + " TEXT," + BUS_STOP_LATITUDE + " DOUBLE,"
                + BUS_STOP_LONGITUDE + " DOUBLE," + BUS_STOP_SERVICES + " TEXT," + BUS_STOP_TIMESTAMP + " INTEGER);";
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

    private void bulkAddFromJSON(BusStopJSON[] busStops) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_ITEMS + " (" + BUS_STOP_CODE + ", " + BUS_STOP_ROAD + ", "
                + BUS_STOP_DESC + ", " + BUS_STOP_LATITUDE + ", " + BUS_STOP_LONGITUDE + ", " + BUS_STOP_SERVICES + ", "
                + BUS_STOP_TIMESTAMP + ") VALUES (?,?,?,?,?,?,?);";
        db.beginTransaction();
        SQLiteStatement stmt = db.compileStatement(sql);
        for (BusStopJSON busStop : busStops) {
            LogHelper.d("DEBUG-BusStops", "Processing Bus Stop: " + busStop.getBusStopCode());
            stmt.bindString(1, busStop.getBusStopCode());
            stmt.bindString(2, busStop.getRoadName());
            stmt.bindString(3, busStop.getDescription());
            stmt.bindDouble(4, busStop.getLatitude());
            stmt.bindDouble(5, busStop.getLongitude());
            stmt.bindString(6, busStop.getServices());
            stmt.bindLong(7, busStop.getTimestamp());
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * Add an array of records to the database
     * @param busStops The array of objects to add
     */
    public void addMultipleToDB(BusStopJSON[] busStops) {
        bulkAddFromJSON(busStops);
    }

    private BusStopJSON getBusStopJsonObject(Cursor cursor) {
        BusStopJSON bs = new BusStopJSON();
        bs.setBusStopCode(cursor.getString(1));
        bs.setRoadName(cursor.getString(2));
        bs.setDescription(cursor.getString(3));
        bs.setLatitude(cursor.getDouble(4));
        bs.setLongitude(cursor.getDouble(5));
        bs.setServices(cursor.getString(6));
        bs.setTimestamp(cursor.getInt(7));
        return bs;
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
                results.add(getBusStopJsonObject(cursor));
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
    public BusStopJSON getBusStopByBusStopCode(String busStopCode){
        busStopCode = DatabaseUtils.sqlEscapeString(busStopCode);
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_CODE + "=" + busStopCode + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopJSON bs = new BusStopJSON();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                bs = getBusStopJsonObject(cursor);
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
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_DESC + " LIKE " + stopName + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopJSON> result = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                result.add(getBusStopJsonObject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * Returns a list of Bus Stop Object based on Service No
     * @param svcNo Service No
     * @param company Bus Service Company
     * @return Bus Stop Object ArrayList
     */
    public ArrayList<BusStopJSON> getBusStopsBySvcNo(String svcNo, String company){
        String concat = "%" + svcNo + ":" + company + "%";
        concat = DatabaseUtils.sqlEscapeString(concat);
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_SERVICES + " LIKE " + concat + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopJSON> result = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                result.add(getBusStopJsonObject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * Returns a bus stop object that is found
     * @param lng Longitude
     * @param lat Latitude
     * @return Bus Stop Object if found, null otherwise
     */
    public BusStopJSON getBusStopByLocation(double lng, double lat) {
        String longitude = lng + "";
        String latitude = lat + "";
        longitude = longitude.substring(0, longitude.length() - 2);
        latitude = latitude.substring(0, latitude.length() - 2);
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_LONGITUDE + " LIKE '" + longitude + "%' AND " + BUS_STOP_LATITUDE + " LIKE '" + latitude + "%';";
        SQLiteDatabase db = this.getReadableDatabase();
        BusStopJSON result = new BusStopJSON();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) return null;
        if (cursor.moveToFirst()) {
            do {
                result = getBusStopJsonObject(cursor);
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
        query = DatabaseUtils.sqlEscapeString("%" + query + "%");
        String queryString = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + BUS_STOP_CODE + " LIKE " + query + " COLLATE NOCASE OR "
                + BUS_STOP_ROAD + " LIKE " + query + " COLLATE NOCASE OR " + BUS_STOP_DESC + " LIKE " + query + " COLLATE NOCASE;";
        System.out.println("DB QUERY-STRING: "+ queryString);
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<BusStopJSON> result = new ArrayList<>();

        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.getCount() == 0)
            return null;
        if (cursor.moveToFirst()){
            do {
                result.add(getBusStopJsonObject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
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
