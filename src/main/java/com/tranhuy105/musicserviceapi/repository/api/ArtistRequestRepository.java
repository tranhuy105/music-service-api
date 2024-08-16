package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.ArtistRequest;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;

import java.util.List;
import java.util.Optional;

public interface ArtistRequestRepository {
    void createArtistRequest(Long userId,String artistName, String genre, String portfolioUrl, String bio, String socialMediaLinks);
    Page<ArtistRequest> getPendingRequests(QueryOptions queryOptions);
    void reviewRequest(Long requestId, String status, Long reviewedBy, String reason);
    Optional<ArtistRequest> findArtistRequestById(Long id);

    List<ArtistRequest> getRequestsByUserId(Long userId);
}
