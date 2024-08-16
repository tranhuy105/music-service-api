package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.CreateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.ArtistRequest;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistRequestService {
    private final ArtistRequestRepository artistRequestRepository;
    private final ArtistService artistService;

    public void createArtistRequest(Long userId,
                                    String artistName,
                                    String genre,
                                    String portfolioUrl,
                                    String bio,
                                    String socialMediaLinks
    ) {
        if (artistName == null) {
            throw new IllegalArgumentException("Must at least provide artist stage name");
        }

        if (artistService.findArtistByUserId(userId) != null) {
            throw new IllegalArgumentException("This user has already become an artist");
        }

        artistRequestRepository.createArtistRequest(userId, artistName, genre, portfolioUrl, bio, socialMediaLinks);
    }

    public List<ArtistRequest> findRequestsByUserId(Long userId) {
        return artistRequestRepository.getRequestsByUserId(userId);
    }

    public Page<ArtistRequest> findPendingRequests(Integer page) {
        page = page != null ? page : 1;
        return artistRequestRepository.getPendingRequests(
                QueryOptions
                        .of(page, 20)
                        .sortBy("id")
                        .desc()
                        .build()
        );
    }

    public ArtistRequest findById(@NonNull Long id) {
        return artistRequestRepository.findArtistRequestById(id).orElseThrow(
                () -> new ObjectNotFoundException("artist_request", id.toString())
        );
    }

    @Transactional
    public void reviewRequest(@NonNull Long requestId, Long adminId, String status, String reason) {
        ArtistRequest artistRequest = artistRequestRepository.findArtistRequestById(requestId)
                .orElseThrow(() -> new ObjectNotFoundException("artist_request", requestId.toString()));
        if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("DENIED")) {
            throw new IllegalArgumentException("Status must be either 'APPROVED' or 'DENIED'");
        }
        if (status.equalsIgnoreCase("DENIED") && (reason == null || reason.trim().isEmpty())) {
            throw new IllegalArgumentException("Reason is required when denying a request.");
        }

        artistRequestRepository.reviewRequest(requestId, status, adminId, reason);
        if (status.equalsIgnoreCase("APPROVED")) {
            artistService.createArtistProfile(new CreateArtistProfileRequestDto(
                    artistRequest.getUserId(),
                    artistRequest.getArtistName(),
                    artistRequest.getBio(),
                    null
            ));
        }
    }
}
