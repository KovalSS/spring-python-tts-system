# RabbitMQ configuration
RABBITMQ_URL = "amqp://user:password@localhost:5672/"
INPUT_QUEUE = "tts_requests"   # Java writes here, Python reads
OUTPUT_QUEUE = "tts_responses" # Python writes here, Java reads

MINIO_URL = "localhost:9000"
MINIO_ACCESS_KEY = "admin"
MINIO_SECRET_KEY = "password123"
TEXT_BUCKET = "text-bucket"
SPEECH_BUCKET = "speech-bucket"