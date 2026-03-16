package com.floodrescue.config.seed;

import com.floodrescue.module.asset.entity.AssetEntity;
import com.floodrescue.module.asset.repository.AssetRepository;
import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import com.floodrescue.module.inventory.repository.ItemCategoryRepository;
import com.floodrescue.module.relief.entity.ReliefRequestEntity;
import com.floodrescue.module.relief.entity.ReliefRequestLineEntity;
import com.floodrescue.module.relief.repository.ReliefRequestLineRepository;
import com.floodrescue.module.relief.repository.ReliefRequestRepository;
import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.enums.RescueRequestStatus;
import com.floodrescue.shared.enums.AssetStatus;
import com.floodrescue.shared.enums.AssetType;
import com.floodrescue.shared.enums.InventoryDocumentStatus;
import com.floodrescue.shared.enums.ReliefDeliveryStatus;
import com.floodrescue.shared.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Seed roles + default coordinator account for local/dev usage.
 * Controlled by app.seed.enabled in application.properties.
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class SeedDataRunner implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository;
    private final AssetRepository assetRepository;
    private final RescueRequestRepository rescueRequestRepository;
    private final ReliefRequestRepository reliefRequestRepository;
    private final ReliefRequestLineRepository reliefRequestLineRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.coordinator.email:coordinator@example.com}")
    private String coordinatorEmail;

    @Value("${app.seed.coordinator.phone:0900000001}")
    private String coordinatorPhone;

    @Value("${app.seed.coordinator.password:Password123}")
    private String coordinatorPassword;

    @Value("${app.seed.coordinator.full-name:Điều phối viên mặc định}")
    private String coordinatorFullName;

    // Admin seed (for demo / local)
    @Value("${app.seed.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.seed.admin.phone:0900000000}")
    private String adminPhone;

    @Value("${app.seed.admin.password:Admin123}")
    private String adminPassword;

    @Value("${app.seed.admin.full-name:Quản trị viên hệ thống}")
    private String adminFullName;

    // Manager seed (for demo / local)
    @Value("${app.seed.manager.email:manager@example.com}")
    private String managerEmail;

    @Value("${app.seed.manager.phone:0900000003}")
    private String managerPhone;

    @Value("${app.seed.manager.password:Manager123}")
    private String managerPassword;

    @Value("${app.seed.manager.full-name:Quản lý hệ thống}")
    private String managerFullName;

    // Team 1 seed
    @Value("${app.seed.team1.phone:0910000001}")
    private String team1Phone;
    @Value("${app.seed.team1.email:team1@gmail.com}")
    private String team1Email;
    @Value("${app.seed.team1.password:Team123}")
    private String team1Password;
    @Value("${app.seed.team1.full-name:Đội trưởng Đội Cứu hộ số 1}")
    private String team1FullName;

    // Team 2 seed
    @Value("${app.seed.team2.phone:0910000002}")
    private String team2Phone;
    @Value("${app.seed.team2.email:team2@gmail.com}")
    private String team2Email;
    @Value("${app.seed.team2.password:Team123}")
    private String team2Password;
    @Value("${app.seed.team2.full-name:Đội trưởng Đội Cứu hộ số 2}")
    private String team2FullName;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;

        // Ensure roles exist
        RoleEntity citizenRole = ensureRole("CITIZEN", "Công dân");
        RoleEntity coordinatorRole = ensureRole("COORDINATOR", "Điều phối");
        RoleEntity rescuerRole = ensureRole("RESCUER", "Đội cứu hộ");
        RoleEntity managerRole = ensureRole("MANAGER", "Quản lý");
        RoleEntity adminRole = ensureRole("ADMIN", "Admin");

        // Seed default admin + coordinator + manager accounts (if not exists)
        seedAdminUser(adminRole);
        seedCoordinatorUser(coordinatorRole);
        seedManagerUser(managerRole);

        // Seed team accounts (RESCUER role) - cần có team trước
        seedTeamUsers(rescuerRole);

        // Seed citizen accounts + rescue requests for local/dev demo
        List<UserEntity> citizens = seedCitizenUsers(citizenRole);
        seedRescueRequests(citizens);
        seedReliefRequests(citizens);

        // Seed realistic assets for local/dev demo and normalize old placeholder data
        seedAssets();
    }

        private List<UserEntity> seedCitizenUsers(RoleEntity citizenRole) {
        List<CitizenSeed> seeds = List.of(
            new CitizenSeed("0901000001", "citizen1@gmail.com", "Citizen123", "Nguyen Van An"),
            new CitizenSeed("0901000002", "citizen2@gmail.com", "Citizen123", "Tran Thi Binh"),
            new CitizenSeed("0901000003", "citizen3@gmail.com", "Citizen123", "Le Minh Chau"),
            new CitizenSeed("0901000004", "citizen4@gmail.com", "Citizen123", "Pham Quoc Dung"),
            new CitizenSeed("0901000005", "citizen5@gmail.com", "Citizen123", "Vo Ngoc Ha")
        );

        List<UserEntity> citizens = new ArrayList<>();
        for (CitizenSeed seed : seeds) {
            String email = normalizeEmail(seed.email());
            String phone = normalizePhone(seed.phone());

            UserEntity user = null;
            if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
            }
            if (user == null && phone != null) {
            user = userRepository.findByPhone(phone).orElse(null);
            }

            if (user == null) {
            LocalDateTime now = LocalDateTime.now();
            user = UserEntity.builder()
                .role(citizenRole)
                .teamId(null)
                .fullName(seed.fullName())
                .phone(phone)
                .email(email)
                .passwordHash(passwordEncoder.encode(seed.password()))
                .status((byte) 1)
                .isLeader(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
            user = userRepository.save(user);
            }

            citizens.add(user);
        }

        return citizens;
        }

        private void seedRescueRequests(List<UserEntity> citizens) {
        if (citizens == null || citizens.isEmpty()) {
            return;
        }

        List<RescueRequestSeed> seeds = List.of(
            new RescueRequestSeed("RRSEED260001", 0, RescueRequestStatus.PENDING, RescuePriority.HIGH, 5,
                "Có người già và trẻ nhỏ mắc kẹt trên tầng 2, nước dâng nhanh.",
                "123 Nguyen Van Linh, Quan 7, TP.HCM", 10.7321, 106.7215,
                "Nhà màu xanh cạnh tiệm thuốc"),
            new RescueRequestSeed("RRSEED260002", 1, RescueRequestStatus.PENDING, RescuePriority.MEDIUM, 2,
                "Hai người bị kẹt trong xe giữa đoạn ngập sâu.",
                "45 Le Loi, Quan 1, TP.HCM", 10.7753, 106.7018,
                "Gần giao lộ với Nam Ky Khoi Nghia"),
            new RescueRequestSeed("RRSEED260003", 2, RescueRequestStatus.VERIFIED, RescuePriority.HIGH, 4,
                "Mất điện và liên lạc, cần hỗ trợ sơ tán khẩn.",
                "88 Quang Trung, Go Vap, TP.HCM", 10.8382, 106.6687,
                "Hẻm 88, cuối hẻm có biển đỏ"),
            new RescueRequestSeed("RRSEED260004", 3, RescueRequestStatus.ASSIGNED, RescuePriority.MEDIUM, 3,
                "Gia đình có người bệnh nền cần di chuyển đến điểm an toàn.",
                "210 Kha Van Can, Thu Duc, TP.HCM", 10.8501, 106.7552,
                "Đối diện trường tiểu học"),
            new RescueRequestSeed("RRSEED260005", 4, RescueRequestStatus.IN_PROGRESS, RescuePriority.HIGH, 6,
                "Nước ngập tới ngực, có 6 người cần áo phao và xuồng.",
                "15A Vo Van Kiet, Binh Tan, TP.HCM", 10.7426, 106.6044,
                "Ngay chân cầu vượt"),
            new RescueRequestSeed("RRSEED260006", 0, RescueRequestStatus.COMPLETED, RescuePriority.LOW, 1,
                "Đã được đội cứu hộ tiếp cận và đưa tới điểm tập kết an toàn.",
                "5 Tran Hung Dao, Quan 5, TP.HCM", 10.7558, 106.6701,
                "Nhà số 5"),
            new RescueRequestSeed("RRSEED260007", 1, RescueRequestStatus.CANCELLED, RescuePriority.LOW, 2,
                "Tự di chuyển được sau khi nước rút, xin hủy yêu cầu.",
                "300 Pham Van Dong, Thu Duc, TP.HCM", 10.8294, 106.7365,
                "Khu chung cư A"),
            new RescueRequestSeed("RRSEED260008", 2, RescueRequestStatus.PENDING, RescuePriority.LOW, 1,
                "Cần hỗ trợ vận chuyển thuốc cho người lớn tuổi.",
                "16 Cach Mang Thang 8, Quan 3, TP.HCM", 10.7812, 106.6841,
                "Căn hộ tầng trệt"),
            new RescueRequestSeed("RRSEED260009", 3, RescueRequestStatus.VERIFIED, RescuePriority.MEDIUM, 3,
                "Nước tràn vào nhà nhanh, cần hỗ trợ đưa trẻ em đến nơi an toàn.",
                "72 Huynh Tan Phat, Quan 7, TP.HCM", 10.7297, 106.7163,
                "Gần chợ phường"),
            new RescueRequestSeed("RRSEED260010", 4, RescueRequestStatus.ASSIGNED, RescuePriority.HIGH, 7,
                "Nhiều hộ dân bị cô lập trong hẻm sâu, cần điều xuồng cứu hộ.",
                "110 To Ky, Quan 12, TP.HCM", 10.8527, 106.6208,
                "Hẻm 110, khu nhà trọ cuối hẻm")
        );

        for (RescueRequestSeed seed : seeds) {
            if (rescueRequestRepository.existsByCode(seed.code())) {
            continue;
            }

            UserEntity citizen = citizens.get(seed.citizenIndex() % citizens.size());
            RescueRequestEntity request = RescueRequestEntity.builder()
                .code(seed.code())
                .citizen(citizen)
                .status(seed.status())
                .priority(seed.priority())
                .affectedPeopleCount(seed.affectedPeopleCount())
                .description(seed.description())
                .addressText(seed.addressText())
                .latitude(seed.latitude())
                .longitude(seed.longitude())
                .locationDescription(seed.locationDescription())
                .locationVerified(seed.status() != RescueRequestStatus.PENDING)
                .waitingForTeam(seed.status() == RescueRequestStatus.ASSIGNED)
                .build();

            rescueRequestRepository.save(request);
        }
        }

            private void seedReliefRequests(List<UserEntity> citizens) {
            if (citizens == null || citizens.isEmpty()) {
                return;
            }

            List<ItemCategoryEntity> itemCategories = itemCategoryRepository.findAll();
            if (itemCategories.isEmpty()) {
                return;
            }

            List<RescueRequestEntity> rescueRequests = rescueRequestRepository.findAll();

            List<ReliefRequestSeed> seeds = List.of(
                new ReliefRequestSeed("IRSEED260001", 0, InventoryDocumentStatus.DRAFT, ReliefDeliveryStatus.REQUESTED,
                    "Phuong Tan Thuan Dong, Quan 7", "120 Huynh Tan Phat, Quan 7, TP.HCM",
                    10.7287, 106.7204, "Tram y te phuong can tiep te khan cap", "Can bo sung nuoc uong va do an kho."),
                new ReliefRequestSeed("IRSEED260002", 1, InventoryDocumentStatus.APPROVED, ReliefDeliveryStatus.MANAGER_APPROVED,
                    "Phuong 13, Quan 8", "61 Ta Quang Buu, Quan 8, TP.HCM",
                    10.7448, 106.6562, "Diem tap ket gan UBND phuong", "Uu tien sua va bo so cuu."),
                new ReliefRequestSeed("IRSEED260003", 2, InventoryDocumentStatus.APPROVED, ReliefDeliveryStatus.RESCUER_RECEIVED,
                    "Phuong Linh Xuan, Thu Duc", "22 Quoc lo 1K, Thu Duc, TP.HCM",
                    10.8771, 106.7712, "Khu nha tro sau cho Linh Xuan", "Can ao mua va men cho tre em."),
                new ReliefRequestSeed("IRSEED260004", 3, InventoryDocumentStatus.DONE, ReliefDeliveryStatus.COMPLETED,
                    "Phuong 5, Go Vap", "90 Nguyen Oanh, Go Vap, TP.HCM",
                    10.8369, 106.6794, "Diem phat qua da hoan tat", "Da giao day du theo nhu cau."),
                new ReliefRequestSeed("IRSEED260005", 4, InventoryDocumentStatus.CANCELLED, ReliefDeliveryStatus.REJECTED,
                    "Phuong 9, Tan Binh", "15 Truong Son, Tan Binh, TP.HCM",
                    10.8107, 106.6648, "Khu dan cu da duoc tiep te boi don vi khac", "Tam ngung yeu cau vi da du vat pham."),
                new ReliefRequestSeed("IRSEED260006", 0, InventoryDocumentStatus.DRAFT, ReliefDeliveryStatus.REQUESTED,
                    "Phuong An Phu Dong, Quan 12", "180 Vong Cung, Quan 12, TP.HCM",
                    10.8632, 106.6967, "Khu nha cap 4 ven kenh", "Can bo sung man va den pin.")
            );

            for (int i = 0; i < seeds.size(); i++) {
                ReliefRequestSeed seed = seeds.get(i);
                if (reliefRequestRepository.existsByCode(seed.code())) {
                continue;
                }

                UserEntity createdBy = citizens.get(seed.createdByIndex() % citizens.size());
                RescueRequestEntity linkedRescue = rescueRequests.isEmpty() ? null : rescueRequests.get(i % rescueRequests.size());

                ReliefRequestEntity request = ReliefRequestEntity.builder()
                    .code(seed.code())
                    .createdById(createdBy.getId())
                    .status(seed.status())
                    .deliveryStatus(seed.deliveryStatus())
                    .targetArea(seed.targetArea())
                    .addressText(seed.addressText())
                    .latitude(seed.latitude())
                    .longitude(seed.longitude())
                    .locationDescription(seed.locationDescription())
                    .rescueRequest(linkedRescue)
                    .note(seed.note())
                    .build();

                ReliefRequestEntity saved = reliefRequestRepository.save(request);

                List<ReliefRequestLineEntity> lines = new ArrayList<>();
                for (int offset = 0; offset < 2; offset++) {
                ItemCategoryEntity category = itemCategories.get((i + offset) % itemCategories.size());
                lines.add(ReliefRequestLineEntity.builder()
                    .reliefRequest(saved)
                    .itemCategory(category)
                    .qty(BigDecimal.valueOf(20 + (i * 5L) + (offset * 10L)))
                    .unit(category.getUnit())
                    .build());
                }
                reliefRequestLineRepository.saveAll(lines);
            }
            }

    private void seedAssets() {
        List<AssetSeed> seeds = List.of(
                new AssetSeed("CN-042", "High-speed rescue canoe 42", AssetType.CANO, 8,
                        "Ben Chuong Duong, District 1 | Fast-response water rescue for urban flooding."),
                new AssetSeed("CN-091", "Inflatable rescue canoe 91", AssetType.CANO, 6,
                        "Ninh Kieu Wharf, Can Tho | Riverbank rescue and evacuation support."),
                new AssetSeed("AM-108", "High-clearance flood truck AM-108", AssetType.TRUCK, 10,
                        "Binh Chanh District, HCMC | Delivers supplies into deep flood zones."),
                new AssetSeed("AM-202", "Logistics flood truck AM-202", AssetType.TRUCK, 12,
                        "Thu Duc central depot | Carries food, medicine, and rescue tools."),
                new AssetSeed("GEN-22", "Mobile generator unit 22", AssetType.GENERATOR, 5,
                        "Thu Duc central depot | Emergency power for field clinics and shelters."),
                new AssetSeed("GEN-07", "Emergency generator GEN-07", AssetType.GENERATOR, 5,
                        "Phong Nha medical station | Backup power for nighttime operations."),
                new AssetSeed("DR-15", "Flood reconnaissance drone DR-15", AssetType.DRONE, 1,
                        "District 7 command center | Searches stranded civilians in flooded areas."),
                new AssetSeed("DR-21", "Thermal camera drone DR-21", AssetType.DRONE, 1,
                        "South Saigon response zone | Night monitoring and victim detection."),
                new AssetSeed("MD-31", "Medical emergency kit MD-31", AssetType.MEDICAL_KIT, 30,
                        "Binh Dong reserve warehouse | 30 first-aid sets ready for deployment."),
                new AssetSeed("PM-12", "High-capacity drainage pump PM-12", AssetType.PUMP, 1,
                        "Binh Tan District | Removes floodwater from basements and low roads."),
                new AssetSeed("PM-18", "Electric suction pump PM-18", AssetType.PUMP, 1,
                        "Hoc Mon logistics hub | Rapid drainage after heavy rainfall."),
                new AssetSeed("VC-05", "Relief transport van VC-05", AssetType.VAN, 15,
                        "District 12 relief warehouse | Carries dry food, drinking water, and medicine."),
                new AssetSeed("AMB-03", "Emergency ambulance AMB-03", AssetType.AMBULANCE, 4,
                        "District 8 field clinic | Transfers severe patients to partner hospitals.")
        );

        List<AssetEntity> existingAssets = new ArrayList<>(assetRepository.findAll());
        for (AssetSeed seed : seeds) {
            AssetEntity asset = findByNormalizedCode(existingAssets, seed.code())
                    .orElseGet(() -> AssetEntity.builder()
                            .status(AssetStatus.AVAILABLE)
                            .build());

            asset.setCode(seed.code());
            asset.setName(seed.name());
            asset.setAssetType(seed.assetType());
            asset.setCapacity(seed.capacity());
            asset.setNote(seed.note());
            if (asset.getStatus() == null) {
                asset.setStatus(AssetStatus.AVAILABLE);
            }

            AssetEntity saved = assetRepository.save(asset);
            if (!existingAssets.contains(saved)) {
                existingAssets.add(saved);
            }
        }
    }

    private java.util.Optional<AssetEntity> findByNormalizedCode(List<AssetEntity> assets, String expectedCode) {
        String normalizedExpected = normalizeAssetCode(expectedCode);
        return assets.stream()
                .filter(asset -> normalizeAssetCode(asset.getCode()).equals(normalizedExpected))
                .findFirst();
    }

    private String normalizeAssetCode(String code) {
        if (code == null) return "";
        return code.replace("#", "").trim().toUpperCase();
    }

    private record AssetSeed(
            String code,
            String name,
            AssetType assetType,
            Integer capacity,
            String note
    ) {
    }

        private record CitizenSeed(
            String phone,
            String email,
            String password,
            String fullName
        ) {
        }

        private record RescueRequestSeed(
            String code,
            int citizenIndex,
            RescueRequestStatus status,
            RescuePriority priority,
            int affectedPeopleCount,
            String description,
            String addressText,
            Double latitude,
            Double longitude,
            String locationDescription
        ) {
        }

            private record ReliefRequestSeed(
                String code,
                int createdByIndex,
                InventoryDocumentStatus status,
                ReliefDeliveryStatus deliveryStatus,
                String targetArea,
                String addressText,
                Double latitude,
                Double longitude,
                String locationDescription,
                String note
            ) {
            }

    private RoleEntity ensureRole(String code, String name) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> {
                    RoleEntity role = RoleEntity.builder()
                            .code(code)
                            .name(name)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return roleRepository.save(role);
                });
    }

    private void seedAdminUser(RoleEntity adminRole) {
        String email = normalizeEmail(adminEmail);
        String phone = normalizePhone(adminPhone);
        if (email == null && phone == null) {
            return;
        }

        boolean exists = userExistsByEmailOrPhone(email, phone);
        if (exists) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        UserEntity admin = UserEntity.builder()
                .role(adminRole)
                .teamId(null)
                .fullName(adminFullName)
                .phone(phone)
                .email(email)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .status((byte) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(admin);
    }

    private void seedCoordinatorUser(RoleEntity coordinatorRole) {
        String email = normalizeEmail(coordinatorEmail);
        String phone = normalizePhone(coordinatorPhone);
        if (email == null && phone == null) {
            return;
        }

        boolean exists = userExistsByEmailOrPhone(email, phone);
        if (exists) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        UserEntity user = UserEntity.builder()
                .role(coordinatorRole)
                .teamId(null)
                .fullName(coordinatorFullName)
                .phone(phone)
                .email(email)
                .passwordHash(passwordEncoder.encode(coordinatorPassword))
                .status((byte) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(user);
    }

    private void seedManagerUser(RoleEntity managerRole) {
        String email = normalizeEmail(managerEmail);
        String phone = normalizePhone(managerPhone);
        if (email == null && phone == null) {
            return;
        }

        boolean exists = userExistsByEmailOrPhone(email, phone);
        if (exists) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        UserEntity manager = UserEntity.builder()
                .role(managerRole)
                .teamId(null)
                .fullName(managerFullName)
                .phone(phone)
                .email(email)
                .passwordHash(passwordEncoder.encode(managerPassword))
                .status((byte) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(manager);
    }

    /**
     * Seed tài khoản đội cứu hộ (RESCUER role).
     * Tự động tạo team nếu chưa có, rồi gán user vào team đó.
     */
    private void seedTeamUsers(RoleEntity rescuerRole) {
        // Đảm bảo có ít nhất 2 team để gán user
        TeamEntity team1 = ensureTeam("Đội Cứu hộ số 1", "Đội phản ứng nhanh khu vực trung tâm");
        TeamEntity team2 = ensureTeam("Đội Cứu hộ số 2", "Đội hỗ trợ khu vực ngoại thành");

        // Seed user cho Team 1
        seedTeamUser(rescuerRole, team1.getId(), team1Phone, team1Email, team1Password, team1FullName);

        // Seed user cho Team 2
        seedTeamUser(rescuerRole, team2.getId(), team2Phone, team2Email, team2Password, team2FullName);
    }

    /**
     * Đảm bảo team tồn tại, nếu chưa có thì tạo mới.
     */
    private TeamEntity ensureTeam(String name, String description) {
        return teamRepository.findByName(name)
                .orElseGet(() -> {
                    String code = CodeGenerator.generateTeamCode();
                    // Đảm bảo code unique
                    int attempts = 0;
                    while (teamRepository.existsByCode(code) && attempts < 20) {
                        code = CodeGenerator.generateTeamCode();
                        attempts++;
                    }
                    if (teamRepository.existsByCode(code)) {
                        throw new IllegalStateException("Không thể tạo mã team unique cho " + name);
                    }

                    TeamEntity team = TeamEntity.builder()
                            .code(code)
                            .name(name)
                            .description(description)
                            .build();
                    return teamRepository.save(team);
                });
    }

    /**
     * Seed 1 user RESCUER gán vào team.
     */
    private void seedTeamUser(RoleEntity rescuerRole, Long teamId, String phone, String email, String password, String fullName) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);
        if (normalizedEmail == null && normalizedPhone == null) {
            return;
        }

        // Check xem đã tồn tại user với phone hoặc email này chưa
        boolean exists = userExistsByEmailOrPhone(normalizedEmail, normalizedPhone);
        if (exists) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        UserEntity user = UserEntity.builder()
                .role(rescuerRole)
                .teamId(teamId) // Gán vào team
                .fullName(fullName)
                .phone(normalizedPhone)
                .email(normalizedEmail) // Set email
                .passwordHash(passwordEncoder.encode(password))
                .status((byte) 1)
                .isLeader(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(user);
    }

    private boolean userExistsByEmailOrPhone(String email, String phone) {
        return (email != null && userRepository.existsByEmail(email))
                || (phone != null && userRepository.existsByPhone(phone));
    }

    private String normalizeEmail(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
