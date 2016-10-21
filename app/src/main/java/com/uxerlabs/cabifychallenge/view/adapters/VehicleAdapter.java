package com.uxerlabs.cabifychallenge.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.uxerlabs.cabifychallenge.R;
import com.uxerlabs.cabifychallenge.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 * @author Francisco Cuenca on 21/10/16.
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>{

    private List<Vehicle> vehicles;
    private Context mContext;
    private int positionSelected = 0;

    private final PublishSubject<Vehicle> onClickSubject = PublishSubject.create();

    public VehicleAdapter(Context context, List<Vehicle> vehicles) {
        this.vehicles = vehicles;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return vehicles == null ? 0 : vehicles.size();
    }

    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VehicleViewHolder(parent.getContext());
    }

    @Override
    public void onBindViewHolder(final VehicleViewHolder holder, final int position) {
        final Vehicle vehicle = vehicles.get(position);
        holder.vehicleName.setText(vehicle.getName());
        holder.vehiclePrice.setText(vehicle.getPrice());
        Glide.with(mContext)
                .load(vehicle.getIconURL())
                .into(holder.vehicleIcon);
        if (position == positionSelected)
            holder.background.setBackgroundColor(Color.WHITE);
        else
            holder.background.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubject.onNext(vehicle); // Notifies to subscriber that a cell has benn clicked
                notifyItemChanged(positionSelected); // Deselect the previous cell
                positionSelected = position; // Save the position selected to change background
                notifyItemChanged(positionSelected); // Select the new cell
            }
        });
    }

    /*
    Replace adapter with a new list of vehicles
     */
    public void updateVehicles(List<Vehicle> vehiclesList){
        positionSelected = 0;
        if (this.vehicles == null)
            vehicles = new ArrayList<>();
        else
            vehicles.clear();
        vehicles.addAll(vehiclesList);
        notifyDataSetChanged();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.background_relative_layout) RelativeLayout background;
        @BindView(R.id.vehicle_icon) ImageView vehicleIcon;
        @BindView(R.id.vehicle_name) TextView vehicleName;
        @BindView(R.id.vehicle_price) TextView vehiclePrice;

        public VehicleViewHolder(Context context) {
            this(LayoutInflater.from(context).inflate(R.layout.vehicle_item, null));
        }

        private VehicleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public Observable<Vehicle> getPositionClicks(){
        return onClickSubject.asObservable();
    }
}