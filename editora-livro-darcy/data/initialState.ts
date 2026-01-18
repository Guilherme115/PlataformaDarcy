import { PageContent } from '../types';

export const initialPages: PageContent[] = [
  {
    id: 'page-1',
    title: 'Capa Principal',
    module: 'Capa',
    pageNumber: 1,
    layout: 'cover',
    htmlContent: `<!-- TEMPLATE CAPA -->
<div class="flex justify-between items-start border-b-2 border-black pb-4">
    <div class="flex flex-col">
        <span class="font-mono font-bold tracking-[0.2em] text-sm uppercase">Plataforma Darcy</span>
        <span class="font-mono text-[10px] uppercase text-gray-500">PAS / UnB</span>
    </div>
    <div class="bg-darcy-yellow px-2 py-1 border border-black font-mono font-bold text-xs shadow-[2px_2px_0px_0px_black]">
        VOLUME 01
    </div>
</div>

<div class="flex-1 flex flex-col justify-center items-center text-center my-8 relative">
    <span class="font-mono text-xs uppercase tracking-widest bg-black text-white px-3 py-1 mb-6">
        Manual de Guerra
    </span>

    <h1 class="font-sans font-black text-[90px] leading-[0.9] tracking-tighter uppercase relative z-10">
        Lógica<br/>
        <span class="text-transparent" style="-webkit-text-stroke: 2px black;">& Espaço</span>
    </h1>
    
    <div class="mt-8 flex flex-col items-center gap-2">
            <span class="font-serif italic text-xl font-bold">O Início da Jornada</span>
            <div class="h-1 w-20 bg-black mt-2"></div>
    </div>
</div>

<div class="grid grid-cols-3 gap-0 border-t-2 border-l-2 border-black">
    <div class="p-4 border-r-2 border-b-2 border-black">
        <span class="block font-mono text-[10px] font-bold uppercase mb-1 text-gray-500">Alvo</span>
        <span class="font-sans font-black text-2xl">PAS 1</span>
    </div>
    <div class="p-4 border-r-2 border-b-2 border-black col-span-2 bg-darcy-yellow/20">
        <span class="block font-mono text-[10px] font-bold uppercase mb-1 text-gray-500">Conteúdo</span>
        <span class="font-serif italic text-sm leading-tight block">
            Matemática • Geografia • Lógica de Games
        </span>
    </div>
</div>`
  },
  {
    id: 'page-2',
    title: 'Sumário',
    module: 'Índice',
    pageNumber: 3,
    layout: 'toc',
    htmlContent: `<!-- TEMPLATE SUMÁRIO -->
<div class="w-full max-w-3xl mx-auto">
    
    <!-- Manifesto Section -->
    <div class="mb-8">
        <div class="flex items-baseline justify-between border-b-2 border-dotted border-gray-400 py-3 hover:bg-darcy-yellow/10 transition-colors px-2">
        <span class="font-sans font-black text-lg uppercase flex items-center gap-2">
            Manifesto UnB
        </span>
        <span class="font-mono font-bold">05</span>
        </div>
    </div>

    <div class="font-mono text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 mt-4 pl-2">
        Parte I: Onde Estamos?
    </div>

    <!-- Lista de Capítulos -->
    <div class="flex items-center justify-between border-b border-gray-300 py-4 px-2 hover:pl-6 transition-all cursor-pointer group">
        <div>
            <span class="font-mono text-xs font-bold text-gray-400 mr-4">CAP 01</span>
            <span class="font-serif text-xl font-bold uppercase group-hover:bg-black group-hover:text-white px-1 transition-colors">Coordenadas & Minecraft</span>
            <p class="font-sans text-xs text-gray-500 mt-1 pl-[70px] italic">A lógica de localização</p>
        </div>
        <span class="font-mono font-black text-lg">07</span>
    </div>

    <div class="flex items-center justify-between border-b border-gray-300 py-4 px-2 hover:pl-6 transition-all cursor-pointer group">
        <div>
            <span class="font-mono text-xs font-bold text-gray-400 mr-4">CAP 02</span>
            <span class="font-serif text-xl font-bold uppercase group-hover:bg-black group-hover:text-white px-1 transition-colors">Plano Cartesiano</span>
            <p class="font-sans text-xs text-gray-500 mt-1 pl-[70px] italic">O mapa do tesouro</p>
        </div>
        <span class="font-mono font-black text-lg">25</span>
    </div>

</div>`
  },
  {
    id: 'page-3',
    title: 'Capítulo 01',
    module: 'Módulo 01',
    pageNumber: 7,
    layout: 'standard',
    htmlContent: `<!-- TEMPLATE PÁGINA PADRÃO -->
<p class="mb-6 font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
    <span class="text-7xl font-black float-left mr-3 mt-[-10px]">A</span>
    ntes de falarmos sobre funções complexas, precisamos responder a pergunta mais básica da existência: <strong>"Onde eu estou?"</strong>.
</p>

<p class="mb-4 font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
    Imagine que você está jogando <strong>Minecraft</strong>. Você achou uma vila, mas seu amigo está perdido. Como você explica onde está? Você usa <span class="bg-darcy-yellow px-1 border border-black font-bold">Coordenadas</span>.
</p>

<!-- CAIXA DE DESTAQUE "MINECRAFT" -->
<div class="border-4 border-black p-4 bg-gray-50 my-8 shadow-[4px_4px_0px_0px_black]">
    <div class="flex justify-between mb-2 font-mono text-[10px] font-bold uppercase">
        <span>Server: UnB_PAS_1</span>
        <span>X=20, Y=64, Z=-10</span>
    </div>
    
    <!-- GRID VISUAL CSS -->
    <div class="relative aspect-video bg-white border-2 border-black overflow-hidden" 
         style="background-image: linear-gradient(black 1px, transparent 1px), linear-gradient(90deg, black 1px, transparent 1px); background-size: 40px 40px;">
        
        <!-- Eixos -->
        <div class="absolute top-1/2 left-0 w-full h-0.5 bg-black"></div>
        <div class="absolute top-0 left-1/2 h-full w-0.5 bg-black"></div>

        <!-- Player Marker -->
        <div class="absolute top-[30%] left-[70%] w-4 h-4 bg-darcy-yellow border-2 border-black"></div>
        <div class="absolute top-[30%] left-[70%] mt-5 ml-[-20px] bg-black text-white text-[9px] font-mono px-1">VOCÊ (3, 4)</div>
    </div>
</div>

<h2 class="font-sans font-black text-xl uppercase mt-8 mb-4 border-l-4 border-black pl-3">
    O Sistema XYZ
</h2>

<p class="font-serif text-[11.5pt] leading-relaxed text-justify font-medium">
    Em qualquer jogo 3D (e na vida real), usamos três valores. No PAS 1, focamos no mapa 2D (X e Y).
</p>

<!-- CAIXA DE CONCEITO -->
<div class="relative border-[3px] border-black bg-white p-6 my-8 shadow-[6px_6px_0px_0px_black]">
    <div class="absolute -top-3 left-0 bg-black text-white px-3 py-1 text-[10px] font-mono uppercase font-bold border-2 border-white">
        Definição Espacial
    </div>
    <ul class="space-y-4 font-serif mt-2">
        <li class="flex gap-4">
            <span class="font-black font-mono text-xl bg-darcy-yellow px-2 h-fit">X</span>
            <div>
                <strong class="block text-sm uppercase font-sans font-black">Longitude</strong>
                Eixo horizontal. Direita (+) e Esquerda (-).
            </div>
        </li>
        <li class="flex gap-4">
            <span class="font-black font-mono text-xl bg-black text-white px-2 h-fit">Y</span>
            <div>
                <strong class="block text-sm uppercase font-sans font-black">Latitude</strong>
                Eixo vertical. Norte (+) e Sul (-).
            </div>
        </li>
    </ul>
</div>`
  }
];