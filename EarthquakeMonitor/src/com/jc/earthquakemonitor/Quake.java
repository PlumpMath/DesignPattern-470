package com.jc.earthquakemonitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;

public class Quake {
    private Date date;
    private String details;
    private Location location;
    private double magnitude;
    private String link;
    public Date getDate() {
		return date;
	}
	public String getDetails() {
		return details;
	}
	public Location getLocation() {
		return location;
	}
	public double getMagnitude() {
		return magnitude;
	}
	public String getLink() {
		return link;
	}
	
	public Quake(Date _d,String _det,Location _loc,double _mag,String _link){
		this.date = _d;
		this.details = _det;
		this.location = _loc;
		this.magnitude = _mag;
		this.link = _link;
	}
    
	public String toString(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
		String dateString = sdf.format(date);
		return dateString + ": "+magnitude +" "+details;
	}
    
}
