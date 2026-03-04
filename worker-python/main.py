import asyncio
from config import RABBITMQ_URL
from rabbitmq_worker import RabbitMQWorker

async def main():
    worker = RabbitMQWorker(RABBITMQ_URL)
    await worker.connect()
    await worker.start_consuming()

if __name__ == "__main__":
    asyncio.run(main())