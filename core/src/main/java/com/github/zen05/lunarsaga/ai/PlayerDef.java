package com.github.zen05.lunarsaga.ai;

/**
 * Định nghĩa tất cả các thông số của Player.
 * Tập trung tại đây để dễ dàng cân bằng game (balance) thay vì hardcode rải rác.
 */
public class PlayerDef {

    // --- Giai đoạn 5 trở về trước ---
    public static final int   MAX_HP          = 5;
    public static final float IFRAME_DURATION = 1.0f;
    public static final float SPEED           = 4.0f; // Mặc định từ Tiled thường là 4.0

    // --- Giai đoạn 6 — Charge Shot ---
    /** Thời gian giữ chuột tối đa để đạt full charge (giây). */
    public static final float CHARGE_MAX_TIME   = 1.0f;
    /** Tốc độ mũi tên khi nhấp chuột (không tụ lực). */
    public static final float ARROW_SPEED_MIN   = 4.0f;
    /** Tốc độ mũi tên khi full charge. */
    public static final float ARROW_SPEED_MAX   = 12.0f;
    /** Tầm xa (thời gian bay) khi nhấp chuột. Càng lớn bay càng xa. */
    public static final float ARROW_LIFETIME_MIN = 0.15f;
    /** Tầm xa (thời gian bay) khi full charge. Càng lớn bay càng xa. */
    public static final float ARROW_LIFETIME_MAX = 0.8f;
    /** Sát thương khi nhấp chuột thường. */
    public static final int   ARROW_DAMAGE_MIN  = 1;
    /** Sát thương khi full charge. */
    public static final int   ARROW_DAMAGE_MAX  = 3;
    /** Cao độ ban đầu của mũi tên khi bắn (càng nhỏ càng sát đất). */
    public static final float ARROW_INITIAL_Z   = 0.1f;
    /**
     * Trọng lực tác dụng lên mũi tên (Fake Z-Axis).
     * => CHỈNH ĐỘ CONG Ở ĐÂY: Số càng LỚN thì đường cong càng vồng lên cao. Số càng NHỎ thì càng bay là là sát đất.
     */
    public static final float ARROW_GRAVITY     = 1.0f;

    private PlayerDef() {
        // Ngăn khởi tạo object vì đây là class chứa hằng số
    }
}
