package mbr.personal.unihack_smartbits.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import mbr.personal.unihack_smartbits.R;
import mbr.personal.unihack_smartbits.types.MessageItem;

public class AdapterMessages extends RecyclerView.Adapter<AdapterMessages.ViewHolder> {
    private static final String TAG = AdapterMessages.class.getSimpleName();

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
            v = inflater.inflate(R.layout.glove_message_item, parent, false);
        } else if (viewType == ME_MESSAGE_TYPE) {
            v = inflater.inflate(R.layout.message_item, parent, false);
        }

        return new ViewHolder(v);
    }

    private void playMessage(Context ctx, String message) {
        String audioUrl = "http://85.120.206.70:8080/api/v1/speech?message=" + message;

        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(ctx, Uri.parse(audioUrl));

            mp.prepare();
            mp.start();

        } catch (IOException exc) {
            Log.e(TAG, "Failed to GET speech");
            exc.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.message.setText(data.get(position).getMessage());

        holder.message.setOnClickListener(view -> {
            playMessage(
                    view.getContext(),
                    data.get(position).getMessage()
            );
        });
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

        TextView message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.message = (TextView) itemView.findViewById(R.id.txt_message);
        }
    }
}
