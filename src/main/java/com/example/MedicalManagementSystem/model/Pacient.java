package com.example.MedicalManagementSystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Table(name = "pacienti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pacient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pacientId;

    @NotBlank(message = "Numele este obligatoriu")
    @Size(min = 2, max = 50, message = "Numele trebuie să aibă între 2 și 50 de caractere")
    @Pattern(regexp = "^[a-zA-ZăîâșțĂÎÂȘȚ -]+$", message = "Numele poate conține doar litere, spații și cratime")
    private String nume;

    @NotBlank(message = "Prenumele este obligatoriu")
    @Size(min = 2, max = 50, message = "Prenumele trebuie să aibă între 2 și 50 de caractere")
    @Pattern(regexp = "^[a-zA-ZăîâșțĂÎÂȘȚ -]+$", message = "Prenumele poate conține doar litere, spații și cratime")
    private String prenume;

    @Pattern(regexp = "^[0-9]{13}$", message = "CNP-ul trebuie să conțină exact 13 cifre")
    private String cnp;

    @Email(message = "Adresa de email trebuie să fie validă")
    private String email;

    @Pattern(regexp = "^(\\+4|0)[0-9]{9}$", message = "Numărul de telefon trebuie să fie în format românesc (07xxxxxxxx sau +407xxxxxxxx)")
    private String telefon;

    @Size(max = 500, message = "Observațiile nu pot depăși 500 de caractere")
    private String observatii;
}
