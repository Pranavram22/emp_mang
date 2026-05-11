package com.employeedb.security;

import com.employeedb.model.AppUser;
import com.employeedb.model.Role;
import com.employeedb.repo.AppUserRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

public class AppUserDetails implements UserDetails {

  private final AppUser user;

  public AppUserDetails(AppUser user) { this.user = user; }

  public AppUser getAppUser() { return user; }
  public Role getRole()       { return user.getRole(); }

  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())); }
  @Override public String getPassword()  { return user.getPasswordHash(); }
  @Override public String getUsername()  { return user.getUsername(); }
  @Override public boolean isAccountNonExpired()     { return true; }
  @Override public boolean isAccountNonLocked()      { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled()               { return true; }
}

@Service
class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepository repo;

  AppUserDetailsService(AppUserRepository repo) { this.repo = repo; }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repo.findByUsername(username)
        .map(AppUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException(username));
  }
}
