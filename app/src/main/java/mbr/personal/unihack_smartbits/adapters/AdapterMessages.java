package mbr.personal.unihack_smartbits.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mbr.personal.unihack_smartbits.R;
import mbr.personal.unihack_smartbits.types.MessageItem;

public class AdapterMessages extends RecyclerView.Adapter<AdapterMessages.ViewHolder> {

    private static final int ME_MESSAGE_TYPE = 0;
    private static final int OTHER_MESSAGE_TYPE = 1;

    private List<MessageItem> data;

    public AdapterMessages(List<MessageItem> data) {
        this.data = data;
    }

    public void add(MessageItem msg) {
        this.data.add(msg);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = null;

        if (viewType == OTHER_MESSAGE_TYPE) {
            v = inflater.inflate(R.layout.message_other, parent, false);
        } else if (viewType == ME_MESSAGE_TYPE) {
            v = inflater.inflate(R.layout.message_me, parent, false);
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.timestamp.setText(data.get(position).getTimestamp());
        holder.message.setText(data.get(position).getMessage());
        if (data.get(position).isOther()) {
            holder.image.setImageResource(R.drawable.cg_logo);
        } else {
            holder.image.setImageResource(R.drawable.leaderboard_king);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).isOther()) {
            return OTHER_MESSAGE_TYPE;
        } else if (!data.get(position).isOther()) {
            return ME_MESSAGE_TYPE;
        }

        return 0;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView message, timestamp;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.message = (TextView) itemView.findViewById(R.id.txt_message);
            this.timestamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            this.image = (ImageView) itemView.findViewById(R.id.img_message);
        }
    }
}
