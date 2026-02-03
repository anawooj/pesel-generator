package pl.anawoj.peselgenerator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import pl.anawoj.peselgenerator.model.User;

import java.util.List;

@Repository
public class UserJdbcRepository {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByUsername(String username) {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM toik.users WHERE username=?",
                (rs, i) -> new User(rs.getLong("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("email")),
                username
        );

        if (users.isEmpty()) return null;
        User u = users.get(0);
        List<String> roles = jdbcTemplate.query(
                "SELECT role FROM toik.user_roles WHERE user_id=?",
                (rs, i) -> rs.getString("role"),
                u.getId()
        );
        u.setRoles(roles);
        return u;
    }

    public void registerUser(String username, String password, String email){
        String hashed = passwordEncoder.encode(password);
        String sql = "INSERT INTO toik.users (username, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, username, hashed, email);
        Long user_id = jdbcTemplate.queryForObject("SELECT id FROM toik.users WHERE username = ?", Long.class, username);
        jdbcTemplate.update( "INSERT INTO toik.user_roles (user_id, role) VALUES (?, ?)", user_id, "USER" );
    }

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM toik.users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
}