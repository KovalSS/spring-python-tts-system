import pytest
import json
from unittest.mock import AsyncMock, patch, MagicMock
from rabbitmq_worker import RabbitMQWorker


@pytest.fixture
def mock_worker():
    with patch('rabbitmq_worker.Minio') as mock_minio:
        worker = RabbitMQWorker("amqp://fake_url")
        worker.channel = AsyncMock()
        worker.channel.default_exchange = AsyncMock()
        yield worker


@pytest.mark.asyncio
async def test_send_status_update(mock_worker):
    await mock_worker.send_status_update("job-123", "DONE", "result.mp3", None)

    mock_worker.channel.default_exchange.publish.assert_called_once()

    call_args = mock_worker.channel.default_exchange.publish.call_args
    message = call_args[0][0]
    body = json.loads(message.body.decode())

    assert body["jobId"] == "job-123"
    assert body["status"] == "DONE"
    assert body["resultFile"] == "result.mp3"


@pytest.mark.asyncio
async def test_process_message_success(mock_worker):
    mock_msg = MagicMock()
    msg_data = {
        "jobId": "job-123",
        "text": "Тестовий текст",
        "voiceId": "uk-UA-OstapNeural",
        "rate": "+0%",
        "pitch": "+0Hz",
        "volume": "+0%"
    }
    mock_msg.body.decode.return_value = json.dumps(msg_data)

    mock_process_cm = AsyncMock()
    mock_process_cm.__aenter__.return_value = None
    mock_process_cm.__aexit__.return_value = None
    mock_msg.process.return_value = mock_process_cm

    with patch('rabbitmq_worker.TTSEngine.generate_audio', new_callable=AsyncMock) as mock_generate, \
            patch('rabbitmq_worker.os.remove') as mock_remove, \
            patch('rabbitmq_worker.os.path.exists') as mock_exists:
        mock_generate.return_value = "local_result.mp3"
        mock_exists.return_value = True

        await mock_worker.process_message(mock_msg)

        mock_generate.assert_called_once_with(
            "job-123", "Тестовий текст", "uk-UA-OstapNeural", "+0%", "+0Hz", "+0%"
        )
        mock_worker.minio_client.fput_object.assert_called_once_with(
            "speech-bucket", "job-123.mp3", "local_result.mp3", content_type="audio/mpeg"
        )

        mock_exists.assert_called_once_with("local_result.mp3")
        mock_remove.assert_called_once_with("local_result.mp3")

@pytest.mark.asyncio
async def test_process_message_error(mock_worker):
    mock_msg = MagicMock()
    msg_data = {
        "jobId": "err-999",
        "text": "",
    }
    mock_msg.body.decode.return_value = json.dumps(msg_data)

    mock_process_cm = AsyncMock()
    mock_process_cm.__aenter__.return_value = None
    mock_process_cm.__aexit__.return_value = None
    mock_msg.process.return_value = mock_process_cm

    await mock_worker.process_message(mock_msg)

    call_args = mock_worker.channel.default_exchange.publish.call_args
    message = call_args[0][0]
    body = json.loads(message.body.decode())

    assert body["jobId"] == "err-999"
    assert body["status"] == "ERROR"
    assert "error" in body


@pytest.mark.asyncio
async def test_process_message_with_source_path(mock_worker):
    mock_msg = MagicMock()
    msg_data = {
        "jobId": "minio-job-123",
        "text": "",
        "sourcePath": "test_text.txt"
    }
    mock_msg.body.decode.return_value = json.dumps(msg_data)

    mock_process_cm = AsyncMock()
    mock_process_cm.__aenter__.return_value = None
    mock_process_cm.__aexit__.return_value = None
    mock_msg.process.return_value = mock_process_cm

    with patch('rabbitmq_worker.TTSEngine.generate_audio', new_callable=AsyncMock) as mock_generate, \
            patch('rabbitmq_worker.os.remove'), \
            patch('rabbitmq_worker.os.path.exists', return_value=True):
        mock_generate.return_value = "local.mp3"

        mock_minio_response = MagicMock()
        mock_minio_response.read.return_value = b"Text from MinIO file"
        mock_worker.minio_client.get_object.return_value = mock_minio_response

        await mock_worker.process_message(mock_msg)

        mock_worker.minio_client.get_object.assert_called_once_with("text-bucket", "test_text.txt")
        mock_generate.assert_called_once_with(
            "minio-job-123", "Text from MinIO file", "uk-UA-OstapNeural", "+0%", "+0Hz", "+0%"
        )


@pytest.mark.asyncio
async def test_init_creates_bucket_if_not_exists():
    with patch('rabbitmq_worker.Minio') as mock_minio_class:
        mock_minio_instance = mock_minio_class.return_value
        mock_minio_instance.bucket_exists.return_value = False

        worker = RabbitMQWorker("amqp://fake_url")

        mock_minio_instance.make_bucket.assert_called_once_with("speech-bucket")


@pytest.mark.asyncio
async def test_process_message_json_error(mock_worker):
    mock_msg = MagicMock()
    mock_msg.body.decode.return_value = "{invalid json string"

    mock_process_cm = AsyncMock()
    mock_process_cm.__aenter__.return_value = None
    mock_process_cm.__aexit__.return_value = None
    mock_msg.process.return_value = mock_process_cm

    await mock_worker.process_message(mock_msg)

    mock_worker.channel.default_exchange.publish.assert_not_called()


@pytest.mark.asyncio
async def test_process_message_file_not_found(mock_worker):
    mock_msg = MagicMock()
    msg_data = {
        "jobId": "job-no-file",
        "text": "Текст",
    }
    mock_msg.body.decode.return_value = json.dumps(msg_data)

    mock_process_cm = AsyncMock()
    mock_process_cm.__aenter__.return_value = None
    mock_process_cm.__aexit__.return_value = None
    mock_msg.process.return_value = mock_process_cm

    with patch('rabbitmq_worker.TTSEngine.generate_audio', new_callable=AsyncMock) as mock_generate, \
            patch('rabbitmq_worker.os.remove') as mock_remove, \
            patch('rabbitmq_worker.os.path.exists') as mock_exists:
        mock_generate.return_value = "ghost.mp3"
        mock_exists.return_value = False

        await mock_worker.process_message(mock_msg)

        mock_exists.assert_called_once_with("ghost.mp3")
        mock_remove.assert_not_called()