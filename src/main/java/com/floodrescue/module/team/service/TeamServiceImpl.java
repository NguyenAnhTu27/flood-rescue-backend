package com.floodrescue.module.team.service;

import com.floodrescue.module.team.dto.request.CreateTeamRequest;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public TeamEntity createTeam(CreateTeamRequest request) {
        String name = request.getName().trim();
        // Không cho trùng tên đội (Đội cứu hộ số 1, 2, 3...)
        if (teamRepository.existsByName(name)) {
            throw new BusinessException("Tên đội cứu hộ đã tồn tại");
        }

        // Generate unique code
        String code;
        int attempts = 0;
        do {
            code = CodeGenerator.generateTeamCode();
            attempts++;
            if (attempts > 10) {
                throw new BusinessException("Không thể tạo mã đội cứu hộ duy nhất");
            }
        } while (teamRepository.existsByCode(code));

        TeamEntity team = TeamEntity.builder()
                .code(code)
                .name(name)
                .description(request.getDescription())
                .build();
        return teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamEntity> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamEntity getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đội cứu hộ không tồn tại"));
    }
}   
