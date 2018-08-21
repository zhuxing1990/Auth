package com.vunke.auth.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.vunke.auth.util.LogUtil;

/**
 * Created by zhuxi on 2017/10/20.
 */
public class AuthProvider extends ContentProvider {

    private final static String AUTHORITH = "com.vunke.auth.authentication";
    private final static String PATH = "/auth";
    private final static String PATHS = "/auth/#";
    private final static String TABLE_NAME = "auth";
    private final static UriMatcher mUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    private static final int CODE_DIR = 1;
    private static final int CODE_ITEM = 2;
    static {
        mUriMatcher.addURI(AUTHORITH, PATH, CODE_DIR);
        mUriMatcher.addURI(AUTHORITH, PATHS, CODE_ITEM);
    }
    private AuthHelper dbHelper;
    private SQLiteDatabase db;
    private static final String USER_ID = "user_id";
    private static final String AUTH_CODE = "auth_code";
    private static final String CREATE_TIME = "create_time";
    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_INFO = "error_info";
    @Override
    public boolean onCreate() {
        dbHelper = new AuthHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        db = dbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case 1:
                String sql;
                if (selectionArgs != null && selectionArgs.length != 0) {
                    sql = "select * from " + TABLE_NAME + " where " + USER_ID
                            + "='" + selectionArgs[0] + "'";
                } else {
                    sql = "select * from " + TABLE_NAME;
                }
                sql = sql + " order by "+CREATE_TIME+" desc";
                LogUtil.i("tv_launcher", "sql:"+sql);
                cursor = db.rawQuery(sql, null);
			/*
			 * cursor = db.query(TABLE_NAME, null, sql, null, null, null,
			 * "create_time des ");
			 */
                break;
            default:

                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case CODE_DIR:
                return "vnd.android.cursor.dir/auth";
            case CODE_ITEM:
                return "vnd.android.cursor.item/auth";

            default:
                throw new IllegalArgumentException("异常参数");
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select "+USER_ID+" from " + TABLE_NAME
                + " where "+USER_ID+" = '" + values.get(USER_ID) + "'", null);
        // db.query(true, TABLE_NAME, new String[] {
        // "body", "user_id", "create_time" }, null, null, "user_id", null,
        // null, null, null);
        if (cursor.moveToNext()) {// 有下一个 ，更新
            String userID = values.get(USER_ID).toString();
            db.update(TABLE_NAME, values, USER_ID+"=?", new String[] { userID });
        } else {// 否则 插入数据
            switch (mUriMatcher.match(uri)) {
                case 1:
                    db.insert(TABLE_NAME, null, values);
                    break;
                case 2:
                default:
                    break;
            }
        }
        // db.execSQL("delete from groupinfo where rowid not in(select max(rowid) from groupinfo group by user_id)");
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int number = 0;
        db = dbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case 1:
                number = db.delete(TABLE_NAME, selection, selectionArgs);
            case 2:
                long id = ContentUris.parseId(uri);
                selection = (selection != null || "".equals(selection.trim()) ? USER_ID
                        + "=" + id
                        : selection + "and" + USER_ID + "=" + id);
                number = db.delete(TABLE_NAME, selection, selectionArgs);
        }
        return number;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int number = 0;
        db = dbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case 1:
                number = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case 2:
                long id = ContentUris.parseId(uri);
                selection = (selection != null || "".equals(selection.trim()) ? USER_ID
                        + "=" + id
                        : selection + "and" + USER_ID + "=" + id);
                number = db.update(TABLE_NAME, values, selection, selectionArgs);
        }
        return number;
    }
}
