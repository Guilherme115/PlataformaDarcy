#!/usr/bin/env python3
"""
Script para rodar os testes do intelligence-engine.

Uso:
    python run_tests.py           # Roda todos os testes
    python run_tests.py -v        # Roda com verbose
    python run_tests.py --cov     # Roda com cobertura de código
"""
import subprocess
import sys
import os

def main():
    # Muda para o diretório do script
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
    # Argumentos base
    args = ["python", "-m", "pytest"]
    
    # Adiciona argumentos da linha de comando
    if "--cov" in sys.argv:
        args.extend(["--cov=src", "--cov-report=term-missing"])
        sys.argv.remove("--cov")
    
    # Passa outros argumentos para o pytest
    args.extend([arg for arg in sys.argv[1:] if arg != sys.argv[0]])
    
    # Executa os testes
    print("🧪 Rodando testes...")
    print(f"   Comando: {' '.join(args)}")
    print("-" * 50)
    
    result = subprocess.run(args)
    
    if result.returncode == 0:
        print("\n✅ Todos os testes passaram!")
    else:
        print(f"\n❌ Alguns testes falharam (código: {result.returncode})")
    
    return result.returncode


if __name__ == "__main__":
    sys.exit(main())
