package com.tranhuy105.musicserviceapi.repository.api;


import com.tranhuy105.musicserviceapi.model.Advertisement;
import java.util.List;
import java.util.Optional;

public interface AdvertisementRepository {
    void save(Advertisement advertisement);
    Optional<Advertisement> findById(Long id);
    Optional<Advertisement> findRandomAdByRegion(String regionCode);
    List<Advertisement> findAll();
    void update(Advertisement advertisement);
    void deleteById(Long id);
}

