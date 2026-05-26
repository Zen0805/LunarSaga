package com.github.zen05.lunarsaga.ai;

/**
 * "Siêu từ điển" kẻ địch — mô phỏng entity_defs.lua của Legend of Lua.
 *
 * Mỗi giá trị Enum tương ứng với 1 loại quái trong game, chứa đầy đủ thông số
 * mặc định.
 * Khi thêm quái mới, chỉ cần thêm 1 dòng Enum ở đây là xong, không cần sửa ở
 * nơi nào khác.
 *
 * TiledAshleyConfigurator sẽ đọc EnemyDef làm GIÁ TRỊ MẶC ĐỊNH, sau đó ưu tiên
 * đọc
 * thêm Custom Properties trong Tiled để ghi đè (Override) nếu cần tùy chỉnh cho
 * 1 ô cụ thể.
 *
 * Đơn vị đo lường: "world unit" (1 unit = 1 ô gạch 16px, đơn vị chuẩn của
 * Box2D).
 */
public enum EnemyDef {

    // ─────────────────────────────────────────────────────────────────────────
    // id sprW sprH bodyR speed wander aggro deAggro leash hp
    // ─────────────────────────────────────────────────────────────────────────
    BAT("bat", 16, 16, 0.3f, 2.0f, 3f, 4f, 6f, 8f, 2),
    SLIME("slime", 16, 16, 0.35f, 0.8f, 2f, 3f, 5f, 6f, 5),
    SKELETON("skeleton", 16, 16, 0.4f, 1.2f, 2f, 5f, 7f, 10f, 4);

    // ─── Trường dữ liệu ───────────────────────────────────────────────────────
    public final String id; // ID phải khớp với "enemyId" trong Tiled
    public final int spriteW; // Chiều rộng sprite (pixel)
    public final int spriteH; // Chiều cao sprite (pixel)
    public final float bodyRadius; // Bán kính body Box2D (world units)
    public final float speed; // Tốc độ di chuyển tối đa (world units/s)
    public final float wanderRadius; // Phạm vi đi dạo quanh Spawn (world units)
    public final float aggroRange; // Tầm phát hiện Player → CHASE (world units)
    public final float deAggroRange; // Tầm mất dấu → bỏ CHASE (world units)
    public final float leashRange; // Dây xích CHASE → quay về tổ (world units)
    public final int hp; // Máu đầy mặc định

    EnemyDef(String id, int spriteW, int spriteH, float bodyRadius, float speed,
            float wanderRadius, float aggroRange, float deAggroRange, float leashRange, int hp) {
        this.id = id;
        this.spriteW = spriteW;
        this.spriteH = spriteH;
        this.bodyRadius = bodyRadius;
        this.speed = speed;
        this.wanderRadius = wanderRadius;
        this.aggroRange = aggroRange;
        this.deAggroRange = deAggroRange;
        this.leashRange = leashRange;
        this.hp = hp;
    }

    /**
     * Tra từ điển bằng enemyId (string từ Tiled).
     * Trả về BAT làm fallback nếu không tìm thấy, để game không crash khi đặt sai
     * tên.
     */
    public static EnemyDef fromId(String id) {
        for (EnemyDef def : values()) {
            if (def.id.equalsIgnoreCase(id))
                return def;
        }
        com.badlogic.gdx.Gdx.app.error("EnemyDef",
                "Không tìm thấy EnemyDef cho id='" + id + "'. Dùng BAT làm fallback.");
        return BAT;
    }
}
