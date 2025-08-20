package com.itachi1706.busarrivalsg.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusStopJSON
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.LogHelper.w
import java.io.File

class BusStopsDb(context: Context) : SQLiteOpenHelper(
    context,
    context.getExternalFilesDir(null).toString() + File.separator + DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val TAG = "BusStopsDb"

        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "appdb.db"

        private const val TABLE_ITEMS: String = "BusStops"

        //Bus Stops Table Column Names
        private const val CODE_ID: String = "id"
        private const val BUS_STOP_CODE: String = "busStopCode"
        private const val BUS_STOP_ROAD: String = "roadName"
        private const val BUS_STOP_DESC: String = "description"
        private const val BUS_STOP_LATITUDE: String = "latitude"
        private const val BUS_STOP_LONGITUDE: String = "longitude"
        private const val BUS_STOP_SERVICES: String = "services"
        private const val BUS_STOP_TIMESTAMP: String = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBusTable = "CREATE TABLE $TABLE_ITEMS($CODE_ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, $BUS_STOP_CODE TEXT, $BUS_STOP_ROAD TEXT, $BUS_STOP_DESC " +
                "TEXT, $BUS_STOP_LATITUDE DOUBLE, $BUS_STOP_LONGITUDE DOUBLE, $BUS_STOP_SERVICES " +
                "TEXT, $BUS_STOP_TIMESTAMP INTEGER);"
        db.execSQL(createBusTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        recreateDb(db)
    }

    private fun recreateDb(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        onCreate(db)
    }

    fun dropAndRebuildDb() {
        val db = this.writableDatabase
        recreateDb(db)
    }

    fun truncateDb(): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_ITEMS, "1", null)
    }

    private fun bulkAddFromJson(busStops: Array<BusStopJSON>) {
        val db = this.writableDatabase
        val sql = "INSERT INTO $TABLE_ITEMS ($BUS_STOP_CODE, $BUS_STOP_ROAD, $BUS_STOP_DESC, " +
                "$BUS_STOP_LATITUDE, $BUS_STOP_LONGITUDE, $BUS_STOP_SERVICES, $BUS_STOP_TIMESTAMP) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        db.transaction {
            val stmt = compileStatement(sql)
            for (busStop in busStops) {
                stmt.bindString(1, busStop.busStopCode)
                stmt.bindString(2, busStop.roadName)
                stmt.bindString(3, busStop.description)
                stmt.bindDouble(4, busStop.latitude)
                stmt.bindDouble(5, busStop.longitude)
                stmt.bindString(6, busStop.services)
                stmt.bindLong(7, busStop.timestamp.toLong())
                stmt.executeInsert()
                stmt.clearBindings()
            }
        }
        db.close()
    }

    /**
     * Add array of records to the database
     * @param busStops Array of BusStopJSON objects to be added to the database
     */
    fun addMultipleToDb(busStops: Array<BusStopJSON>) {
        if (busStops.isEmpty()) {
            w(TAG, "No bus stops to add to the database")
            return
        }
        bulkAddFromJson(busStops)
    }

    private fun getBusStopJsonObject(cursor: Cursor): BusStopJSON {
        return BusStopJSON(
            busStopCode = cursor.getString(cursor.getColumnIndexOrThrow(BUS_STOP_CODE)),
            roadName = cursor.getString(cursor.getColumnIndexOrThrow(BUS_STOP_ROAD)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(BUS_STOP_DESC)),
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(BUS_STOP_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(BUS_STOP_LONGITUDE)),
            services = cursor.getString(cursor.getColumnIndexOrThrow(BUS_STOP_SERVICES)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(BUS_STOP_TIMESTAMP)).toInt()
        )
    }

    /**
     * Get all bus stops from the database
     * @return List of BusStopJSON objects representing all bus stops in the database
     */
    fun getAllBusStops(): List<BusStopJSON> {
        val query = "SELECT * FROM $TABLE_ITEMS"
        val db = this.readableDatabase
        val results = mutableListOf<BusStopJSON>()
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                results.add(getBusStopJsonObject(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return results
    }

    /**
     * Return single bus stop object based on Unique Bus Stop Code
     * @param busStopCode Unique bus stop code to search for
     * @return BusStopJSON object representing the bus stop with the given code or null if not found
     */
    fun getBusStopByBusStopCode(busStopCode: String?): BusStopJSON? {
        val query = "SELECT * FROM $TABLE_ITEMS WHERE $BUS_STOP_CODE = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, arrayOf(busStopCode))

        if (cursor.moveToFirst()) {
            val busStop = getBusStopJsonObject(cursor)
            cursor.close()
            db.close()
            return busStop
        }
        cursor.close()
        db.close()
        return null
    }

    /**
     * Return list of bus stop objects based on stop name
     * If no elements found will return empty array
     * @param stopName Name of the bus stop to search for
     * @return List of BusStopJSON objects representing bus stops with names matching the given stop
     */
    fun getBusStopsByStopName(stopName: String): List<BusStopJSON> {
        val query = "SELECT * FROM $TABLE_ITEMS WHERE $BUS_STOP_DESC LIKE ?"
        val db = this.readableDatabase
        val results = mutableListOf<BusStopJSON>()
        val cursor = db.rawQuery(query, arrayOf("%$stopName%"))

        if (cursor.moveToFirst()) {
            do {
                results.add(getBusStopJsonObject(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return results
    }

    /**
     * Return list of bus stop objects based on service number and company
     * If no elements found will return empty array
     * @param svcNo Service number to search for
     * @param company Company name to search for
     * @return List of BusStopJSON objects representing bus stops that serve the given service number and company
     */
    fun getBusStopsBySvcNo(svcNo: String, company: String): List<BusStopJSON> {
        val query = "SELECT * FROM $TABLE_ITEMS WHERE $BUS_STOP_SERVICES LIKE ?"
        val db = this.readableDatabase
        val results = mutableListOf<BusStopJSON>()
        val cursor = db.rawQuery(query, arrayOf("%$svcNo:$company%"))

        if (cursor.moveToFirst()) {
            do {
                results.add(getBusStopJsonObject(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return results
    }

    /**
     * Return a bus stop object based on latitude and longitude
     * If no bus stop found at the given location, throws NoSuchElementException
     * @param lat Latitude of the bus stop location
     * @param lng Longitude of the bus stop location
     * @return BusStopJSON object representing the bus stop at the given location
     * @throws NoSuchElementException if no bus stop is found at the given location
     */
    fun getBusStopByLocation(lat: Double, lng: Double): BusStopJSON {
        val query =
            "SELECT * FROM $TABLE_ITEMS WHERE $BUS_STOP_LATITUDE LIKE ? AND $BUS_STOP_LONGITUDE LIKE ?"
        val db = this.readableDatabase
        val longitude = lng.toString().substring(0, lng.toString().length - 2)
        val latitude = lat.toString().substring(0, lat.toString().length - 2)
        val cursor = db.rawQuery(query, arrayOf("$latitude%", "$longitude%"))

        if (cursor.moveToFirst()) {
            val busStop = getBusStopJsonObject(cursor)
            cursor.close()
            db.close()
            return busStop
        }
        cursor.close()
        db.close()
        throw NoSuchElementException("No bus stop found at location: ($latitude, $longitude)")
    }

    /**
     * Return a list of bus stops based on a search query
     * The query can match bus stop code, road name, or description
     * If no bus stops match the query, an empty list is returned
     * @param query The search query to match against bus stop code, road name, or description
     * @return List of BusStopJSON objects representing bus stops that match the query
     */
    fun getBusStopsByQuery(query: String): List<BusStopJSON> {
        val queryString = "SELECT * FROM $TABLE_ITEMS WHERE $BUS_STOP_CODE LIKE ? COLLATE NOCASE " +
                "OR $BUS_STOP_ROAD LIKE ? COLLATE NOCASE OR $BUS_STOP_DESC LIKE ? COLLATE NOCASE;"
        d(TAG, "QUERY-STRING: $queryString")
        val db = this.readableDatabase
        val result = mutableListOf<BusStopJSON>()
        val cursor = db.rawQuery(queryString, arrayOf("%$query%", "%$query%", "%$query%"))

        if (cursor.moveToFirst()) {
            do {
                result.add(getBusStopJsonObject(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return result
    }

    val size: Int
        get() {
            val db = this.readableDatabase
            val query = "SELECT COUNT(*) FROM $TABLE_ITEMS"
            val cursor = db.rawQuery(query, null)
            var size = 0
            if (cursor.moveToFirst()) {
                size = cursor.getInt(0)
            }
            cursor.close()
            db.close()
            return size
        }
}