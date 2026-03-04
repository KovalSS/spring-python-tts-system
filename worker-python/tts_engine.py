import os
import edge_tts

class TTSEngine:
    """Class responsible exclusively for audio generation"""

    @staticmethod
    async def generate_audio(job_id: str, text: str, voice: str, rate: str, pitch: str, volume: str) -> str:
        print(f"[{job_id}] Starting audio generation with settings: Voice={voice}, Rate={rate}, Pitch={pitch}, Volume={volume}")

        # Create results folder if it doesn't exist
        os.makedirs("results", exist_ok=True)
        output_file = f"results/{job_id}.mp3"

        communicate = edge_tts.Communicate(
            text=text,
            voice=voice,
            rate=rate,
            pitch=pitch,
            volume=volume
        )
        await communicate.save(output_file)
        print(f"[{job_id}] Done! File saved to: {output_file}")

        return output_file