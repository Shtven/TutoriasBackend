package com.codespace.tutorias.Repositories;
import com.codespace.tutorias.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {
        User findByCorreo(String correo);
}
