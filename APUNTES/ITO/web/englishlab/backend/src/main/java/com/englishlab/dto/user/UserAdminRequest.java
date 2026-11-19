package com.englishlab.dto.user;

import com.englishlab.model.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta/edición de usuario por parte del ADMIN.
 *
 * <p>{@code password} es obligatoria al crear (el servicio la exige) y opcional al
 * editar (si viene en blanco se conserva la actual). {@code teacherId}, cuando se
 * indica, debe referenciar a un TEACHER.
 */
public record UserAdminRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @Size(max = 72, message = "La contraseña no puede superar los 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
        )
        String password,

        @NotNull(message = "El rol es obligatorio")
        RoleName role,

        Long teacherId,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone
) {}