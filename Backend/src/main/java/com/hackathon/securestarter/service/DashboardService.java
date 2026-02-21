package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.response.DashboardResponse;
import com.hackathon.securestarter.dto.response.TripResponse;
import com.hackathon.securestarter.enums.TripStatus;
import com.hackathon.securestarter.repository.DriverRepository;
import com.hackathon.securestarter.repository.TripRepository;
import com.hackathon.securestarter.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final TripService tripService;

    /**
     * Get dashboard KPIs and recent trip data for all roles.
     */
    public DashboardResponse getDashboard() {
        Long totalVehicles = vehicleRepository.countNonRetired();
        Long activeFleet = vehicleRepository.countActiveFleet();
        Long inShopVehicles = vehicleRepository.countInShop();
        Long pendingCargo = tripRepository.countPendingCargo();
        Long activeTrips = tripRepository.countActiveTrips();
        Long totalDrivers = driverRepository.count();
        Long availableDrivers = (long) driverRepository.findAvailableDrivers(LocalDate.now()).size();

        // Get recent trips (last 10 dispatched, in-transit, or completed)
        List<TripResponse> recentTrips = Stream.of(
                        tripRepository.findByStatus(TripStatus.IN_TRANSIT),
                        tripRepository.findByStatus(TripStatus.DISPATCHED),
                        tripRepository.findByStatus(TripStatus.COMPLETED)
                )
                .flatMap(List::stream)
                .limit(10)
                .map(trip -> tripService.getTripById(trip.getId()))
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalVehicles(totalVehicles)
                .activeFleet(activeFleet)
                .inShopVehicles(inShopVehicles)
                .pendingCargo(pendingCargo)
                .activeTrips(activeTrips)
                .totalDrivers(totalDrivers)
                .availableDrivers(availableDrivers)
                .recentTrips(recentTrips)
                .build();
    }
}
