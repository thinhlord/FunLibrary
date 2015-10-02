package com.teamx.funlibrary;

/**
 * Created by Nguyen Duc Thinh on 11/02/2015.
 * Project type: Android
 */

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

public class FunHttpClient {
    private static final String BASE_URL = "http://128.199.167.255/soa/book/";

    private static final String SUB_ADD = "add";
    private static final String SUB_DEL = "delete";
    private static final String SUB_DEL2 = "delete2";
    private static final String SUB_LIST = "list";
    private static final String SUB_UPDATE = "updateWithPost";
    private static final String SUB_SEARCH = "search";

    private static AsyncHttpClient client = new AsyncHttpClient();

    private static Context context;

    public static void initialize(Context context) {
        FunHttpClient.context = context;
    }

    static RequestHandle get(String subUrl, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.get(getUrl(subUrl), params, responseHandler);
    }

    public static RequestHandle post(String subUrl, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(getUrl(subUrl), params, responseHandler);

    }

    public static RequestHandle post(String subUrl, JSONObject jsonObject, AsyncHttpResponseHandler responseHandler) {
        Iterator<String> keysIt = jsonObject.keys();
        RequestParams requestParams = new RequestParams();
        while (keysIt.hasNext()) {
            String key = keysIt.next();
            Object value = jsonObject.opt(key);
            requestParams.put(key, value);
        }

        return post(subUrl, requestParams, responseHandler);
    }

    public static RequestHandle postJson(String subUrl, String json, AsyncHttpResponseHandler responseHandler) {
        try {
            return post(subUrl, new JSONObject(json), responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static String getUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static void getList(int limit, int offset, final GetListCallback callback) {
        Log.d("DB", limit + " " + offset);
        RequestParams requestParams = new RequestParams();
        requestParams.put("limit", limit);
        requestParams.put("offset", offset);
        client.get(getUrl(SUB_LIST), requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray data = response.optJSONArray("data");
                ArrayList<Book> list = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    list.add(new Book(data.optJSONObject(i)));
                }
                callback.onSuccess(list);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onFailure(statusCode + " " + responseString);
            }
        });
    }

    public static void delete(int id, final CommonCallback callback) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("book_id", id);
        client.post(getUrl(SUB_DEL2), requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onFailure(statusCode + " " + responseString);
            }
        });
    }

    public static void put(Book book, File image, final CommonCallback callback) {
        //book_name, book_image, book_description, book_author, book_publisher, book_year, book_id
        RequestParams requestParams = new RequestParams();
        requestParams.put("book_name", book.name);
        requestParams.put("book_year", book.year);
        requestParams.put("book_description", book.description);
        requestParams.put("book_author", book.author);
        requestParams.put("book_publisher", book.publisher);
        if (image != null) try {
            requestParams.put("book_image", image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } else {
            requestParams.put("book_image", "NULL");
        }
        JsonHttpResponseHandler httpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onFailure(statusCode + " " + responseString);
            }
        };
        if (book.id != -1) {
            requestParams.put("book_id", book.id);
            client.post(getUrl(SUB_UPDATE), requestParams, httpResponseHandler);
        } else {
            client.post(getUrl(SUB_ADD), requestParams, httpResponseHandler);
        }
    }

    public interface GetListCallback {
        void onSuccess(ArrayList<Book> list);
        void onFailure(String error);
    }

    public interface CommonCallback {
        void onSuccess();
        void onFailure(String error);
    }
}