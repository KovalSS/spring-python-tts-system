# RabbitMQ configuration
RABBITMQ_URL = "amqp://user:password@localhost:5672/"
INPUT_QUEUE = "tts_requests"   # Java writes here, Python reads
OUTPUT_QUEUE = "tts_responses" # Python writes here, Java reads