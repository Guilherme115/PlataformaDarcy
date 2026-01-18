import React from 'react';
import { Network, Database, Lock } from 'lucide-react';

const Philosophy: React.FC = () => {
  return (
    <section className="py-20 md:py-32 bg-white" id="filosofia">
      <div className="container mx-auto px-6 max-w-5xl text-center">
        <div className="inline-block bg-yellow-400 text-black px-4 py-1 font-bold border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] uppercase text-xs mb-8 tracking-widest">
          Liberdade Educacional
        </div>
        
        <h2 className="text-4xl md:text-6xl font-black leading-tight mb-8 text-black">
          NÃO IMPORTA ONDE VOCÊ ESTUDA.<br/>
          O QUE IMPORTA É <span className="relative inline-block text-white bg-black px-2 mx-1 italic font-serif-display transform -skew-x-6">COMO</span> VOCÊ ORGANIZA.
        </h2>

        <div className="text-left text-lg md:text-xl leading-relaxed space-y-6 font-medium text-black max-w-3xl mx-auto">
          <p>
            Sabemos que você já tem acesso a muito conteúdo. O problema não é falta de aula, é o <strong>excesso de ruído</strong>. É PDF espalhado no WhatsApp, vídeo solto no YouTube, drive compartilhado confuso.
          </p>
          <p>
            A <strong className="bg-yellow-400 px-1 border border-black">Plataforma Darcy</strong> funciona como um sistema operacional de dados. Nós organizamos o caos.
          </p>
          <p className="p-6 bg-blue-50 border-2 border-black italic text-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
            "Imagine um lugar onde você pode subir aquele material incrível que achou na internet, e nossa IA automaticamente classifica, resume e indica se aquilo é relevante para o PAS ou não."
          </p>
        </div>

        {/* Features Grid Mini - Brutalist Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20 text-left">
          
          <div className="p-6 border-2 border-black bg-white shadow-[8px_8px_0px_0px_#000] hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[6px_6px_0px_0px_#000] transition-all">
            <div className="w-12 h-12 bg-blue-600 border-2 border-black flex items-center justify-center mb-4">
               <Network className="w-6 h-6 text-white" />
            </div>
            <h3 className="font-black text-xl mb-3 uppercase">Conhecimento Aberto</h3>
            <p className="text-sm font-medium text-gray-700 leading-snug">Uma rede onde estudantes compartilham materiais de diversas fontes. A inteligência coletiva vence.</p>
          </div>

          <div className="p-6 border-2 border-black bg-yellow-400 shadow-[8px_8px_0px_0px_#000] hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[6px_6px_0px_0px_#000] transition-all relative">
             <div className="absolute -top-3 -right-3 bg-white border-2 border-black px-2 py-0.5 text-[10px] font-bold uppercase">Destaque</div>
            <div className="w-12 h-12 bg-black border-2 border-black flex items-center justify-center mb-4">
               <Database className="w-6 h-6 text-yellow-400" />
            </div>
            <h3 className="font-black text-xl mb-3 uppercase">Curadoria por IA</h3>
            <p className="text-sm font-medium text-black leading-snug">O Gemini processa cada arquivo enviado. Ele te diz: "Isso cai no PAS 2" ou "Isso é perda de tempo".</p>
          </div>

          <div className="p-6 border-2 border-black bg-white shadow-[8px_8px_0px_0px_#000] hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[6px_6px_0px_0px_#000] transition-all">
            <div className="w-12 h-12 bg-red-600 border-2 border-black flex items-center justify-center mb-4">
               <Lock className="w-6 h-6 text-white" />
            </div>
            <h3 className="font-black text-xl mb-3 uppercase">Seu Cofre Pessoal</h3>
            <p className="text-sm font-medium text-gray-700 leading-snug">Salve seus melhores resumos e playlists privadas. Tudo indexado e buscável em segundos.</p>
          </div>

        </div>

      </div>
    </section>
  );
};

export default Philosophy;