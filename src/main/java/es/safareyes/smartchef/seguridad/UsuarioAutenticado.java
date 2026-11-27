package es.safareyes.smartchef.seguridad;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UsuarioAutenticado implements UserDetails {

    private final Integer id;
    private final String email;
    private final String rol;
    private final String passwordHash;
    private final boolean activo;

    public UsuarioAutenticado(Integer id, String email, String rol, String passwordHash, boolean activo) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.passwordHash = passwordHash;
        this.activo = activo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
