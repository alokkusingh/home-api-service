package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.FamilyMemberDTO;
import com.alok.home.commons.entity.FamilyProfile;
import com.alok.home.commons.repository.FamilyProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/family")
@CrossOrigin(origins = "*") // Allows your Web UI frontend port to access data without CORS blocks
public class FamilyController {

    @Autowired
    private final FamilyProfileRepository familyProfileRepository;

    public FamilyController(FamilyProfileRepository familyProfileRepository) {
        this.familyProfileRepository = familyProfileRepository;
    }

    /**
     * Get family members.
     * GET /api/family/members
     */
    @GetMapping("/members")
    public ResponseEntity<List<FamilyMemberDTO>> getMembers() {
        List<FamilyProfile> members = Optional.of(familyProfileRepository.findAll()).orElse(Collections.emptyList());
        return ResponseEntity.ok(members.stream().map(
                member -> FamilyMemberDTO.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .phone(member.getMobile())
                        .build()
        ).toList());
    }

    /**
     * Get get the family member details by name.
     * GET /api/family/members/Alok
     */
    @GetMapping("/members/{name}")
    public ResponseEntity<FamilyMemberDTO> getFamilyMemberDetails(@PathVariable String name) {
        Optional<FamilyProfile> memberOpt = familyProfileRepository.findByName(name);
        if (memberOpt.isPresent()) {
            FamilyProfile member = memberOpt.get();
            FamilyMemberDTO dto = FamilyMemberDTO.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .phone(member.getMobile())
                    .build();
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
