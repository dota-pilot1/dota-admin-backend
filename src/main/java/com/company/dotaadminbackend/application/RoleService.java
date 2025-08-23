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
 * ??븷:
 *  - API / 鍮꾩쫰?덉뒪 ?덉씠?댁뿉??Role(Entity) CRUD瑜??섑뻾?쒕떎.
 *  - ?대? DB(?먮뒗 珥덇린??濡쒖쭅)??議댁옱?섎뒗 Role ?ㅼ쓣 議고쉶/?앹꽦/?섏젙/??젣.
 *
 * ?섏? ?딅뒗 寃?以묒슂):
 *  - "湲곕낯(USER / ADMIN ?? 濡??먮룞 ?앹꽦" 梨낆엫? ?ш린 ?녿떎.
 *      -> ?좏뵆由ъ??댁뀡 湲곕룞 ??1??蹂댁옣?섎뒗 seeding ? bootstrap ?⑦궎吏??
 *         {@link com.company.dotaadminbackend.bootstrap.RoleInitializer} 媛 ?대떦.
 *  - "愿由ъ옄(ADMIN) 怨꾩젙 ?먮룞 ?앹꽦" 梨낆엫???녿떎.
 *      -> {@link com.company.dotaadminbackend.bootstrap.UserInitializer} 李몄“.
 *  - Authority(沅뚰븳 肄붾뱶) 留ㅽ븨/?앹꽦 濡쒖쭅? ?꾩옱 ???쒕퉬?ㅼ뿉 ?ы븿?섏뼱 ?덉? ?딅떎.
 *      -> ?ν썑 沅뚰븳 ?몃텇?붾? ?щ룄?낇븯硫?蹂꾨룄 Service ??Initializer / Migration ?쇰줈 遺꾨━ 沅뚯옣.
 *
 * ??遺꾨━?덈굹?
 *  - Seed(珥덇린 ?곗씠??蹂댁옣) 濡쒖쭅? "?좏뵆由ъ??댁뀡 ?쒖옉 ??1?? ?섑뻾?섏뼱???섎ŉ,
 *    ?쇰컲?곸씤 API ?몄텧 ?먮쫫怨?援щ텇?섏? ?딆쑝硫?以묐났 ?앹꽦 / 遺덊븘?뷀븳 ?몃옖??뀡 諛쒖깮 媛??
 *  - ?곕씪??CRUD ?쒕퉬?ㅻ뒗 ?쒖닔?섍쾶 ?붿껌 湲곕컲 ?숈옉留??닿퀬, 珥덇린 蹂댁옣/?뺤콉? bootstrap + UserService(?깅줉 ?뺤콉) 濡?遺꾨━.
 *
 * createRole 硫붿꽌?쒕뒗 ?⑥닚??以묐났 ?대쫫 寃利???Role ????ν븷 肉?
 * 湲곕낯 Role ??鍮꾩뼱 ?덉쓣 ???먮룞 梨꾩슦??濡쒖쭅???섑뻾?섏? ?딅뒗??
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
        // (1) ?ш린???섎뒗 ?? ?숈씪 ?대쫫 Role 議댁옱 ?щ? 寃利?-> ?좉퇋 Role ???
        // (2) ?ш린?????섎뒗 ?? 湲곕낯 Role(USER/ADMIN) 蹂댁옣, Authority 留ㅽ븨 ?먮룞?앹꽦
        //     -> 湲곕룞 ??seeding ? RoleInitializer 媛 ?대? 泥섎━. ?곕씪???ш린???쒖닔 CRUD.
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
