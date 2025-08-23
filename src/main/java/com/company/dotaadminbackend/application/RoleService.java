package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.model.Role;
import com.company.dotaadminbackend.domain.role.dto.CreateRoleRequest;
import com.company.dotaadminbackend.domain.role.dto.UpdateRoleRequest;
import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RoleService
 * --------------------------------------------------------------------
 * 역할:
 *  - API / 비즈니스 레이어에서 Role(Entity) CRUD를 수행한다.
 *  - 이미 DB(또는 초기화 로직)에 존재하는 Role 들을 조회/생성/수정/삭제.
 *
 * 하지 않는 것(중요):
 *  - "기본(USER / ADMIN 등) 롤 자동 생성" 책임은 여기 없다.
 *      -> 애플리케이션 기동 시 1회 보장되는 seeding 은 bootstrap 패키지의
 *         {@link com.company.dotaadminbackend.bootstrap.RoleInitializer} 가 담당.
 *  - "관리자(ADMIN) 계정 자동 생성" 책임도 없다.
 *      -> {@link com.company.dotaadminbackend.bootstrap.UserInitializer} 참조.
 *  - Authority(권한 코드) 매핑/생성 로직은 현재 이 서비스에 포함되어 있지 않다.
 *      -> 향후 권한 세분화를 재도입하면 별도 Service 나 Initializer / Migration 으로 분리 권장.
 *
 * 왜 분리했나?
 *  - Seed(초기 데이터 보장) 로직은 "애플리케이션 시작 시 1회" 수행되어야 하며,
 *    일반적인 API 호출 흐름과 구분하지 않으면 중복 생성 / 불필요한 트랜잭션 발생 가능.
 *  - 따라서 CRUD 서비스는 순수하게 요청 기반 동작만 담고, 초기 보장/정책은 bootstrap + UserService(등록 정책) 로 분리.
 *
 * createRole 메서드는 단순히 중복 이름 검증 후 Role 을 저장할 뿐,
 * 기본 Role 이 비어 있을 때 자동 채우는 로직을 수행하지 않는다.
 */
@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
    log.debug("[RoleService] Fetching all roles from repository");
    List<Role> list = roleRepository.findAll()
                .stream()
                .map(this::toRole)
                .collect(Collectors.toList());
    log.debug("[RoleService] Fetched {} roles", list.size());
    return list;
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    log.debug("[RoleService] Found role id={}", id);
        return toRole(entity);
    }

    public Role createRole(CreateRoleRequest request) {
        // (1) 여기서 하는 일: 동일 이름 Role 존재 여부 검증 -> 신규 Role 저장
        // (2) 여기서 안 하는 일: 기본 Role(USER/ADMIN) 보장, Authority 매핑 자동생성
        //     -> 기동 시 seeding 은 RoleInitializer 가 이미 처리. 따라서 여기선 순수 CRUD.
        if (roleRepository.existsByName(request.getName())) {
            String msg = "Role with name '" + request.getName() + "' already exists";
            log.warn("[RoleService] {}", msg);
            throw new RuntimeException(msg);
        }

        RoleEntity entity = new RoleEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        RoleEntity savedEntity = roleRepository.save(entity);
        log.debug("[RoleService] Created role id={}", savedEntity.getId());
        return toRole(savedEntity);
    }

    public Role updateRole(Long id, UpdateRoleRequest request) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check if name is already taken by another role
        if (!entity.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            String msg = "Role with name '" + request.getName() + "' already exists";
            log.warn("[RoleService] {}", msg);
            throw new RuntimeException(msg);
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        
    RoleEntity savedEntity = roleRepository.save(entity);
    log.debug("[RoleService] Updated role id={}", savedEntity.getId());
    return toRole(savedEntity);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            String msg = "Role not found with id: " + id;
            log.warn("[RoleService] {}", msg);
            throw new RuntimeException(msg);
        }
        roleRepository.deleteById(id);
        log.debug("[RoleService] Deleted role id={}", id);
    }

    private Role toRole(RoleEntity entity) {
        return new Role(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}