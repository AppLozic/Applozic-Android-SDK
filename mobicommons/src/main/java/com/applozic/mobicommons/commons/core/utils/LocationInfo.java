package com.applozic.mobicommons.commons.core.utils;

import com.applozic.mobicommons.json.JsonMarker;

public class LocationInfo extends JsonMarker {

    public double lat = 0.0;
    public double lon = 0.0;

    public LocationInfo(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

}
