package mbr.personal.unihack_smartbits.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mbr.personal.unihack_smartbits.R;
import mbr.personal.unihack_smartbits.types.BluetoothItem;

public class AdapterBluetooth extends RecyclerView.Adapter<AdapterBluetooth.ViewHolder> {

    private List<BluetoothItem> devices;


    public AdapterBluetooth(List<BluetoothItem> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.bluetooth_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(devices.get(position).getName());
        holder.address.setText(devices.get(position).getAddress());

        holder.background.setOnClickListener(v -> {
            try {
//                Intent chatIntent = new Intent(v.getContext(), ChatActivity.class);
//
//                chatIntent.putExtra("address", holder.address.getText().toString());
//
//                v.getContext().startActivity(chatIntent);
            } catch (Exception err) {
                Toast.makeText(v.getContext(), err.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, address;
        ConstraintLayout background;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.txt_bt_name);
            this.address = itemView.findViewById(R.id.txt_bt_address);
            this.background = itemView.findViewById(R.id.bkg_bluetooth);
        }


    }

}
