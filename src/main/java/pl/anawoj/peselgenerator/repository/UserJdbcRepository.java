package pl.anawoj.peselgenerator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import pl.anawoj.peselgenerator.model.User;

import java.util.List;

/**
 * Repozytorium odpowiedzialne za operacje na użytkownikach z wykorzystaniem JDBC.
 *
 * <p>Klasa korzysta z {@link JdbcTemplate} do wykonywania zapytań SQL oraz
 * {@link PasswordEncoder} do bezpiecznego haszowania haseł podczas rejestracji.
 *
 * <p>Repozytorium obsługuje:
 * <ul>
 *     <li>wyszukiwanie użytkownika po nazwie,</li>
 *     <li>rejestrację nowego użytkownika wraz z przypisaniem roli,</li>
 *     <li>sprawdzanie, czy użytkownik o danej nazwie już istnieje.</li>
 * </ul>
 */
@Repository
public class UserJdbcRepository {

    /** Obiekt ułatwiający wykonywanie zapytań SQL. */
    private final JdbcTemplate jdbcTemplate;

    /** Komponent odpowiedzialny za kodowanie haseł użytkowników. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Konstruktor repozytorium wstrzykujący zależności.
     *
     * @param jdbcTemplate komponent do wykonywania zapytań SQL
     * @param passwordEncoder komponent do haszowania haseł
     */
    public UserJdbcRepository(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Wyszukuje użytkownika na podstawie nazwy użytkownika.
     *
     * <p>Metoda:
     * <ul>
     *     <li>pobiera dane użytkownika z tabeli {@code toik.users},</li>
     *     <li>pobiera przypisane role z tabeli {@code toik.user_roles},</li>
     *     <li>zwraca obiekt {@link User} lub {@code null}, jeśli użytkownik nie istnieje.</li>
     * </ul>
     *
     * @param username nazwa użytkownika
     * @return obiekt {@link User} lub {@code null}, jeśli nie znaleziono użytkownika
     */
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

    /**
     * Rejestruje nowego użytkownika w bazie danych.
     *
     * <p>Proces obejmuje:
     * <ul>
     *     <li>haszowanie hasła,</li>
     *     <li>wstawienie użytkownika do tabeli {@code toik.users},</li>
     *     <li>pobranie wygenerowanego ID użytkownika,</li>
     *     <li>przypisanie roli {@code USER} w tabeli {@code toik.user_roles}.</li>
     * </ul>
     *
     * @param username nazwa użytkownika
     * @param password hasło użytkownika (przed haszowaniem)
     * @param email adres e-mail użytkownika
     */
    public void registerUser(String username, String password, String email) {
        String hashed = passwordEncoder.encode(password);

        String sql = "INSERT INTO toik.users (username, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, username, hashed, email);

        Long user_id = jdbcTemplate.queryForObject(
                "SELECT id FROM toik.users WHERE username = ?",
                Long.class,
                username
        );

        jdbcTemplate.update(
                "INSERT INTO toik.user_roles (user_id, role) VALUES (?, ?)",
                user_id,
                "USER"
        );
    }

    /**
     * Sprawdza, czy użytkownik o podanej nazwie istnieje w bazie danych.
     *
     * @param username nazwa użytkownika
     * @return {@code true}, jeśli użytkownik istnieje; {@code false} w przeciwnym razie
     */
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM toik.users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
}