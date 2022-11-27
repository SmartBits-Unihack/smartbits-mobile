package mbr.personal.unihack_smartbits.types;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageItem implements IDatabaseTypes {
    private static final String TAG = MessageItem.class.getSimpleName();

    private final String timestamp;
    private final String message;
    private final boolean other;

    public MessageItem(String timestamp, String message, boolean other) {
        this.timestamp = timestamp;
        this.message = message;
        this.other = other;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOther() {
        return other;
    }

    @Override
    public JSONObject prepare(Object... extras) {
        JSONObject ret = new JSONObject();

        try {
            ret.put("message", this.message);
            ret.put("is_phone", !this.other);
            ret.put("glove_address", extras[0]);
        } catch (JSONException err) {
            Log.e(TAG, "JSON creation failed with error: " + err);
        }

        return ret;
    }
}
