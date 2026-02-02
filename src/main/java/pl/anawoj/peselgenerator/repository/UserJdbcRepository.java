package pl.anawoj.peselgenerator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.anawoj.peselgenerator.model.User;

import java.util.List;

@Repository
public class UserJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        String sql = "INSERT INTO toik.users (username, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, username, password, email);
    }

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0; }
    }