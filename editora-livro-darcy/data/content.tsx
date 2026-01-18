import React from 'react';
import { PageContent } from '../types';
import { HighlightBox, GraphExample } from '../components/PageElements';
import { Crosshair, Sigma, ShieldAlert, Map, Box, MousePointer2 } from 'lucide-react';

// Componente visual personalizado para a explicação do Minecraft/Blocos
const MinecraftGrid = () => (
    <div className="border-4 border-black p-4 bg-gray-50 my-6 shadow-brutal-sm">
        <div className="flex justify-between mb-2 font-mono text-[10px] font-bold uppercase">
            <span>Server: UnB_PAS_1</span>
            <span>Coords: X=20, Y=64, Z=-10</span>
        </div>
        <div className="relative aspect-video bg-white border-2 border-black overflow-hidden pattern-grid-lg">
            {/* Grid Lines */}
            <div className="absolute inset-0" style={{ 
                backgroundImage: 'linear-gradient(black 1px, transparent 1px), linear-gradient(90deg, black 1px, transparent 1px)', 
                backgroundSize: '40px 40px',
                opacity: 0.1
            }}></div>
            
            {/* Eixos */}
            <div className="absolute top-1/2 left-0 w-full h-0.5 bg-black"></div>
            <div className="absolute top-0 left-1/2 h-full w-0.5 bg-black"></div>

            {/* Ponto "Steve" */}
            <div className="absolute top-[30%] left-[70%] transform -translate-x-1/2 -translate-y-1/2 flex flex-col items-center">
                <div className="w-4 h-4 bg-darcy-yellow border-2 border-black mb-1 animate-bounce"></div>
                <span className="font-mono text-[9px] font-black bg-black text-white px-1">VOCÊ (3, 4)</span>
            </div>

            {/* Ponto "Casa" */}
            <div className="absolute top-[60%] left-[30%] transform -translate-x-1/2 -translate-y-1/2 flex flex-col items-center">
                <div className="w-4 h-4 bg-blue-500 border-2 border-black mb-1"></div>
                <span className="font-mono text-[9px] font-black bg-black text-white px-1">BASE (-2, -1)</span>
            </div>
        </div>
        <p className="font-mono text-[10px] mt-2 text-center text-gray-500">
            Figura 1.1: O mundo real é apenas um grande servidor com coordenadas.
        </p>
    </div>
);

export const samplePages: PageContent[] = [
  // --- CAPA ---
  {
    id: 'cover',
    layout: 'cover',
    pageNumber: 1,
    content: (
        <>
            <div className="flex justify-between items-start border-b-2 border-black pb-4">
                <div className="flex flex-col">
                    <span className="font-mono font-bold tracking-[0.2em] text-sm uppercase">Plataforma Darcy</span>
                    <span className="font-mono text-[10px] uppercase text-gray-500">Estratégia para o PAS/UnB</span>
                </div>
                <div className="bg-darcy-yellow px-2 py-1 border border-black font-mono font-bold text-xs shadow-[2px_2px_0px_0px_black]">
                    ETAPA 01
                </div>
            </div>

            <div className="flex-1 flex flex-col justify-center items-center text-center my-8 relative">
                <div className="absolute inset-0 flex items-center justify-center opacity-[0.05] pointer-events-none">
                     <Box size={400} strokeWidth={1} />
                </div>

                <span className="font-mono text-xs uppercase tracking-widest bg-black text-white px-3 py-1 mb-6">
                    Módulo Fundamental
                </span>

                <h1 className="font-sans font-black text-[90px] leading-[0.9] tracking-tighter uppercase relative z-10">
                    Lógica<br/>
                    <span className="text-transparent" style={{ WebkitTextStroke: '2px black' }}>& Espaço</span>
                </h1>
                
                <div className="mt-8 flex flex-col items-center gap-2">
                     <span className="font-serif italic text-xl font-bold">O Início da Jornada</span>
                     <span className="font-mono text-xs bg-gray-200 px-2 py-1">VOLUME 01 DE 12</span>
                </div>
            </div>

            <div className="grid grid-cols-3 gap-0 border-t-2 border-l-2 border-black">
                <div className="p-4 border-r-2 border-b-2 border-black">
                    <span className="block font-mono text-[10px] font-bold uppercase mb-1 text-gray-500">Alvo</span>
                    <span className="font-sans font-black text-2xl">PAS 1</span>
                </div>
                <div className="p-4 border-r-2 border-b-2 border-black col-span-2 bg-darcy-yellow/20">
                    <span className="block font-mono text-[10px] font-bold uppercase mb-1 text-gray-500">Interdisciplinaridade</span>
                    <span className="font-serif italic text-sm leading-tight block">
                        Matemática • Geografia • Física • Artes Visuais
                    </span>
                </div>
            </div>
        </>
    )
  },

  // --- SUMÁRIO ---
  {
    id: 'toc',
    layout: 'toc',
    pageNumber: 3,
    content: (
        <div className="w-full max-w-3xl mx-auto">
            <div className="grid gap-2">
                <div className="mb-6">
                     <div className="flex items-baseline justify-between border-b-2 border-dotted border-gray-400 py-3 hover:bg-darcy-yellow/10 transition-colors px-2">
                        <span className="font-sans font-black text-lg uppercase flex items-center gap-2">
                            <ShieldAlert className="w-5 h-5" /> Manifesto UnB
                        </span>
                        <span className="font-mono font-bold">05</span>
                     </div>
                </div>

                <div className="font-mono text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 mt-4 pl-2">
                    Parte I: Onde Estamos?
                </div>

                {[
                    { title: "Coordenadas & Minecraft", sub: "A lógica de localização no espaço", page: "07" },
                    { title: "Plano Cartesiano", sub: "O mapa do tesouro de Descartes", page: "25" },
                    { title: "Pares Ordenados", sub: "Endereços matemáticos (x, y)", page: "42" },
                    { title: "Distância entre Pontos", sub: "Pitágoras disfarçado", page: "60" },
                ].map((item, idx) => (
                    <div key={idx} className="flex items-center justify-between border-b border-gray-300 py-4 px-2 hover:pl-6 transition-all cursor-pointer group">
                        <div>
                            <span className="font-mono text-xs font-bold text-gray-400 mr-4">CAP {String(idx + 1).padStart(2, '0')}</span>
                            <span className="font-serif text-xl font-bold uppercase group-hover:text-darcy-yellow/100 group-hover:bg-black px-1 transition-colors">{item.title}</span>
                            <p className="font-sans text-xs text-gray-500 mt-1 pl-[90px] italic hidden sm:block">{item.sub}</p>
                        </div>
                        <span className="font-mono font-black text-lg">{item.page}</span>
                    </div>
                ))}

                <div className="font-mono text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 mt-8 pl-2">
                    Parte II: As Regras do Jogo
                </div>

                {[
                    { title: "Conjuntos Numéricos", sub: "Classificando os 'itens' do inventário", page: "85" },
                    { title: "Intervalos Reais", sub: "A linha da vida", page: "110" },
                    { title: "Introdução a Funções", sub: "Input, Processamento e Output", page: "135" },
                ].map((item, idx) => (
                    <div key={idx + 4} className="flex items-center justify-between border-b border-gray-300 py-4 px-2 hover:pl-6 transition-all cursor-pointer group">
                        <div>
                            <span className="font-mono text-xs font-bold text-gray-400 mr-4">CAP {String(idx + 5).padStart(2, '0')}</span>
                            <span className="font-serif text-xl font-bold uppercase group-hover:text-darcy-yellow/100 group-hover:bg-black px-1 transition-colors">{item.title}</span>
                            <p className="font-sans text-xs text-gray-500 mt-1 pl-[90px] italic hidden sm:block">{item.sub}</p>
                        </div>
                        <span className="font-mono font-black text-lg">{item.page}</span>
                    </div>
                ))}
            </div>
        </div>
    )
  },

  // --- CAPÍTULO 1: MINECRAFT / COORDENADAS ---
  {
    id: 'pas-01-intro',
    module: 'Capítulo 01 • Localização',
    title: 'O Mundo em Blocos',
    pageNumber: 7,
    layout: 'standard',
    marginNotes: [
      {
        id: 'mn-01',
        type: 'note',
        title: 'Interdisciplinaridade',
        content: 'Na Geografia, isso é Latitude (Y) e Longitude (X). O conceito é idêntico: cruzar duas linhas para achar um ponto.'
      },
      {
        id: 'mn-02',
        type: 'tip',
        title: 'Dica Gamer',
        content: 'Pressione F3 no Minecraft. Aqueles números flutuando na tela são pura Geometria Analítica.'
      }
    ],
    content: (
      <>
        <p className="mb-6 font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
          <span className="text-7xl font-black float-left mr-3 mt-[-10px]">A</span>
          ntes de falarmos sobre funções complexas, precisamos responder a pergunta mais básica da existência: <strong>"Onde eu estou?"</strong>.
        </p>
        <p className="mb-4 font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
          Imagine que você está jogando Minecraft com seu amigo. Você achou uma vila incrível, mas ele está perdido na floresta. Como você explica para ele onde você está? Dizer "perto da árvore grande" não funciona num mundo infinito.
        </p>
        <p className="mb-4 font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
          Você precisa de um sistema de referência. Você precisa de <strong>Coordenadas</strong>.
        </p>

        <MinecraftGrid />

        <h2 className="font-sans font-black text-xl uppercase mt-8 mb-4 flex items-center gap-3 border-l-4 border-black pl-3">
          <MousePointer2 className="w-6 h-6 text-black" /> 
          O Sistema XYZ
        </h2>

        <p className="font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
          Em qualquer jogo 3D (e na vida real), usamos três valores para definir uma posição:
        </p>

        <HighlightBox label="Definição Espacial">
            <ul className="space-y-4 font-serif">
                <li className="flex gap-4">
                    <span className="font-black font-mono text-xl bg-darcy-yellow px-2 h-fit">X</span>
                    <div>
                        <strong className="block text-sm uppercase font-sans font-black">Longitude (Leste/Oeste)</strong>
                        O quanto você anda para os lados. No gráfico matemático, é o eixo horizontal.
                    </div>
                </li>
                <li className="flex gap-4">
                    <span className="font-black font-mono text-xl bg-black text-white px-2 h-fit">Z</span>
                    <div>
                        <strong className="block text-sm uppercase font-sans font-black">Latitude (Norte/Sul)</strong>
                        O quanto você anda para frente ou para trás. Na matemática escolar 2D, geralmente ignoramos este eixo, ou o chamamos de Y.
                    </div>
                </li>
                <li className="flex gap-4">
                    <span className="font-black font-mono text-xl border-2 border-black px-2 h-fit">Y</span>
                    <div>
                        <strong className="block text-sm uppercase font-sans font-black">Altitude (Altura)</strong>
                        O quão alto ou fundo você está (Camada 11 para diamantes, lembra?).
                    </div>
                </li>
            </ul>
        </HighlightBox>

        <p className="font-serif text-[11.5pt] leading-relaxed text-justify font-medium mt-6">
          Para o PAS 1, vamos simplificar. Vamos esquecer a altura por enquanto e olhar para o mundo de cima, como um mapa de papel. Sobram apenas dois eixos: <strong>X</strong> e <strong>Y</strong>. Bem-vindo ao Plano Cartesiano.
        </p>
      </>
    )
  },

  // --- CAPÍTULO 2: PLANO CARTESIANO ---
  {
    id: 'pas-02-cartesiano',
    module: 'Capítulo 02 • O Mapa',
    title: 'O Plano Cartesiano',
    pageNumber: 25,
    layout: 'standard',
    marginNotes: [
      {
        id: 'mn-03',
        type: 'quote',
        title: 'História',
        content: 'Diz a lenda que René Descartes criou isso observando uma mosca voando no teto do seu quarto e tentando descrever a posição dela.'
      }
    ],
    content: (
      <>
        <p className="mb-6 font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
           Agora que entendemos a lógica do jogo, vamos formalizar. O Plano Cartesiano nada mais é do que dois eixos numéricos perpendiculares que se cruzam em um ponto central chamado <strong>Origem (0,0)</strong>.
        </p>

        <div className="flex flex-col items-center my-8">
            <div className="border-4 border-black p-4 w-full max-w-md bg-white">
                <svg viewBox="0 0 200 200" className="w-full h-auto">
                    {/* Grid */}
                    <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                        <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#eee" strokeWidth="1"/>
                    </pattern>
                    <rect width="200" height="200" fill="url(#grid)" />
                    
                    {/* Eixos */}
                    <line x1="100" y1="0" x2="100" y2="200" stroke="black" strokeWidth="2" />
                    <line x1="0" y1="100" x2="200" y2="100" stroke="black" strokeWidth="2" />
                    
                    {/* Quadrantes */}
                    <text x="150" y="50" className="text-[10px] font-mono opacity-50">I (+,+)</text>
                    <text x="50" y="50" className="text-[10px] font-mono opacity-50">II (-,+)</text>
                    <text x="50" y="150" className="text-[10px] font-mono opacity-50">III (-,-)</text>
                    <text x="150" y="150" className="text-[10px] font-mono opacity-50">IV (+,-)</text>

                    {/* Ponto Exemplo */}
                    <circle cx="140" cy="60" r="3" fill="#facc15" stroke="black" />
                    <text x="145" y="55" className="text-[8px] font-bold font-mono">P(4, 4)</text>

                    {/* Linhas pontilhadas */}
                    <line x1="140" y1="60" x2="140" y2="100" stroke="black" strokeWidth="1" strokeDasharray="4" />
                    <line x1="140" y1="60" x2="100" y2="60" stroke="black" strokeWidth="1" strokeDasharray="4" />
                </svg>
            </div>
            <span className="font-mono text-[10px] mt-2 text-gray-500 uppercase">Figura 2.1: O campo de batalha matemático.</span>
        </div>

        <HighlightBox label="Conceito Chave: Par Ordenado">
            <div className="text-center font-serif text-2xl font-bold my-2">
                P (x, y)
            </div>
            <p className="text-sm font-sans text-center px-4">
                A ordem importa! O primeiro número <strong>sempre</strong> é o movimento horizontal (Abscissa). O segundo é o vertical (Ordenada).
            </p>
            <div className="mt-4 border-t-2 border-black pt-2 flex justify-center gap-4 text-xs font-mono">
                <span className="bg-gray-200 px-2 py-1">P(2, 3) ≠ P(3, 2)</span>
            </div>
        </HighlightBox>

        <p className="font-serif text-[11.5pt] leading-relaxed text-justify font-medium mt-6">
            <strong>Por que isso cai no PAS?</strong><br/>
            Porque mapas são funções. Gráficos de física (velocidade x tempo) são planos cartesianos. O gráfico de crescimento populacional em Biologia é um plano cartesiano. Se você não sabe ler o mapa, você não sabe jogar o jogo.
        </p>
      </>
    )
  }
];
