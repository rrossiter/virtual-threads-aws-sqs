#!/bin/bash

set -e

QUEUE_NAME="order-events-queue"
REGION="sa-east-1"
TOTAL_MENSAGENS=100

PRODUTOS=(
  "Notebook Dell"
  "Mouse Logitech"
  "Teclado Mecanico"
  "Monitor LG 27"
  "Headset Sony"
  "Webcam Logitech"
  "SSD Samsung 1TB"
  "Memoria RAM 32GB"
  "Hub USB-C"
  "Suporte Notebook"
)

# -----------------------------------------------------------------------------
echo "Criando fila SQS: $QUEUE_NAME na regiao $REGION..."

QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name "$QUEUE_NAME" \
  --region "$REGION" \
  --query 'QueueUrl' \
  --output text)

echo "Fila criada: $QUEUE_URL"

# -----------------------------------------------------------------------------
echo "Inserindo $TOTAL_MENSAGENS mensagens na fila..."

for i in $(seq 1 $TOTAL_MENSAGENS); do
  PRODUTO="${PRODUTOS[$((RANDOM % ${#PRODUTOS[@]}))]}"
  ORDER_ID=$(cat /proc/sys/kernel/random/uuid 2>/dev/null || uuidgen | tr '[:upper:]' '[:lower:]')
  QUANTITY=$((RANDOM % 10 + 1))
  PRICE="$((RANDOM % 9900 + 100)).$(printf '%02d' $((RANDOM % 100)))"

  BODY=$(printf '{"orderId":"%s","product":"%s","quantity":%d,"totalPrice":%s}' \
    "$ORDER_ID" "$PRODUTO" "$QUANTITY" "$PRICE")

  awslocal sqs send-message \
    --queue-url "$QUEUE_URL" \
    --region "$REGION" \
    --message-body "$BODY" \
    --message-attributes '{
      "eventType": {
        "DataType": "String",
        "StringValue": "ORDER_CREATED"
      }
    }' \
    --output text > /dev/null

  echo "  [$i/$TOTAL_MENSAGENS] produto=$PRODUTO | qtd=$QUANTITY | total=$PRICE"
done

# -----------------------------------------------------------------------------
TOTAL=$(awslocal sqs get-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --region "$REGION" \
  --attribute-names ApproximateNumberOfMessages \
  --query 'Attributes.ApproximateNumberOfMessages' \
  --output text)

echo ""
echo "=============================="
echo "Fila     : $QUEUE_NAME"
echo "Regiao   : $REGION"
echo "Mensagens: $TOTAL na fila"
echo "=============================="
