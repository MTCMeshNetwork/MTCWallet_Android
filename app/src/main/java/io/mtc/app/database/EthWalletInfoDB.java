package io.mtc.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by admin on 2018/1/5.
 */

public class EthWalletInfoDB extends SQLiteOpenHelper {

    private final static String DB_NAME = "eth_wallet_db";
    private final static String ETH_WALLET_TABLE = "eth_wallet";
    private final static String ETH_TOKEN_TABLE = "eth_token";
    private final static int VERSION = 1;

    private final static String SQL_DDL_ETH_WALLET_TABLE_V1 = "CREATE TABLE " + ETH_WALLET_TABLE + " (\n" +
            "    id            INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    address       TEXT    UNIQUE\n" +
            "                          NOT NULL,\n" +
            "    name          TEXT,\n" +
            "    create_time   INTEGER,\n" +
            "    type          INTEGER,\n" +
            "    password_hint TEXT,\n" +
            "    note          TEXT\n" +
            ");";

    private final static String SQL_DDL_ETH_TOKEN_TABLE_V1 = "CREATE TABLE " + ETH_TOKEN_TABLE + " (\n" +
            "    id             INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    wallet_address TEXT,\n" +
            "    contract_address          TEXT,\n" +
            "    name           TEXT,\n" +
            "    create_time    INTEGER,\n" +
            "    note           TEXT\n" +
            ");\n";

    public EthWalletInfoDB(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_DDL_ETH_WALLET_TABLE_V1);
        db.execSQL(SQL_DDL_ETH_TOKEN_TABLE_V1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addWallet(EthWalletInfo walletInfo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + ETH_WALLET_TABLE + " (address,name,create_time,type,password_hint,note) VALUES (?,?,?,?,?,?)",
                    new Object[] { walletInfo.address,
                            walletInfo.name,
                            walletInfo.create_time,
                            walletInfo.type,
                            walletInfo.password_hint,
                            walletInfo.note });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateWalletName(String walletAddress, String walletName) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("UPDATE " + ETH_WALLET_TABLE + " SET name=? WHERE address=?", new String[] { walletName, walletAddress });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeWallet(String walletAddress) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + ETH_WALLET_TABLE + " WHERE address=?", new String[] { walletAddress });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public EthWalletInfo getWalletInfo(String walletAddress) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ETH_WALLET_TABLE + " WHERE address=?", new String[]{walletAddress});
            if (cursor.moveToNext()) {
                return getWalletInfo(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public List<EthWalletInfo> getWalletList() {
        SQLiteDatabase db = getReadableDatabase();
        List<EthWalletInfo> resultList = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ETH_WALLET_TABLE, new String[]{});
            while (cursor.moveToNext()) {
                EthWalletInfo ethWalletInfo = getWalletInfo(cursor);
                if (ethWalletInfo == null)
                    break;
                resultList.add(ethWalletInfo);
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

    private EthWalletInfo getWalletInfo(Cursor cursor) {
        EthWalletInfo ethWalletInfo = new EthWalletInfo();
        ethWalletInfo.id = cursor.getInt(cursor.getColumnIndex("id"));
        ethWalletInfo.address = cursor.getString(cursor.getColumnIndex("address"));
        ethWalletInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        ethWalletInfo.type = cursor.getInt(cursor.getColumnIndex("type"));
        ethWalletInfo.create_time = cursor.getLong(cursor.getColumnIndex("create_time"));
        ethWalletInfo.password_hint = cursor.getString(cursor.getColumnIndex("password_hint"));
        ethWalletInfo.note = cursor.getString(cursor.getColumnIndex("note"));
        return ethWalletInfo;
    }

    private EthTokenInfo getTokenInfo(Cursor cursor) {
        EthTokenInfo ethTokenInfo = new EthTokenInfo();
        ethTokenInfo.id = cursor.getInt(cursor.getColumnIndex("id"));
        ethTokenInfo.wallet_address = cursor.getString(cursor.getColumnIndex("wallet_address"));
        ethTokenInfo.contract_address = cursor.getString(cursor.getColumnIndex("contract_address"));
        ethTokenInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        ethTokenInfo.create_time = cursor.getLong(cursor.getColumnIndex("create_time"));
        ethTokenInfo.note = cursor.getString(cursor.getColumnIndex("note"));
        return ethTokenInfo;
    }

    public boolean addToken(EthTokenInfo tokenInfo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + ETH_TOKEN_TABLE + " (wallet_address,contract_address,name,create_time,note) VALUES (?,?,?,?,?)",
                    new Object[] { tokenInfo.wallet_address,
                            tokenInfo.contract_address,
                            tokenInfo.name,
                            tokenInfo.create_time,
                            tokenInfo.note });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeToken(String token) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + ETH_TOKEN_TABLE + " WHERE contract_address=?", new String[] { token });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<EthTokenInfo> getTokenList(String walletAddress) {
        SQLiteDatabase db = getReadableDatabase();
        List<EthTokenInfo> resultList = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ETH_TOKEN_TABLE + " WHERE wallet_address=?", new String[]{ walletAddress });
            while (cursor.moveToNext()) {
                EthTokenInfo ethTokenInfo = getTokenInfo(cursor);
                if (ethTokenInfo == null)
                    break;
                resultList.add(ethTokenInfo);
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

}
