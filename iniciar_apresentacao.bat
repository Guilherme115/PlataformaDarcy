@echo off
title Iniciando Plataforma Darcy para Apresentacao
color 0A

echo ============================================
echo    PLATAFORMA DARCY - MODO APRESENTACAO
echo ============================================
echo.

echo [1/4] Iniciando banco de dados MySQL...
cd /d "C:\Users\Guilherme R\PycharmProjects\projeto_pas_docker\intelligence-engine"
docker-compose up -d db
if %errorlevel% neq 0 (
    echo ERRO: Falha ao iniciar o banco de dados!
    pause
    exit /b 1
)

echo.
echo [2/4] Aguardando banco iniciar (15 segundos)...
timeout /t 15 /nobreak

echo.
echo [3/4] Iniciando aplicacao Spring Boot...
echo       (Isso abrira uma nova janela)
cd /d "C:\Users\Guilherme R\PycharmProjects\projeto_pas_docker\backend-core"
start "Plataforma Darcy - Backend" cmd /k "mvn spring-boot:run"

echo.
echo [4/4] Aguardando aplicacao iniciar (45 segundos)...
echo       Voce pode acompanhar na outra janela...
timeout /t 45 /nobreak

echo.
echo ============================================
echo    INICIANDO TUNEL NGROK
echo ============================================
echo.
echo IMPORTANTE: Anote a URL que aparecera abaixo!
echo Ela sera algo como: https://xxxxx.ngrok-free.app
echo.
echo Pressione CTRL+C para encerrar o ngrok
echo.

ngrok http 8080

echo.
echo Ngrok encerrado.
pause
