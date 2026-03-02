package com.example.backendspring.entity;

public enum JobStatus {
    CREATED,    // Щойно створено
    QUEUED,     // Відправлено в RabbitMQ
    PROCESSING, // Python генерує аудіо
    DONE,       // Успішно згенеровано
    ERROR       // Помилка
}