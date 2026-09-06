package com.alok.home.service;


import com.alok.home.commons.dto.api.response.EventTimelineResponseDTO;
import com.alok.home.commons.dto.api.response.LifeEventResponseDTO;
import com.alok.home.commons.entity.FamilyProfile;
import com.alok.home.commons.entity.LifeEvent;
import com.alok.home.commons.repository.FamilyProfileRepository;
import com.alok.home.commons.repository.LifeEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineService {

    private final FamilyProfileRepository familyProfileRepository;
    private final LifeEventRepository lifeEventRepository;

    public TimelineService(FamilyProfileRepository familyProfileRepository, LifeEventRepository lifeEventRepository) {
        this.familyProfileRepository = familyProfileRepository;
        this.lifeEventRepository = lifeEventRepository;
    }

    @Transactional(readOnly = true)
    public List<LifeEventResponseDTO> getGlobalProfilesTimeline() {
        // Fetch graph records securely down a singular execution channel
        List<FamilyProfile> profiles = familyProfileRepository.findAllWithEventsEagerly();

        return profiles.stream()
                .map(LifeEventResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventTimelineResponseDTO> getGlobalEventsTimeline() {
        List<LifeEvent> events = lifeEventRepository.findAllEventsWithParticipantsEagerly();

        return events.stream()
                .map(EventTimelineResponseDTO::new)
                .collect(Collectors.toList());
    }

    public LifeEventResponseDTO.EventDTO getLifeEventDetails(Long id) {
        return new LifeEventResponseDTO.EventDTO(lifeEventRepository.findEventDetailsById(id));
    }
}
