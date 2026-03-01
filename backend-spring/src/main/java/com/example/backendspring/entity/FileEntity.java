package com.example.backendspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String text;       // Текст для озвучки
    private String voiceId;    // Обраний голос
    private String filePath;   // Шлях до згенерованого файлу

    @Enumerated(EnumType.STRING)
    private FileStatus status; // Наш життєвий цикл
}