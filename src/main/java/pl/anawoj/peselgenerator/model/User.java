package pl.anawoj.peselgenerator.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Model reprezentujący użytkownika aplikacji, implementujący interfejs
 * {@link org.springframework.security.core.userdetails.UserDetails},
 * dzięki czemu może być wykorzystywany przez Spring Security
 * podczas procesu uwierzytelniania i autoryzacji.
 *
 * <p>Obiekt użytkownika zawiera:
 * <ul>
 *     <li>identyfikator użytkownika,</li>
 *     <li>nazwę użytkownika,</li>
 *     <li>zahaszowane hasło,</li>
 *     <li>adres e-mail,</li>
 *     <li>listę ról użytkownika.</li>
 * </ul>
 *
 * <p>Klasa jest niemutowalna poza listą ról, która może zostać ustawiona
 * po pobraniu użytkownika z bazy danych.
 */
public class User implements UserDetails {

    /** Unikalny identyfikator użytkownika. */
    private final Long id;

    /** Nazwa użytkownika wykorzystywana do logowania. */
    private final String username;

    /** Zahasowane hasło użytkownika. */
    private final String password;

    /** Adres e-mail użytkownika. */
    private final String email;

    /** Lista ról przypisanych użytkownikowi. */
    private List<String> roles;

    /**
     * Konstruktor tworzący obiekt użytkownika.
     *
     * @param id identyfikator użytkownika
     * @param username nazwa użytkownika
     * @param password zahaszowane hasło użytkownika
     * @param email adres e-mail użytkownika
     */
    public User(Long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * Zwraca identyfikator użytkownika.
     *
     * @return identyfikator użytkownika
     */
    public Long getId() {
        return id;
    }

    /**
     * Ustawia listę ról użytkownika.
     *
     * @param roles lista ról (np. "USER", "ADMIN")
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Zwraca kolekcję uprawnień użytkownika na podstawie jego ról.
     *
     * @return kolekcja obiektów {@link org.springframework.security.core.GrantedAuthority}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    /**
     * Zwraca zahaszowane hasło użytkownika.
     *
     * @return hasło użytkownika
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Zwraca nazwę użytkownika.
     *
     * @return nazwa użytkownika
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Zwraca adres e-mail użytkownika.
     *
     * @return adres e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Informuje, czy konto użytkownika nie wygasło.
     *
     * @return zawsze true — konta nie wygasają
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Informuje, czy konto użytkownika nie jest zablokowane.
     *
     * @return zawsze true — konta nie są blokowane
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Informuje, czy poświadczenia użytkownika nie wygasły.
     *
     * @return zawsze true — poświadczenia nie wygasają
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Informuje, czy konto użytkownika jest aktywne.
     *
     * @return zawsze true — wszystkie konta są aktywne
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}