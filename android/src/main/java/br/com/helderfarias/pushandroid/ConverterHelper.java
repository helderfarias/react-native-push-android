package br.com.helderfarias.pushandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public final class ConverterHelper {

    private static final String TAG = "ConverterHelper";

    private static final Map<Class<?>, Setter> SETTERS = new HashMap<Class<?>, Setter>();

    static {
        SETTERS.put(Boolean.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putBoolean(key, (Boolean) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value)  throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(Integer.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putInt(key, (Integer) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value)  throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(Long.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putLong(key, (Long) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value)  throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(Double.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putDouble(key, (Double) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value)  throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(String.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putString(key, (String) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value)  throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(String[].class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                throw new IllegalArgumentException("Unexpected type from JSON");
            }

            public void setOnJSON(JSONObject json, String key, Object value)  throws JSONException {
                JSONArray jsonArray = new JSONArray();
                for (String stringValue : (String[])value) {
                    jsonArray.put(stringValue);
                }
                json.put(key, jsonArray);
            }
        });

        SETTERS.put(JSONArray.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                JSONArray jsonArray = (JSONArray)value;
                ArrayList<String> stringArrayList = new ArrayList<String>();

                if (jsonArray.length() == 0) {
                    bundle.putStringArrayList(key, stringArrayList);
                    return;
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    Object current = jsonArray.get(i);
                    if (current instanceof String) {
                        stringArrayList.add((String)current);
                    } else {
                        throw new IllegalArgumentException("Unexpected type in an array: " + current.getClass());
                    }
                }

                bundle.putStringArrayList(key, stringArrayList);
            }

            @Override
            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                throw new IllegalArgumentException("JSONArray's are not supported in bundles.");
            }
        });
    }

    public static JSONObject fromBundleToJson(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();

        for(String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value == null) {
                continue;
            }

            if (value instanceof List<?>) {
                JSONArray jsonArray = new JSONArray();
                List<String> listValue = (List<String>)value;
                for (String stringValue : listValue) {
                    jsonArray.put(stringValue);
                }
                json.put(key, jsonArray);
                continue;
            }

            if (value instanceof Bundle) {
                json.put(key, fromBundleToJson((Bundle)value));
                continue;
            }

            Setter setter = SETTERS.get(value.getClass());
            if (setter == null) {
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
            }

            setter.setOnJSON(json, key, value);
        }

        return json;
    }

    public static Bundle fromJsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator<String> jsonIterator = jsonObject.keys();

        while (jsonIterator.hasNext()) {
            String key = jsonIterator.next();
            Object value = jsonObject.get(key);
            if (value == null || value == JSONObject.NULL) {
                continue;
            }

            if (value instanceof JSONObject) {
                bundle.putBundle(key, fromJsonToBundle((JSONObject)value));
                continue;
            }

            Setter setter = SETTERS.get(value.getClass());
            if (setter == null) {
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
            }

            setter.setOnBundle(bundle, key, value);
        }

        return bundle;
    }

    public static WritableMap fromIntentToWritableMap(Intent intent) {
        WritableMap params;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            try {
                params = Arguments.fromBundle(extras);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                params = Arguments.createMap();
            }
        } else {
            params = Arguments.createMap();
        }
        return params;
    }

    static interface Setter {
        void setOnBundle(Bundle bundle, String key, Object value) throws JSONException;

        void setOnJSON(JSONObject json, String key, Object value) throws JSONException;
    }

}