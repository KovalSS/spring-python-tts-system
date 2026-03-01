package com.example.backendspring.entity;

public enum FileStatus {
    CREATED,    // Щойно створено
    QUEUED,     // Відправлено в чергу
    PROCESSING, // Python зараз генерує аудіо
    DONE,       // Успішно згенеровано
    ERROR       // Помилка генерації
}