import pytest
from unittest.mock import AsyncMock, patch
from tts_engine import TTSEngine


@pytest.mark.asyncio
async def test_generate_audio_success():
    job_id = "test-uuid-1234"
    text = "Привіт! Це тест."
    voice = "uk-UA-OstapNeural"
    rate = "+0%"
    pitch = "+0Hz"
    volume = "+0%"

    expected_output_file = f"results/{job_id}.mp3"

    with patch("tts_engine.os.makedirs") as mock_makedirs, \
            patch("tts_engine.edge_tts.Communicate") as mock_communicate:
        mock_instance = mock_communicate.return_value
        mock_instance.save = AsyncMock()

        result = await TTSEngine.generate_audio(job_id, text, voice, rate, pitch, volume)

        mock_makedirs.assert_called_once_with("results", exist_ok=True)

        mock_communicate.assert_called_once_with(
            text=text,
            voice=voice,
            rate=rate,
            pitch=pitch,
            volume=volume
        )

        mock_instance.save.assert_awaited_once_with(expected_output_file)

        assert result == expected_output_file