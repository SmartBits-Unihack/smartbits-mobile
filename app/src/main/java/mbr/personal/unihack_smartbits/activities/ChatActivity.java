package mbr.personal.unihack_smartbits.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import mbr.personal.unihack_smartbits.R;
import mbr.personal.unihack_smartbits.adapters.AdapterMessages;
import mbr.personal.unihack_smartbits.exceptions.AuthenticationException;
import mbr.personal.unihack_smartbits.types.MessageItem;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();

    private String connectedDevice;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    private SendReceive mSendReceive;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button btnSend;
    EditText txtMessage;
    RecyclerView vMessages;
    List<MessageItem> messages;
    AdapterMessages adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        try {
            vMessages = findViewById(R.id.rcv_messages);
            messages = new ArrayList<>();
            adapter = new AdapterMessages(messages);
            vMessages.setAdapter(adapter);
            vMessages.setLayoutManager(new LinearLayoutManager(this));

            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice device = btAdapter.getRemoteDevice(getIntent()
                    .getStringExtra("address"));

            ClientClass client = new ClientClass(device);
            client.start();

            connectedDevice = device.getAddress();
            btnSend = findViewById(R.id.btn_send);
            txtMessage = findViewById(R.id.etx_message);

        } catch (Exception err) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveMessageToDatabase(String message, boolean phone)
            throws InterruptedException, JSONException {
        String url = "http://85.120.206.70:8080/api/v1/messages";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject payload = new JSONObject();
        payload.put("glove_address", connectedDevice);
        payload.put("message", message);
        payload.put("is_phone", phone);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(payload.toString(), JSON);
        Thread saveThread = new Thread(() -> {
            Request request;
            try {
                request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "OkHttp Headers.java")
                        .addHeader("Authorization", LoginActivity.accessToken)
                        .post(body)
                        .build();
            } catch (Exception e ) {
                return;
            }
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new AuthenticationException(response.body().string());
            } catch (AuthenticationException exc) {
                Log.e(TAG, "Authentication failed! With message below: ");
                Log.e(TAG, exc.getMessage());
            } catch (IOException exc) {
                Log.e(TAG, "Failed to make the request to the server.");
                Log.e(TAG, exc.getMessage());
            }
        });

        saveThread.start();

        saveThread.join();
    }

    Handler mHandler = new Handler(msg -> {

        switch (msg.what){
            case STATE_LISTENING:
                Toast.makeText(this, "Listening...", Toast.LENGTH_LONG).show();
                break;

            case STATE_CONNECTING:
                Toast.makeText(this, "Connectiong...", Toast.LENGTH_LONG).show();
                break;

            case STATE_CONNECTED:
                Toast.makeText(this, "Connected!", Toast.LENGTH_LONG).show();
                break;

            case STATE_CONNECTION_FAILED:
                Toast.makeText(this, "Connection failed!", Toast.LENGTH_LONG).show();
                break;

            case STATE_MESSAGE_RECEIVED:

                byte[] readBuffer = (byte[]) msg.obj;
                String tempMsg = new String(readBuffer,0, msg.arg1);
                if (tempMsg.length() == 0)  break;
                MessageItem msgItem = new MessageItem(tempMsg, true);
                if (tempMsg.equals("\r\n")) break;
                adapter.add(msgItem);
                adapter.notifyDataSetChanged();
                vMessages.smoothScrollToPosition(adapter.getItemCount());
                try {
                    saveMessageToDatabase(tempMsg, false);
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    });

    public class ClientClass extends Thread {

        private BluetoothDevice mBluetoothDevice;
        private BluetoothSocket mBluetoothSocket;

        public ClientClass(BluetoothDevice bluetoothDevice){
            mBluetoothDevice = bluetoothDevice;
            try {
                mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                mBluetoothSocket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                mHandler.sendMessage(message);

                mSendReceive = new SendReceive(mBluetoothSocket);
                mSendReceive.start();
            } catch (IOException e){
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                mHandler.sendMessage(message);
            }
        }
    }

    public class SendReceive extends Thread {
        private final BluetoothSocket mBluetoothSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public SendReceive(BluetoothSocket bluetoothSocket){

            mBluetoothSocket = bluetoothSocket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            btnSend.setOnClickListener(v -> {
                        this.write(txtMessage.getText().toString().getBytes());
                        MessageItem item =
                                new MessageItem(txtMessage.getText().toString(), false);
                        adapter.add(item);
                        adapter.notifyDataSetChanged();
                        vMessages.smoothScrollToPosition(adapter.getItemCount());
                        txtMessage.setText("");
                    }
            );

            try {
                tempIn = mBluetoothSocket.getInputStream();
                tempOut = mBluetoothSocket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }

            mInputStream = tempIn;
            mOutputStream = tempOut;

        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes = mInputStream.read(buffer);
                    mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                byte[] data = new byte[27];
                int i = 0;
                try {
                    for (byte b : bytes) {
                        data[i++] = b;
                    }
                } catch (IndexOutOfBoundsException exc) {
                    Toast.makeText(ChatActivity.this,
                            "The max length of a message is 27 characters!",
                            Toast.LENGTH_SHORT).show();
                }
                while (i < 27) {
                    data[i++] = 0x20;
                }

                mOutputStream.write(data);
                mOutputStream.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
