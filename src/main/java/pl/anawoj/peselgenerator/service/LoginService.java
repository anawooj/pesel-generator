package pl.anawoj.peselgenerator.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pl.anawoj.peselgenerator.repository.UserJdbcRepository;

@Service
public class LoginService implements UserDetailsService {

    private final UserJdbcRepository repo;

    public LoginService(UserJdbcRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repo.findByUsername(username);
    }
}