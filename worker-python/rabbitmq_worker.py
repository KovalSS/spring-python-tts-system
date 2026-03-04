# rabbitmq_worker.py
import asyncio
import json
import aio_pika
from config import INPUT_QUEUE, OUTPUT_QUEUE
from tts_engine import TTSEngine

class RabbitMQWorker:
    """Class responsible for RabbitMQ connection and process coordination"""

    def __init__(self, amqp_url: str):
        self.amqp_url = amqp_url
        self.connection = None
        self.channel = None

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
        async with message.process():  # Automatically acknowledges (ack) upon success
            try:
                data = json.loads(message.body.decode())
                job_id = data.get("jobId")
                text = data.get("text")

                voice = data.get("voiceId", "uk-UA-OstapNeural")
                rate = data.get("rate", "+0%")
                pitch = data.get("pitch", "+0Hz")
                volume = data.get("volume", "+0%")

                print(f"\nReceived task: {job_id}")

                # 1. Notify Java that processing has started
                await self.send_status_update(job_id, "PROCESSING")

                # 2. Generate audio with dynamic parameters
                output_file = await TTSEngine.generate_audio(job_id, text, voice, rate, pitch, volume)

                # 3. Notify Java about success
                await self.send_status_update(job_id, "DONE", result_file=output_file)

            except Exception as e:
                print(f"Error processing task: {e}")
                # Notify Java about error
                if 'job_id' in locals():
                    await self.send_status_update(job_id, "ERROR", error_msg=str(e))

    async def start_consuming(self):
        """Starts infinite loop to consume messages from the queue"""
        queue = await self.channel.get_queue(INPUT_QUEUE)
        await queue.consume(self.process_message)
        await asyncio.Future()