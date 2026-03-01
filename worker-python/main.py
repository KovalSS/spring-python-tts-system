import asyncio
import edge_tts

TEXT = "Привіт! Тепер я говорю трохи швидше, а мій голос звучить трохи вище, ніж зазвичай."
VOICE = "uk-UA-OstapNeural"

# Назва файлу для нового тесту
OUTPUT_FILE = "test_custom_audio.mp3"


async def generate_audio():
    print("⏳ Починаємо кастомну генерацію...")

    # Додаємо параметри у Communicate
    communicate = edge_tts.Communicate(
        text=TEXT,
        voice=VOICE,
        rate="+25%",  # Пришвидшуємо на 25%
        pitch="+15Hz",  # Робимо голос вищим на 15 Герц
        volume="+10%"  # Трохи додаємо гучності
    )

    await communicate.save(OUTPUT_FILE)
    print(f"✅ Готово! Аудіо збережено у файл: {OUTPUT_FILE}")


if __name__ == "__main__":
    asyncio.run(generate_audio())