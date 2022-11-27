package mbr.personal.unihack_smartbits.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mbr.personal.unihack_smartbits.R;
import mbr.personal.unihack_smartbits.adapters.AdapterMessages;
import mbr.personal.unihack_smartbits.types.MessageItem;

public class ChatActivity extends AppCompatActivity {

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


            btnSend = findViewById(R.id.btn_send);
            txtMessage = findViewById(R.id.etx_message);

        } catch (Exception err) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
        }
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
                MessageItem msgItem = new MessageItem(java.time.LocalTime.now().toString(), tempMsg, true);
                if (tempMsg.equals("\r")) break;
                adapter.add(msgItem);
                adapter.notifyDataSetChanged();
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
                        MessageItem item = new MessageItem(LocalTime.now().toString(), txtMessage.getText().toString(), false);
                        adapter.add(item);
                        adapter.notifyDataSetChanged();
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
                byte[] data = new byte[bytes.length + 1];
                int i = 0;
                for (byte b : bytes) {
                    data[i++] = b;
                }
                data[i] = '\n';
                mOutputStream.write(data);
                mOutputStream.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
