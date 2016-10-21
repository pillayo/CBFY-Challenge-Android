package com.uxerlabs.cabifychallenge.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Vehicle/Service with cost for a journey
 * @author Francisco Cuenca on 18/10/16.
 */

public class Vehicle implements Parcelable{

    //Vehicle/Service identifier
    private String id;

    //Vehicle/Service name
    private String name;

    //Url of Vehicle/Service image
    private String iconURL;

    //@SerializedName("price_formatted")
    //Vehicle/Service price
    private String price;

    public Vehicle(String id, String name, String iconURL, String price) {
        this.id = id;
        this.name = name;
        this.iconURL = iconURL;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return id +" | " + name + " | " + iconURL + " | " + price;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(iconURL);
        dest.writeString(price);
    }

    private void readFromParcel(Parcel in) {
        id= in.readString();
        name = in.readString();
        iconURL = in.readString();
        price = in.readString();
    }

    public static final Parcelable.Creator<Vehicle> CREATOR = new Parcelable.Creator<Vehicle>() {

        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {

            return new Vehicle[size];
        }

    };
    public Vehicle (Parcel in){
        readFromParcel(in);
    }
}
