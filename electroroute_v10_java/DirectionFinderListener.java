package com.tfg.evelyn.electroroute_v10;

import java.util.List;

/**
 * Created by Evelyn on 05/06/2016.
 */
public interface DirectionFinderListener {

    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
