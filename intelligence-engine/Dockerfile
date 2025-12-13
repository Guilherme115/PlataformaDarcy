# Usa uma imagem leve do Python
FROM python:3.10-slim

# Mantém o linux atualizado e limpo (sem libs gráficas pesadas)
RUN apt-get update && apt-get install -y \
    && rm -rf /var/lib/apt/lists/*

# Cria a pasta de trabalho
WORKDIR /app

# 1. Copia apenas o requirements primeiro (para cachear o pip install)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 2. Copia o código fonte
COPY src/ ./src/

# Comando padrão
CMD ["python", "src/main.py"]