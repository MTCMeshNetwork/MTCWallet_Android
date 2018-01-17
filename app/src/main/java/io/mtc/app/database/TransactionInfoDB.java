package io.mtc.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by admin on 2018/1/15.
 */

public class TransactionInfoDB extends SQLiteOpenHelper {

    public final static int VERSION = 1;
    public final static String DB_NAME = "eth_transaction.db";
    public final static String TABLE_NAME = "eth_transaction";

    public final static String SQL_DDL_TRANSACTION_TABLE = "CREATE TABLE " + TABLE_NAME + " (\n" +
            "    id               INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    address_from     TEXT,\n" +
            "    address_to       TEXT,\n" +
            "    contract_address TEXT,\n" +
            "    txhash           TEXT,\n" +
            "    balance          TEXT,\n" +
            "    data             TEXT,\n" +
            "    oper_type        INTEGER,\n" +
            "    net_type         INTEGER,\n" +
            "    state            INTEGER,\n" +
            "    create_time      INTEGER,\n" +
            "    confirm_time     INTEGER\n" +
            ");";

    public TransactionInfoDB(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_DDL_TRANSACTION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addTransactionInfo(TransactionInfo transactionInfo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + TABLE_NAME + " (address_from,address_to,contract_address,txhash,balance,data,oper_type,net_type,state,create_time) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?)",
                    new Object[] { transactionInfo.address_from,
                            transactionInfo.address_to,
                            transactionInfo.contract_address,
                            transactionInfo.txhash,
                            transactionInfo.balance,
                            transactionInfo.data,
                            transactionInfo.oper_type,
                            transactionInfo.net_type,
                            transactionInfo.state,
                            transactionInfo.create_time});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<TransactionInfo> getTransactionList(String address, String contract_address) {
        List<TransactionInfo> resultList = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE (address_from = ? OR address_to = ?) AND contract_address = ?", new String[]{ address, address, contract_address });
            while (cursor.moveToNext()) {
                TransactionInfo transactionInfo = getTransactionInfo(cursor);
                if (transactionInfo == null)
                    break;
                resultList.add(transactionInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return resultList;
    }

    private TransactionInfo getTransactionInfo(Cursor cursor) {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.id = cursor.getInt(cursor.getColumnIndex("id"));
        transactionInfo.address_from = cursor.getString(cursor.getColumnIndex("address_from"));
        transactionInfo.address_to = cursor.getString(cursor.getColumnIndex("address_to"));
        transactionInfo.contract_address = cursor.getString(cursor.getColumnIndex("contract_address"));
        transactionInfo.txhash = cursor.getString(cursor.getColumnIndex("txhash"));
        transactionInfo.balance = cursor.getString(cursor.getColumnIndex("balance"));
        transactionInfo.data = cursor.getString(cursor.getColumnIndex("data"));
        transactionInfo.oper_type = cursor.getInt(cursor.getColumnIndex("oper_type"));
        transactionInfo.net_type = cursor.getInt(cursor.getColumnIndex("net_type"));
        transactionInfo.state = cursor.getInt(cursor.getColumnIndex("state"));
        transactionInfo.create_time = cursor.getLong(cursor.getColumnIndex("create_time"));
        transactionInfo.confirm_time = cursor.getLong(cursor.getColumnIndex("confirm_time"));
        return transactionInfo;
    }

}
