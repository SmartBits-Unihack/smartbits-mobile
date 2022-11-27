package mbr.personal.unihack_smartbits.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import mbr.personal.unihack_smartbits.adapters.AdapterMessages;
import mbr.personal.unihack_smartbits.types.MessageItem;

public class BluetoothDriver {

    private String connectedDevice;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 5;

    private SendReceive mSendReceive;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button btnSend;
    EditText txtMessage;
    RecyclerView vMessages;
    List<MessageItem> messages;
    AdapterMessages adapter;
    Handler mHandler;

    public BluetoothDriver(Handler pHandler) {
        this.mHandler = pHandler;
    }

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
                    mHandler.obtainMessage(STATE_MESSAGE_RECIEVED, bytes, -1, buffer).sendToTarget();
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
