package com.hackathon.securestarter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private Long totalVehicles;
    private Long activeFleet;        // vehicles ON_TRIP
    private Long inShopVehicles;     // vehicles IN_SHOP
    private Long pendingCargo;       // trips in DRAFT
    private Long activeTrips;        // trips DISPATCHED or IN_TRANSIT
    private Long totalDrivers;
    private Long availableDrivers;
    private List<TripResponse> recentTrips;
}
