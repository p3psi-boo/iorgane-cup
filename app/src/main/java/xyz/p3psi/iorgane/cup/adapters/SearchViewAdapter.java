package xyz.p3psi.iorgane.cup.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.RxBleDevice;

import java.util.List;

import xyz.p3psi.iorgane.cup.R;

import static android.content.ContentValues.TAG;
import static xyz.p3psi.iorgane.cup.Utils.parseBLERecord;

public class SearchViewAdapter extends RecyclerView.Adapter<SearchViewAdapter.SearchViewHolder> {

    private List<RxBleDevice> mDeviceData;
    private List<byte[]> mRecordData;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listenser) {
        this.listener = listenser;
    }

    public SearchViewAdapter(List<RxBleDevice> devicedata, List<byte[]> recordData) {
        this.mDeviceData = devicedata;
        this.mRecordData = recordData;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_search_card, parent, false);
        SearchViewHolder viewHolder = new SearchViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, final int position) {
        holder.macAddr.setText("MAC: " + mDeviceData.get(position).getMacAddress());
        holder.serial.setText("Serial: " +  parseBLERecord(mRecordData.get(position)).specificData);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    Log.d(TAG, "onClick: click " + position);
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + mDeviceData.size());
        return mDeviceData.size();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        public TextView serial; // serial number
        public TextView macAddr;
        public LinearLayout itemLayout;

        public SearchViewHolder(View itemView) {
            super(itemView);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.search_card);
            macAddr = (TextView) itemView.findViewById(R.id.cupMacAddr);
            serial = (TextView) itemView.findViewById(R.id.cupSerial);

        }
    }


}
