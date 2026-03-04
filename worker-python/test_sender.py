import asyncio
import aio_pika
import json
import uuid


async def main():
    connection = await aio_pika.connect_robust("amqp://user:password@localhost:5672/")
    channel = await connection.channel()

    job_id = str(uuid.uuid4())
    message = {
        "jobId": job_id,
        "text": "Привіт! Це тестове повідомлення через RabbitMQ. Архітектура працює чудово!",
        "voiceId": "uk-UA-OstapNeural"
    }

    await channel.default_exchange.publish(
        aio_pika.Message(body=json.dumps(message).encode()),
        routing_key="tts_requests"
    )
    print(f"Надіслано тестове завдання з ID: {job_id}")
    await connection.close()


if __name__ == "__main__":
    asyncio.run(main())