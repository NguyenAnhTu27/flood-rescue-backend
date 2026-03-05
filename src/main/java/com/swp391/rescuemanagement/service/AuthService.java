package com.swp391.rescuemanagement.service;

import com.swp391.rescuemanagement.dto.response.UserResponse;
import com.swp391.rescuemanagement.exception.BusinessException;
import com.swp391.rescuemanagement.model.User;
import com.swp391.rescuemanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String SESSION_KEY = "CURRENT_USER";

    private final UserRepository userRepository;

    /**
     * BƯỚC 1 — Đăng nhập qua Session.
     *
     * Luồng:
     * 1. Lấy session hiện có (không tạo mới).
     * 2. Nếu session đã chứa user → 409 Conflict (không cho duplicate).
     * 3. Tìm user theo email → 401 nếu không thấy.
     * 4. So sánh mật khẩu → 401 nếu sai.
     * 5. Tạo session mới, lưu user vào session.
     */
    @Transactional(readOnly = true)
    public User login(String email, String password, HttpServletRequest request) {

        // --- Kiểm tra duplicate session ---
        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute(SESSION_KEY) != null) {
            User who = (User) existing.getAttribute(SESSION_KEY);
            log.warn("Duplicate login attempt – user '{}' đã có session active", who.getEmail());
            throw BusinessException.conflict(
                    "Tài khoản đã đăng nhập. Vui lòng đăng xuất trước khi đăng nhập lại.");
        }

        // --- Xác thực email ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.unauthorized("Email hoặc mật khẩu không đúng."));

        // --- Kiểm tra tài khoản active ---
        if (!"active".equals(user.getStatus())) {
            throw BusinessException.unauthorized("Tài khoản đã bị khoá hoặc vô hiệu hoá.");
        }

        // --- So sánh mật khẩu ---
        // TODO: thay bằng BCryptPasswordEncoder.matches() ở production
        if (!user.getPasswordHash().equals(password)) {
            throw BusinessException.unauthorized("Email hoặc mật khẩu không đúng.");
        }

        // --- Tạo session mới ---
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_KEY, user);
        session.setMaxInactiveInterval(30 * 60); // 30 phút

        log.info("User '{}' đăng nhập thành công. SessionId={}", user.getEmail(), session.getId());
        return user;
    }

    /**
     * Đăng xuất — invalidate session.
     */
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(SESSION_KEY);
            session.invalidate();
            if (user != null) {
                log.info("User '{}' đã đăng xuất.", user.getEmail());
            }
        }
    }

    /**
     * Lấy user hiện tại từ session — dùng trong các controller khác.
     * Ném 401 nếu chưa đăng nhập.
     */
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_KEY) == null) {
            throw BusinessException.unauthorized("Bạn chưa đăng nhập.");
        }
        return (User) session.getAttribute(SESSION_KEY);
    }
}
