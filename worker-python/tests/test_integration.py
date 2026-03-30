import pytest
import asyncio
import json
from unittest.mock import patch, AsyncMock
from testcontainers.rabbitmq import RabbitMqContainer
import aio_pika
from rabbitmq_worker import RabbitMQWorker
from config import INPUT_QUEUE, OUTPUT_QUEUE


@pytest.fixture(scope="module")
def rabbitmq_container():
    with RabbitMqContainer("rabbitmq:3-management") as rabbitmq:
        yield rabbitmq


@pytest.mark.asyncio
async def test_contract_successful_processing(rabbitmq_container):
    host = rabbitmq_container.get_container_host_ip()
    port = rabbitmq_container.get_exposed_port(5672)
    amqp_url = f"amqp://guest:guest@{host}:{port}/"

    with patch('rabbitmq_worker.Minio'), \
            patch('rabbitmq_worker.TTSEngine.generate_audio', new_callable=AsyncMock) as mock_generate, \
            patch('rabbitmq_worker.os.path.exists', return_value=True), \
            patch('rabbitmq_worker.os.remove'):
        mock_generate.return_value = "fake_audio.mp3"

        worker = RabbitMQWorker(amqp_url)
        await worker.connect()

        connection = await aio_pika.connect_robust(amqp_url)
        channel = await connection.channel()
        await channel.declare_queue(INPUT_QUEUE, durable=True)
        output_queue = await channel.declare_queue(OUTPUT_QUEUE, durable=True)

        consumer_task = asyncio.create_task(worker.start_consuming())
        await asyncio.sleep(0.5)

        job_id = "integration-job-777"
        message_body = {
            "jobId": job_id,
            "text": "Інтеграційний текст",
            "voiceId": "uk-UA-OstapNeural"
        }

        await channel.default_exchange.publish(
            aio_pika.Message(body=json.dumps(message_body).encode()),
            routing_key=INPUT_QUEUE
        )

        response_body = None
        async with output_queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    current_response = json.loads(message.body.decode())
                    response_body = current_response
                    if current_response.get("status") in ["DONE", "ERROR"]:
                        break

        assert response_body is not None
        assert response_body["jobId"] == job_id
        assert response_body["status"] == "DONE"
        assert response_body["resultFile"] == f"{job_id}.mp3"

        consumer_task.cancel()
        await worker.connection.close()
        await connection.close()


@pytest.mark.asyncio
async def test_contract_error_processing(rabbitmq_container):
    host = rabbitmq_container.get_container_host_ip()
    port = rabbitmq_container.get_exposed_port(5672)
    amqp_url = f"amqp://guest:guest@{host}:{port}/"

    with patch('rabbitmq_worker.Minio'):
        worker = RabbitMQWorker(amqp_url)
        await worker.connect()

        connection = await aio_pika.connect_robust(amqp_url)
        channel = await connection.channel()
        await channel.declare_queue(INPUT_QUEUE, durable=True)
        output_queue = await channel.declare_queue(OUTPUT_QUEUE, durable=True)

        consumer_task = asyncio.create_task(worker.start_consuming())
        await asyncio.sleep(0.5)

        job_id = "integration-fail-000"
        bad_message = {"jobId": job_id, "text": ""}

        await channel.default_exchange.publish(
            aio_pika.Message(body=json.dumps(bad_message).encode()),
            routing_key=INPUT_QUEUE
        )

        response_body = None
        async with output_queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    current_response = json.loads(message.body.decode())
                    response_body = current_response
                    if current_response.get("status") in ["DONE", "ERROR"]:
                        break

        assert response_body is not None
        assert response_body["jobId"] == job_id
        assert response_body["status"] == "ERROR"
        assert "error" in response_body

        consumer_task.cancel()
        await worker.connection.close()
        await connection.close()