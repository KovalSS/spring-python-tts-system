# rabbitmq_worker.py
import asyncio
import json
import os
import aio_pika
from minio import Minio
from config import INPUT_QUEUE, OUTPUT_QUEUE, MINIO_URL, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, TEXT_BUCKET, SPEECH_BUCKET
from tts_engine import TTSEngine


class RabbitMQWorker:
    """Class responsible for RabbitMQ connection and process coordination"""

    def __init__(self, amqp_url: str):
        self.amqp_url = amqp_url
        self.connection = None
        self.channel = None

        self.minio_client = Minio(
            MINIO_URL,
            access_key=MINIO_ACCESS_KEY,
            secret_key=MINIO_SECRET_KEY,
            secure=False
        )

        if not self.minio_client.bucket_exists(SPEECH_BUCKET):
            self.minio_client.make_bucket(SPEECH_BUCKET)

    async def connect(self):
        print("Connecting to RabbitMQ...")
        self.connection = await aio_pika.connect_robust(self.amqp_url)
        self.channel = await self.connection.channel()

        # Declare queues to ensure they exist
        await self.channel.declare_queue(INPUT_QUEUE, durable=True)
        await self.channel.declare_queue(OUTPUT_QUEUE, durable=True)
        print("Connected successfully! Waiting for tasks...")

    async def send_status_update(self, job_id: str, status: str, result_file: str = None, error_msg: str = None):
        """Sends status update message back to Java"""
        message_body = {
            "jobId": job_id,
            "status": status,
            "resultFile": result_file,
            "error": error_msg
        }

        await self.channel.default_exchange.publish(
            aio_pika.Message(
                body=json.dumps(message_body).encode(),
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT
            ),
            routing_key=OUTPUT_QUEUE
        )
        print(f"[{job_id}] Status update sent: {status}")

    async def process_message(self, message: aio_pika.abc.AbstractIncomingMessage):
        """Processes a single message from the queue"""
        async with message.process():
            try:
                data = json.loads(message.body.decode())
                job_id = data.get("jobId")
                text = data.get("text")
                source_path = data.get("sourcePath")

                voice = data.get("voiceId", "uk-UA-OstapNeural")
                rate = data.get("rate", "+0%")
                pitch = data.get("pitch", "+0Hz")
                volume = data.get("volume", "+0%")

                print(f"\nReceived task: {job_id}")

                await self.send_status_update(job_id, "PROCESSING")

                actual_text = text

                if source_path and not actual_text:
                    print(f"[{job_id}] Downloading text file {source_path} from MinIO...")
                    response = self.minio_client.get_object(TEXT_BUCKET, source_path)
                    actual_text = response.read().decode('utf-8')
                    response.close()
                    response.release_conn()

                if not actual_text:
                    raise ValueError("Ні текст, ні файл не були передані!")

                local_output_file = await TTSEngine.generate_audio(job_id, actual_text, voice, rate, pitch, volume)

                result_object_name = f"{job_id}.mp3"
                self.minio_client.fput_object(
                    SPEECH_BUCKET,
                    result_object_name,
                    local_output_file,
                    content_type="audio/mpeg"
                )
                print(f"[{job_id}] Audio uploaded to MinIO as {result_object_name}")

                if os.path.exists(local_output_file):
                    os.remove(local_output_file)

                await self.send_status_update(job_id, "DONE", result_file=result_object_name)

            except Exception as e:
                print(f"Error processing task: {e}")
                if 'job_id' in locals():
                    await self.send_status_update(job_id, "ERROR", error_msg=str(e))

    async def start_consuming(self):
        """Starts infinite loop to consume messages from the queue"""
        queue = await self.channel.get_queue(INPUT_QUEUE)
        await queue.consume(self.process_message)
        await asyncio.Future()